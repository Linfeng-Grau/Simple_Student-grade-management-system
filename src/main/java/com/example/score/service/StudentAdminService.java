package com.example.score.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.score.common.BizException;
import com.example.score.common.PasswordPolicy;
import com.example.score.common.Role;
import com.example.score.entity.AppUser;
import com.example.score.entity.Clazz;
import com.example.score.entity.Course;
import com.example.score.entity.CourseStudent;
import com.example.score.entity.Student;
import com.example.score.repository.AppUserRepository;
import com.example.score.repository.ClazzRepository;
import com.example.score.repository.CourseRepository;
import com.example.score.repository.CourseStudentRepository;
import com.example.score.repository.ScoreRepository;
import com.example.score.repository.StudentRepository;
import com.example.score.web.view.CourseStudentsView;
import com.example.score.web.view.EnrollResult;
import com.example.score.web.view.PickableStudent;
import com.example.score.web.view.RegisterResult;
import com.example.score.web.view.StudentRow;

/**
 * 教师端学生管理：登记学生、把已登记学生加入课程、按课程移出、设置班长。
 * 加课只接受库中已登记学生（学号 12 位数字，且只能进本班课程）；移出只影响当前课程。
 */
@Service
public class StudentAdminService {

    /** 学号规则：12 位数字（与演示学号长度一致） */
    private static final Pattern SNO_PATTERN = Pattern.compile("^\\d{12}$");

    /** 姓名规则：2~20 个汉字/英文字母，允许空格与间隔号（少数民族姓名） */
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[\\p{IsHan}A-Za-z][\\p{IsHan}A-Za-z ·]{1,19}$");

    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final StudentRepository studentRepository;
    private final ScoreRepository scoreRepository;
    private final AppUserRepository appUserRepository;
    private final ClazzRepository clazzRepository;

    public StudentAdminService(CourseRepository courseRepository,
                               CourseStudentRepository courseStudentRepository,
                               StudentRepository studentRepository,
                               ScoreRepository scoreRepository,
                               AppUserRepository appUserRepository,
                               ClazzRepository clazzRepository) {
        this.courseRepository = courseRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.studentRepository = studentRepository;
        this.scoreRepository = scoreRepository;
        this.appUserRepository = appUserRepository;
        this.clazzRepository = clazzRepository;
    }

    /** 学生管理页数据：每门课的学生 + 本班未选本课的学生。 */
    public List<CourseStudentsView> coursesWithStudents(String teacherNo) {
        List<Course> courses = courseRepository.findByTeacherNoOrderByCnoAsc(teacherNo);
        Set<String> monitorRefNos = appUserRepository.findByRole(Role.MONITOR).stream()
                .map(AppUser::getRefNo)
                .collect(Collectors.toCollection(HashSet::new));

        List<CourseStudentsView> result = new ArrayList<>(courses.size());
        for (Course course : courses) {
            List<String> snos = courseStudentRepository.findSnoByCno(course.getCno());
            Map<String, Student> enrolled = studentRepository.findAllById(snos).stream()
                    .collect(Collectors.toMap(Student::getSno, Function.identity(), (a, b) -> a));

            List<StudentRow> rows = new ArrayList<>(snos.size());
            for (String sno : snos) {
                Student student = enrolled.get(sno);
                if (student != null) {
                    rows.add(new StudentRow(sno, student.getSname(), monitorRefNos.contains(sno)));
                }
            }

            List<StudentRow> available = new ArrayList<>();
            for (Student student : studentRepository.findByClassNoOrderBySnoAsc(course.getClassNo())) {
                if (!enrolled.containsKey(student.getSno())) {
                    available.add(new StudentRow(student.getSno(), student.getSname(),
                            monitorRefNos.contains(student.getSno())));
                }
            }

            result.add(new CourseStudentsView(course.getCno(), course.getCname(),
                    className(course.getClassNo()), rows, available));
        }
        return result;
    }

    /** 所有班级（登记学生时选班级用）。 */
    public List<Clazz> listClasses() {
        return clazzRepository.findAll(Sort.by("classNo"));
    }

