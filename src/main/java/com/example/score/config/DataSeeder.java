package com.example.score.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.score.common.GradeCalculator;
import com.example.score.common.Role;
import com.example.score.entity.AppUser;
import com.example.score.entity.Clazz;
import com.example.score.entity.Course;
import com.example.score.entity.CourseStudent;
import com.example.score.entity.Score;
import com.example.score.entity.Student;
import com.example.score.entity.Teacher;
import com.example.score.repository.AppUserRepository;
import com.example.score.repository.ClazzRepository;
import com.example.score.repository.CourseRepository;
import com.example.score.repository.CourseStudentRepository;
import com.example.score.repository.ScoreRepository;
import com.example.score.repository.StudentRepository;
import com.example.score.repository.TeacherRepository;

/**
 * 演示数据初始化：先清理旧版演示学生，仅在账号表为空时铸造新数据（重启不重复插入、不覆盖成绩）；
 * 学生全为虚构姓名，3 人中随机 1 人当班长。删除 data 目录后重启即可重建。
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /** 演示账号统一密码（6 位，便于课堂演示；教师新建的学生初始密码另为 114514） */
    private static final String DEMO_PASSWORD = "123456";

    /** 演示学生学号（12 位，保留原学号长度规则） */
    private static final List<String> DEMO_SNOS = List.of(
            "202421147011", "202421147012", "202421147013");

    /** 演示学生姓名：全部虚构，且一眼能看出是演示数据 */
    private static final List<String> DEMO_NAMES = List.of(
            "演示学生甲", "演示学生乙", "演示学生丙");

    private static final String DEMO_CLASS_NO = "2401";

    private final ClazzRepository clazzRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final ScoreRepository scoreRepository;
    private final AppUserRepository appUserRepository;

    public DataSeeder(ClazzRepository clazzRepository,
                      TeacherRepository teacherRepository,
                      StudentRepository studentRepository,
                      CourseRepository courseRepository,
                      CourseStudentRepository courseStudentRepository,
                      ScoreRepository scoreRepository,
                      AppUserRepository appUserRepository) {
        this.clazzRepository = clazzRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.scoreRepository = scoreRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int purged = purgeLegacyDemoStudents();
        if (purged > 0) {
            log.info("已清理旧版演示学生 {} 个（含账号）", purged);
        }

        if (appUserRepository.count() > 0) {
            log.info("已有数据，跳过演示数据初始化");
            return;
        }

        // 班级
        List<Clazz> classes = List.of(
                new Clazz("2401", "智能系 2401 班"),
                new Clazz("2402", "计算机 2402 班"),
                new Clazz("2403", "软件工程 2403 班"));
        clazzRepository.saveAll(classes);

        // 教师（保留原有演示账号）
        List<Teacher> teachers = List.of(
                new Teacher("T1001", "张明"),
                new Teacher("T1002", "李静"),
                new Teacher("T1003", "王强"));
        teacherRepository.saveAll(teachers);

        // 课程
        List<Course> courses = List.of(
                new Course("C101", "高等数学", new BigDecimal("4.0"), "2401", "T1001", 30, 70),
                new Course("C102", "大学英语", new BigDecimal("3.0"), "2401", "T1001", 40, 60),
                new Course("C103", "程序设计基础", new BigDecimal("4.0"), "2401", "T1001", 30, 70),
                new Course("C201", "数据结构", new BigDecimal("3.5"), "2401", "T1002", 20, 80),

                new Course("C104", "线性代数", new BigDecimal("3.0"), "2402", "T1002", 30, 70),
                new Course("C105", "操作系统", new BigDecimal("4.0"), "2402", "T1002", 30, 70),
                new Course("C203", "计算机网络", new BigDecimal("3.0"), "2402", "T1002", 40, 60),
                new Course("C204", "Java 程序设计", new BigDecimal("4.0"), "2402", "T1003", 20, 80),

                new Course("C106", "概率论与数理统计", new BigDecimal("3.0"), "2403", "T1003", 30, 70),
                new Course("C107", "软件工程", new BigDecimal("3.0"), "2403", "T1003", 25, 75),
                new Course("C205", "数据库原理", new BigDecimal("4.0"), "2403", "T1003", 30, 70),
                new Course("C206", "前端开发技术", new BigDecimal("3.0"), "2403", "T1001", 40, 60));
        courseRepository.saveAll(courses);

        // 3 位演示学生
        List<Student> students = new ArrayList<>(DEMO_SNOS.size());
        for (int i = 0; i < DEMO_SNOS.size(); i++) {
            students.add(new Student(DEMO_SNOS.get(i), DEMO_NAMES.get(i), DEMO_CLASS_NO));
        }
        studentRepository.saveAll(students);

        // 选课：2401 班的 4 门课
        List<Course> demoCourses = courses.stream()
                .filter(course -> DEMO_CLASS_NO.equals(course.getClassNo()))
                .toList();
        List<CourseStudent> enrollments = new ArrayList<>();
        for (Course course : demoCourses) {
            for (Student student : students) {
                enrollments.add(new CourseStudent(course.getCno(), student.getSno()));
            }
        }
        courseStudentRepository.saveAll(enrollments);

        // 部分成绩，并随机选一位演示学生当班长
        seedScores(demoCourses, students);
        int monitorIndex = new Random().nextInt(students.size());

        List<AppUser> users = new ArrayList<>();
        for (Teacher teacher : teachers) {
            users.add(new AppUser(teacher.getTno(), DEMO_PASSWORD, teacher.getTname(),
                    Role.TEACHER, teacher.getTno(), null));
        }
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            AppUser user = new AppUser(student.getSno(), DEMO_PASSWORD, student.getSname(),
                    i == monitorIndex ? Role.MONITOR : Role.STUDENT,
                    student.getSno(), student.getClassNo());
            user.setDemoAccount(true);
            user.setMustChangePassword(false);
            users.add(user);
        }
        appUserRepository.saveAll(users);

        log.info("演示数据初始化完成：{} 个班级 / {} 名学生 / {} 门课程 / {} 条选课 / {} 个账号，班长={}",
                classes.size(), students.size(), courses.size(), enrollments.size(), users.size(),
                students.get(monitorIndex).getSname());
    }

    /**
     * 清理旧版演示学生（姓名 + 学号都写死），避免升级后还残留旧演示数据。
     *
     * @return 清理的学生数量
     */
    private int purgeLegacyDemoStudents() {
        List<String> legacySnos = new ArrayList<>();
        for (String prefix : List.of("2024211470", "2024211480", "2024211490")) {
            for (int i = 1; i <= 8; i++) {
                legacySnos.add(prefix + String.format("%02d", i));
            }
        }

        List<AppUser> legacyUsers = appUserRepository.findAll().stream()
                .filter(user -> user.getRole() != Role.TEACHER)
                .filter(user -> legacySnos.contains(user.getUsername()))
                .toList();
        if (legacyUsers.isEmpty()) {
            return 0;
        }
        for (AppUser user : legacyUsers) {
            String sno = user.getRefNo() == null ? user.getUsername() : user.getRefNo();
            scoreRepository.deleteAll(scoreRepository.findBySno(sno));
            courseStudentRepository.deleteAll(courseStudentRepository.findBySno(sno));
            appUserRepository.delete(user);
            studentRepository.deleteById(sno);
        }
        return legacyUsers.size();
    }

    /**
     * 给出 4 门课的成绩：前 3 门全录，第 4 门只录前 2 人，用来演示「未录入」与录入进度。
     */
    private void seedScores(List<Course> courses, List<Student> students) {
        Random random = new Random(20261020L);
        List<Score> scores = new ArrayList<>();

        for (int ci = 0; ci < courses.size(); ci++) {
            Course course = courses.get(ci);
            for (int si = 0; si < students.size(); si++) {
                if (ci == courses.size() - 1 && si >= 2) {
                    continue;
                }
                Student student = students.get(si);

                int usual = 70 + random.nextInt(24);              // 70 ~ 93
                int exam = si == 1 ? 48 + random.nextInt(10)       // 演示一个不及格
                        : 66 + random.nextInt(30);               // 66 ~ 95

                BigDecimal usualScore = BigDecimal.valueOf(usual).setScale(1);
                BigDecimal examScore = BigDecimal.valueOf(exam).setScale(1);
                BigDecimal total = GradeCalculator.computeTotal(usualScore, examScore, course.getUsualRatio());

                Score score = new Score(student.getSno(), course.getCno());
                score.updateScores(usualScore, examScore, total);
                scores.add(score);
            }
        }
        scoreRepository.saveAll(scores);
    }
}

