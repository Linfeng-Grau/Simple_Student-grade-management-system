package com.example.score.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.score.common.LoginUser;
import com.example.score.common.SessionUtil;
import com.example.score.entity.Student;
import com.example.score.service.ScoreService;
import com.example.score.web.view.StudentScoreView;

import jakarta.servlet.http.HttpSession;

/** 学生端：只能查看自己的成绩。 */
@Controller
@RequestMapping("/student")
public class StudentController {

    private final ScoreService scoreService;

    public StudentController(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @GetMapping("/scores")
    public String scores(HttpSession session, Model model) {
        LoginUser me = SessionUtil.require(session);
        fillScoreModel(scoreService, me.getRefNo(), model, "我的成绩", "my-scores");
        return "student/scores";
    }

    /** 成绩页数据装配，班长端「我的成绩」复用。 */
    static void fillScoreModel(ScoreService scoreService, String sno, Model model,
                               String pageTitle, String activeMenu) {
        Student student = scoreService.requireStudent(sno);
        List<StudentScoreView> rows = scoreService.studentScores(sno);

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("student", student);
        model.addAttribute("className", scoreService.className(student.getClassNo()));
        model.addAttribute("rows", rows);
        model.addAttribute("summary", scoreService.summarize(rows));
    }
}