    /** 登记新学生：只入库、不加课，同时建立登录账号（初始密码 114514）。 */
    @Transactional
    public RegisterResult registerStudent(String name, String sno, String classNo) {
        List<String> errors = new ArrayList<>();
        String cleanName = trim(name);
        String cleanSno = trim(sno);
        validateName(cleanName, errors);
        validateSno(cleanSno, errors);

        Clazz clazz = null;
        if (isBlank(classNo)) {
            errors.add("请选择学生所属班级。");
        } else {
            clazz = clazzRepository.findById(classNo.trim()).orElse(null);
            if (clazz == null) {
                errors.add("班级不存在：" + classNo);
            }
        }
        if (!errors.isEmpty()) {
            return new RegisterResult(errors, false);
        }

        Optional<Student> existing = studentRepository.findById(cleanSno);
        if (existing.isPresent()) {
            errors.add("学号 " + cleanSno + " 已登记（姓名：" + existing.get().getSname()
                    + "）。如需修改信息请联系管理员，或核对学号是否输错。");
            return new RegisterResult(errors, false);
        }

        boolean duplicateName = studentRepository
                .findByClassNoOrderBySnoAsc(clazz.getClassNo()).stream()
                .anyMatch(student -> student.getSname().equals(cleanName));

        studentRepository.save(new Student(cleanSno, cleanName, clazz.getClassNo()));

        // 账号与学生信息同时建立：之后无论该学生有没有登录过，都能被加入课程
        if (appUserRepository.findByUsername(cleanSno).isEmpty()) {
            AppUser user = new AppUser(cleanSno, PasswordPolicy.INITIAL_PASSWORD, cleanName,
                    Role.STUDENT, cleanSno, clazz.getClassNo());
            user.setMustChangePassword(true);
            appUserRepository.save(user);
        }
        return new RegisterResult(List.of(), duplicateName);
    }

    /** 第二步下拉框候选：我教的班级里已登记的学生。 */
    public List<PickableStudent> pickableStudents(String teacherNo) {
        List<String> classNos = courseRepository.findByTeacherNoOrderByCnoAsc(teacherNo).stream()
                .map(Course::getClassNo)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<PickableStudent> result = new ArrayList<>();
        for (String classNo : classNos) {
            String label = className(classNo);
            for (Student student : studentRepository.findByClassNoOrderBySnoAsc(classNo)) {
                result.add(new PickableStudent(student.getSno(), student.getSname(), classNo, label));
            }
        }
        return result;
    }

    /**
     * 把下拉框里勾选的已登记学生批量加入某门课或「我的全部课程」，逐个校验：
     * 学号必须已登记、只能加入本班课程，已在课程中的自动忽略。
     */
    @Transactional
    public EnrollResult addStudents(String teacherNo, List<String> snos, String target) {
        List<String> errors = new ArrayList<>();
        List<String> picked = snos == null ? List.of() : snos.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .toList();
        if (picked.isEmpty()) {
            errors.add("请先在下拉框里选择至少一名已登记的学生。");
            return new EnrollResult(errors, 0, 0, 0);
        }

        List<Course> teacherCourses = courseRepository.findByTeacherNoOrderByCnoAsc(teacherNo);
        if (teacherCourses.isEmpty()) {
            errors.add("你当前没有可用于添加学生的课程。");
            return new EnrollResult(errors, 0, 0, 0);
        }

        boolean all = "ALL".equals(target);
        List<Course> targets;
        if (all) {
            targets = teacherCourses;
        } else {
            Course course = teacherCourses.stream()
                    .filter(item -> item.getCno().equals(target))
                    .findFirst()
                    .orElse(null);
            if (course == null) {
                errors.add("请选择要加入的课程。");
                return new EnrollResult(errors, 0, 0, 0);
            }
            targets = List.of(course);
        }

        int added = 0;
        int skipped = 0;
        int already = 0;
        for (String sno : picked) {
            Optional<Student> found = studentRepository.findById(sno);
            if (found.isEmpty()) {
                errors.add("学号 " + sno + " 在系统中不存在（此学生尚未登记）。");
                continue;
            }
            Student student = found.get();
            List<Course> sameClass = targets.stream()
                    .filter(course -> student.getClassNo() != null
                            && student.getClassNo().equals(course.getClassNo()))
                    .toList();
            if (sameClass.isEmpty()) {
                if (all) {
                    skipped++;
                } else {
                    Course sample = targets.get(0);
                    errors.add("「" + student.getSname() + "」（" + sno + "）属于 "
                            + className(student.getClassNo()) + "，《" + sample.getCname()
                            + "》不是该班的课程，不能加入。");
                }
                continue;
            }
            int enrolled = enroll(sno, sameClass);
            added += enrolled;
            already += (sameClass.size() - enrolled);
        }

        if (added == 0 && errors.isEmpty()) {
            errors.add("所选学生都已经在这些课程中，无需重复加入。");
        }
        return new EnrollResult(errors, added, skipped, already);
    }

