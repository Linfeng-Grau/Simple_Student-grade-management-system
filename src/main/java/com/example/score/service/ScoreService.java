package com.example.score.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.score.common.BizException;
import com.example.score.common.GradeCalculator;
import com.example.score.entity.Clazz;
import com.example.score.entity.Course;
import com.example.score.entity.Score;
import com.example.score.entity.Student;
import com.example.score.repository.ClazzRepository;
import com.example.score.repository.CourseRepository;
import com.example.score.repository.CourseStudentRepository;
import com.example.score.repository.ScoreRepository;
import com.example.score.repository.StudentRepository;
import com.example.score.web.view.CourseEntryPage;
import com.example.score.web.view.ScoreRowView;
import com.example.score.web.view.StudentScoreView;
import com.example.score.web.view.StudentSummary;
import com.example.score.web.view.TeacherCourseView;

/** 成绩业务：教师录入、学生查询。 */
@Service
public class ScoreService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseStudentRepository courseStudentRepository;
    private final ScoreRepository scoreRepository;
    private final ClazzRepository clazzRepository;

    public ScoreService(StudentRepository studentRepository,
                        CourseRepository courseRepository,
                        CourseStudentRepository courseStudentRepository,
                        ScoreRepository scoreRepository,
                        ClazzRepository clazzRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.courseStudentRepository = courseStudentRepository;
        this.scoreRepository = scoreRepository;
        this.clazzRepository = clazzRepository;
    }

    // 教师端

    /** 教师所教课程，含录入进度与班级平均分。 */
    public List<TeacherCourseView> teacherCourses(String teacherNo) {
        List<Course> courses = courseRepository.findByTeacherNoOrderByCnoAsc(teacherNo);
        List<TeacherCourseView> result = new ArrayList<>(courses.size());
        for (Course course : courses) {
            List<BigDecimal> totals = scoreRepository.findByCno(course.getCno()).stream()
                    .map(Score::getTotalScore)
                    .filter(Objects::nonNull)
                    .toList();
            result.add(new TeacherCourseView(
                    course.getCno(),
                    course.getCname(),
                    course.getCredit(),
                    className(course.getClassNo()),
                    (int) courseStudentRepository.countByCno(course.getCno()),
                    totals.size(),
                    GradeCalculator.average(totals),
                    course.getUsualRatio(),
                    course.getExamRatio()));
        }
        return result;
    }

    /** 录入页数据：课程、选课学生、已录成绩与统计。 */
    public CourseEntryPage teacherEntryPage(String cno, String teacherNo) {
        Course course = requireOwnedCourse(cno, teacherNo);
        List<Student> students = enrolledStudents(cno);
        Map<String, Score> scoreMap = scoreRepository.findByCno(cno).stream()
                .collect(Collectors.toMap(Score::getSno, Function.identity(), (a, b) -> a));

        List<ScoreRowView> rows = new ArrayList<>(students.size());
        for (Student student : students) {
            Score score = scoreMap.get(student.getSno());
            rows.add(new ScoreRowView(
                    student.getSno(),
                    student.getSname(),
                    score == null ? null : score.getUsualScore(),
                    score == null ? null : score.getExamScore(),
                    score == null ? null : score.getTotalScore()));
        }

        List<BigDecimal> totals = rows.stream()
                .map(ScoreRowView::totalScore)
                .filter(Objects::nonNull)
                .toList();

        return new CourseEntryPage(
                course.getCno(),
                course.getCname(),
                course.getCredit(),
                course.getClassNo(),
                className(course.getClassNo()),
                course.getUsualRatio(),
                course.getExamRatio(),
                rows,
                totals.size(),
                GradeCalculator.average(totals),
                GradeCalculator.max(totals),
                GradeCalculator.min(totals),
                passRate(totals));
    }

    /**
     * 保存整门课的成绩与比例：两项都空表示未录入（删除原成绩），只填一项整批不保存；
     * 校验失败时不产生任何写操作。
     *
     * @return 错误列表，为空表示全部保存成功
     */
    @Transactional
    public List<String> saveScores(String cno, String teacherNo,
                                   String usualRatioRaw, String examRatioRaw,
                                   Map<String, String> params) {
        Course course = requireOwnedCourse(cno, teacherNo);
        List<String> errors = new ArrayList<>();

        Integer usualRatio = parseInt(usualRatioRaw);
        Integer examRatio = parseInt(examRatioRaw);
        boolean ratioOk = true;
        if (usualRatio == null || examRatio == null) {
            errors.add("成绩比例需要填写 0~100 的整数。");
            ratioOk = false;
        } else {
            if (usualRatio < 0 || usualRatio > 100) {
                errors.add("平时成绩比例必须在 0~100 之间。");
                ratioOk = false;
            }
            if (examRatio < 0 || examRatio > 100) {
                errors.add("考试成绩比例必须在 0~100 之间。");
                ratioOk = false;
            }
            if (ratioOk && usualRatio + examRatio != 100) {
                errors.add("平时成绩比例与考试成绩比例之和必须等于 100%。");
                ratioOk = false;
            }
        }

        List<Student> students = enrolledStudents(cno);
        Map<String, Score> existing = scoreRepository.findByCno(cno).stream()
                .collect(Collectors.toMap(Score::getSno, Function.identity(), (a, b) -> a));

        List<Score> toSave = new ArrayList<>();
        List<Score> toDelete = new ArrayList<>();

        for (Student student : students) {
            String rawUsual = trimToNull(params.get("usual_" + student.getSno()));
            String rawExam = trimToNull(params.get("exam_" + student.getSno()));
            Score old = existing.get(student.getSno());

            if (rawUsual == null && rawExam == null) {
                // 两项都空 = 未录入：清掉历史成绩，便于教师撤销误录
                if (old != null) {
                    toDelete.add(old);
                }
                continue;
            }
            if (rawUsual == null || rawExam == null) {
                errors.add(student.getSname() + "（" + student.getSno() + "）：平时成绩与考试成绩需同时填写，或都留空。");
                continue;
            }

            BigDecimal usualScore = parseScore(rawUsual);
            BigDecimal examScore = parseScore(rawExam);
            boolean ok = true;
            if (usualScore == null || !GradeCalculator.isValidScore(usualScore)) {
                errors.add(student.getSname() + "（" + student.getSno() + "）：平时成绩必须是 0~100 的数字。");
                ok = false;
            }
            if (examScore == null || !GradeCalculator.isValidScore(examScore)) {
                errors.add(student.getSname() + "（" + student.getSno() + "）：考试成绩必须是 0~100 的数字。");
                ok = false;
            }
            if (!ok || !ratioOk) {
                continue;
            }

            usualScore = usualScore.setScale(1, RoundingMode.HALF_UP);
            examScore = examScore.setScale(1, RoundingMode.HALF_UP);
            BigDecimal total = GradeCalculator.computeTotal(usualScore, examScore, usualRatio);

            if (old == null) {
                Score created = new Score(student.getSno(), cno);
                created.updateScores(usualScore, examScore, total);
                toSave.add(created);
            } else {
                old.updateScores(usualScore, examScore, total);
                toSave.add(old);
            }
        }

        if (!errors.isEmpty()) {
            // 有错就整批不落库，避免出现「一半保存」的状态
            return errors;
        }

        course.setUsualRatio(usualRatio);
        course.setExamRatio(examRatio);
        courseRepository.save(course);
        if (!toDelete.isEmpty()) {
            scoreRepository.deleteAll(toDelete);
        }
        if (!toSave.isEmpty()) {
            scoreRepository.saveAll(toSave);
        }
        return List.of();
    }

    // 学生端

    /** 学生成绩单：列出所选课程，未录入的也显示（分数为空）。 */
    public List<StudentScoreView> studentScores(String sno) {
        requireStudent(sno);
        List<Course> courses = courseRepository.findAllById(courseStudentRepository.findCnoBySno(sno)).stream()
                .sorted(Comparator.comparing(Course::getCno))
                .toList();
        Map<String, Score> scoreMap = scoreRepository.findBySno(sno).stream()
                .collect(Collectors.toMap(Score::getCno, Function.identity(), (a, b) -> a));

        List<StudentScoreView> rows = new ArrayList<>(courses.size());
        for (Course course : courses) {
            Score score = scoreMap.get(course.getCno());
            BigDecimal total = score == null ? null : score.getTotalScore();
            rows.add(new StudentScoreView(
                    course.getCno(),
                    course.getCname(),
                    course.getCredit(),
                    course.getUsualRatio(),
                    course.getExamRatio(),
                    score == null ? null : score.getUsualScore(),
                    score == null ? null : score.getExamScore(),
                    total,
                    GradeCalculator.gradePoint(total)));
        }
        return rows;
    }

    /** 汇总：已修学分（及格才算）、加权平均分与绩点。 */
    public StudentSummary summarize(List<StudentScoreView> rows) {
        BigDecimal planCredits = BigDecimal.ZERO;
        BigDecimal earnedCredits = BigDecimal.ZERO;
        List<BigDecimal> totals = new ArrayList<>();
        List<BigDecimal> credits = new ArrayList<>();
        List<BigDecimal> gradePoints = new ArrayList<>();
        int failed = 0;

        for (StudentScoreView row : rows) {
            if (row.credit() != null) {
                planCredits = planCredits.add(row.credit());
            }
            if (row.totalScore() == null) {
                continue;
            }
            totals.add(row.totalScore());
            credits.add(row.credit());
            gradePoints.add(row.gradePoint());
            if (row.totalScore().compareTo(GradeCalculator.PASS_LINE) >= 0) {
                if (row.credit() != null) {
                    earnedCredits = earnedCredits.add(row.credit());
                }
            } else {
                failed++;
            }
        }

        return new StudentSummary(
                rows.size(),
                totals.size(),
                failed,
                planCredits,
                earnedCredits,
                GradeCalculator.weightedAverage(totals, credits),
                GradeCalculator.weightedAverage(gradePoints, credits));
    }

    // 公共

    public Student requireStudent(String sno) {
        return studentRepository.findById(sno)
                .orElseThrow(() -> new BizException("未找到学号为 " + sno + " 的学生信息。"));
    }

    public String className(String classNo) {
        if (classNo == null) {
            return "";
        }
        return clazzRepository.findById(classNo)
                .map(Clazz::getClassName)
                .orElse(classNo);
    }

    /** 某门课的选课学生，按学号升序。 */
    private List<Student> enrolledStudents(String cno) {
        List<String> snos = courseStudentRepository.findSnoByCno(cno);
        return studentRepository.findAllById(snos).stream()
                .sorted(Comparator.comparing(Student::getSno))
                .toList();
    }

    /** 取课程并校验归属，防止改 URL 操作别人的课。 */
    private Course requireOwnedCourse(String cno, String teacherNo) {
        Course course = courseRepository.findById(cno)
                .orElseThrow(() -> new BizException("课程不存在：" + cno));
        if (!course.getTeacherNo().equals(teacherNo)) {
            throw new BizException("你不是该课程的任课教师，无法录入成绩。");
        }
        return course;
    }

    /** 及格率（%），无成绩时返回 null */
    private static BigDecimal passRate(List<BigDecimal> totals) {
        if (totals.isEmpty()) {
            return null;
        }
        long passed = totals.stream()
                .filter(total -> total.compareTo(GradeCalculator.PASS_LINE) >= 0)
                .count();
        return BigDecimal.valueOf(passed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totals.size()), 1, RoundingMode.HALF_UP);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Integer parseInt(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return Integer.valueOf(trimmed);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static BigDecimal parseScore(String value) {
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
