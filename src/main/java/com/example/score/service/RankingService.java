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

import com.example.score.common.GradeCalculator;
import com.example.score.entity.Clazz;
import com.example.score.entity.Course;
import com.example.score.entity.Score;
import com.example.score.entity.Student;
import com.example.score.repository.ClazzRepository;
import com.example.score.repository.CourseRepository;
import com.example.score.repository.ScoreRepository;
import com.example.score.repository.StudentRepository;
import com.example.score.web.view.RankingRow;
import com.example.score.web.view.RankingView;

/** 班级排名：按学分加权平均绩点降序，同分比加权平均分。 */
@Service
public class RankingService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ScoreRepository scoreRepository;
    private final ClazzRepository clazzRepository;

    public RankingService(StudentRepository studentRepository,
                          CourseRepository courseRepository,
                          ScoreRepository scoreRepository,
                          ClazzRepository clazzRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.scoreRepository = scoreRepository;
        this.clazzRepository = clazzRepository;
    }

    public RankingView classRanking(String classNo, String selfSno) {
        String className = clazzRepository.findById(classNo)
                .map(Clazz::getClassName)
                .orElse(classNo);

        List<Student> students = studentRepository.findByClassNoOrderBySnoAsc(classNo);
        List<Course> courses = courseRepository.findByClassNoOrderByCnoAsc(classNo);
        Map<String, Course> courseMap = courses.stream()
                .collect(Collectors.toMap(Course::getCno, Function.identity(), (a, b) -> a));
        List<Score> scores = courses.isEmpty()
                ? List.of()
                : scoreRepository.findByCnoIn(courses.stream().map(Course::getCno).toList());
        Map<String, List<Score>> scoresBySno = scores.stream()
                .collect(Collectors.groupingBy(Score::getSno));

        List<RankingRow> drafts = new ArrayList<>(students.size());
        for (Student student : students) {
            List<Score> studentScores = scoresBySno.getOrDefault(student.getSno(), List.of());
            List<BigDecimal> totals = new ArrayList<>();
            List<BigDecimal> credits = new ArrayList<>();
            List<BigDecimal> gradePoints = new ArrayList<>();
            BigDecimal creditSum = BigDecimal.ZERO;

            for (Score score : studentScores) {
                Course course = courseMap.get(score.getCno());
                if (course == null || score.getTotalScore() == null || course.getCredit() == null) {
                    continue;
                }
                totals.add(score.getTotalScore());
                credits.add(course.getCredit());
                gradePoints.add(GradeCalculator.gradePoint(score.getTotalScore()));
                creditSum = creditSum.add(course.getCredit());
            }

            drafts.add(new RankingRow(
                    0,
                    student.getSno(),
                    student.getSname(),
                    totals.size(),
                    creditSum,
                    GradeCalculator.weightedAverage(totals, credits),
                    GradeCalculator.weightedAverage(gradePoints, credits),
                    student.getSno().equals(selfSno)));
        }

        // 有成绩的在前；绩点降序 → 加权均分降序 → 学号升序
        drafts.sort(Comparator
                .comparing((RankingRow row) -> row.gpa() != null)
                .reversed()
                .thenComparing(RankingRow::gpa, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(RankingRow::avgScore, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(RankingRow::sno));

        // 名次：同绩点同均分并列，无成绩不参与排名
        List<RankingRow> rows = new ArrayList<>(drafts.size());
        int rank = 0;
        RankingRow previous = null;
        for (RankingRow row : drafts) {
            if (row.gpa() != null) {
                boolean tie = previous != null
                        && previous.gpa() != null
                        && previous.gpa().compareTo(row.gpa()) == 0
                        && Objects.equals(previous.avgScore(), row.avgScore());
                if (!tie) {
                    rank++;
                }
            }
            rows.add(new RankingRow(
                    row.gpa() == null ? 0 : rank,
                    row.sno(),
                    row.sname(),
                    row.courseCount(),
                    row.credits(),
                    row.avgScore(),
                    row.gpa(),
                    row.self()));
            previous = row;
        }

        List<RankingRow> recorded = rows.stream().filter(row -> row.gpa() != null).toList();
        int recordedCount = recorded.size();
        int passCount = (int) recorded.stream()
                .filter(row -> row.avgScore() != null && row.avgScore().compareTo(GradeCalculator.PASS_LINE) >= 0)
                .count();
        BigDecimal avgGpa = GradeCalculator.average(recorded.stream().map(RankingRow::gpa).toList());
        BigDecimal passRate = recordedCount == 0
                ? null
                : BigDecimal.valueOf(passCount)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(recordedCount), 1, RoundingMode.HALF_UP);

        return new RankingView(classNo, className, students.size(), recordedCount, passCount,
                avgGpa, passRate, rows);
    }
}