    /** 课程卡片上的「加入本课」，仍走一遍存在性与班级校验。 */
    @Transactional
    public EnrollResult enrollIntoCourse(String teacherNo, String cno, String sno) {
        List<String> errors = new ArrayList<>();
        Course course = requireOwnedCourse(cno, teacherNo);

        Optional<Student> found = studentRepository.findById(sno == null ? "" : sno.trim());
        if (found.isEmpty()) {
            errors.add("学号 " + sno + " 在系统中不存在（此学生尚未登记）。");
            return new EnrollResult(errors, 0, 0, 0);
        }
        Student student = found.get();
        if (student.getClassNo() == null || !student.getClassNo().equals(course.getClassNo())) {
            errors.add("该学生属于 " + className(student.getClassNo()) + "，《" + course.getCname()
                    + "》是 " + className(course.getClassNo()) + " 的课程，不能加入。");
            return new EnrollResult(errors, 0, 0, 0);
        }
        if (courseStudentRepository.findByCnoAndSno(cno, student.getSno()).isPresent()) {
            errors.add("该学生已经在《" + course.getCname() + "》中。");
            return new EnrollResult(errors, 0, 0, 0);
        }
        courseStudentRepository.save(new CourseStudent(cno, student.getSno()));
        return new EnrollResult(List.of(), 1, 0, 0);
    }

    /** 移出本课：只删该课选课关系与成绩，账号与其它课程不变。 */
    @Transactional
    public void removeStudent(String teacherNo, String cno, String sno) {
        requireOwnedCourse(cno, teacherNo);
        CourseStudent link = courseStudentRepository.findByCnoAndSno(cno, sno)
                .orElseThrow(() -> new BizException("该学生不在本课程中。"));
        scoreRepository.deleteByCnoAndSno(cno, sno);
        courseStudentRepository.delete(link);
    }

    /** 设为班长：同班旧班长自动降为普通学生。 */
    @Transactional
    public void setMonitor(String teacherNo, String sno) {
        Student student = requireTaughtStudent(teacherNo, sno);
        for (AppUser old : appUserRepository.findByRoleAndClassNo(Role.MONITOR, student.getClassNo())) {
            if (!old.getRefNo().equals(sno)) {
                old.setRole(Role.STUDENT);
            }
        }
        AppUser user = appUserRepository.findByRefNo(sno)
                .orElseThrow(() -> new BizException("该学生还没有登录账号。"));
        user.setRole(Role.MONITOR);
    }

    /** 取消班长：该班可以没有班长。 */
    @Transactional
    public void clearMonitor(String teacherNo, String sno) {
        requireTaughtStudent(teacherNo, sno);
        AppUser user = appUserRepository.findByRefNo(sno)
                .orElseThrow(() -> new BizException("该学生还没有登录账号。"));
        if (user.getRole() == Role.MONITOR) {
            user.setRole(Role.STUDENT);
        }
    }

    // 内部工具

    private int enroll(String sno, List<Course> courses) {
        int added = 0;
        for (Course course : courses) {
            if (courseStudentRepository.findByCnoAndSno(course.getCno(), sno).isEmpty()) {
                courseStudentRepository.save(new CourseStudent(course.getCno(), sno));
                added++;
            }
        }
        return added;
    }

    private Course requireOwnedCourse(String cno, String teacherNo) {
        Course course = courseRepository.findById(cno)
                .orElseThrow(() -> new BizException("课程不存在：" + cno));
        if (!course.getTeacherNo().equals(teacherNo)) {
            throw new BizException("你不是该课程的任课教师，无法管理学生。");
        }
        return course;
    }

    /** 校验该生确实选了当前教师的课，防改 URL 越权。 */
    private Student requireTaughtStudent(String teacherNo, String sno) {
        Student student = studentRepository.findById(sno)
                .orElseThrow(() -> new BizException("未找到学号为 " + sno + " 的学生。"));
        List<String> teacherCnos = courseRepository.findByTeacherNoOrderByCnoAsc(teacherNo).stream()
                .map(Course::getCno)
                .toList();
        boolean taught = courseStudentRepository.findCnoBySno(sno).stream()
                .anyMatch(teacherCnos::contains);
        if (!taught) {
            throw new BizException("该学生不在你的课程中。");
        }
        return student;
    }

    private String className(String classNo) {
        if (classNo == null) {
            return "未知班级";
        }
        return clazzRepository.findById(classNo).map(Clazz::getClassName).orElse(classNo);
    }

    private static void validateSno(String sno, List<String> errors) {
        if (sno.isEmpty()) {
            errors.add("请填写学号。");
        } else if (!SNO_PATTERN.matcher(sno).matches()) {
            errors.add("学号必须是 12 位数字（如 202421147011），当前输入：" + sno);
        }
    }

    private static void validateName(String name, List<String> errors) {
        if (name.isEmpty()) {
            errors.add("请填写学生姓名。");
        } else if (!NAME_PATTERN.matcher(name).matches()) {
            errors.add("姓名需为 2~20 个字符，只能包含中文、英文字母、空格或「·」。");
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
