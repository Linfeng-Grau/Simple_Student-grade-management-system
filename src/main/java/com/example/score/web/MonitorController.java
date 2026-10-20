package com.example.score.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.score.common.LoginUser;
import com.example.score.common.SessionUtil;
import com.example.score.service.RankingService;
import com.example.score.service.ScoreService;

import jakarta.servlet.http.HttpSession;

/**
 * 班长端：查看本人成绩 + 本班全体同学的成绩排名（按学分加权绩点排序）。
 */
@Controller
@RequestMapping("/monitor")
public class MonitorController {

    private final ScoreService scoreService;
    private final RankingService rankingService;

    public MonitorController(ScoreService scoreService, RankingService rankingService) {
        this.scoreService = scoreService;
        this.rankingService = rankingService;
    }

    @GetMapping("/scores")
    public String myScores(HttpSession session, Model model) {
        LoginUser me = SessionUtil.require(session);
        StudentController.fillScoreModel(scoreService, me.getRefNo(), model, "我的成绩", "my-scores");
        model.addAttribute("monitorView", Boolean.TRUE);
        return "student/scores";
    }

    @GetMapping("/ranking")
    public String ranking(HttpSession session, Model model) {
        LoginUser me = SessionUtil.require(session);
        String classNo = me.getClassNo();
        if (classNo == null) {
            classNo = scoreService.requireStudent(me.getRefNo()).getClassNo();
        }
        model.addAttribute("pageTitle", "班级成绩排名");
        model.addAttribute("activeMenu", "ranking");
        model.addAttribute("ranking", rankingService.classRanking(classNo, me.getRefNo()));
        return "monitor/ranking";
    }
}
