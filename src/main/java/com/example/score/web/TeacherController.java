package com.example.score.web;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.score.common.LoginUser;
import com.example.score.common.SessionUtil;
import com.example.score.service.ScoreService;
import com.example.score.service.StudentAdminService;
import com.example.score.web.view.CourseEntryPage;
import com.example.score.web.view.EnrollResult;
import com.example.score.web.view.RegisterResult;

import jakarta.servlet.http.HttpSession;

/**
 * 教师端：我的课程 → 按课程录入成绩；学生管理 → 按课程添加/移出学生、设置班长。
 */
@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final ScoreService scoreService;
    private final StudentAdminService studentAdminService;

    public TeacherController(ScoreService scoreService, StudentAdminService studentAdminService) {
        this.scoreService = scoreService;
        this.studentAdminService = studentAdminService;
    }

    @GetMapping("/courses")
    public String courses(HttpSession session, Model model) {
        LoginUser me = SessionUtil.require(session);
        model.addAttribute("pageTitle", "我的课程");
        model.addAttribute("activeMenu", "courses");
        model.addAttribute("courses", scoreService.teacherCourses(me.getRefNo()));
        return "teacher/courses";
    }

    @GetMapping("/course/{cno}")
    public String entryPage(@PathVariable String cno,
                            @RequestParam(required = false) String saved,
                            HttpSession session,
                            Model model) {
        LoginUser me = SessionUtil.require(session);
        CourseEntryPage page = scoreService.teacherEntryPage(cno, me.getRefNo());
        model.addAttribute("pageTitle", page.cname());
        model.addAttribute("activeMenu", "courses");
        model.addAttribute("page", page);
        model.addAttribute("saved", saved != null);
        return "teacher/scores";
    }

    @PostMapping("/course/{cno}")
    public String save(@PathVariable String cno,
                       @RequestParam(name = "usualRatio", required = false) String usualRatio,
                       @RequestParam(name = "examRatio", required = false) String examRatio,
                       @RequestParam Map<String, String> params,
                       HttpSession session,
                       Model model) {
        LoginUser me = SessionUtil.require(session);
        List<String> errors = scoreService.saveScores(cno, me.getRefNo(), usualRatio, examRatio, params);
        if (errors.isEmpty()) {
            return "redirect:/teacher/course/" + cno + "?saved=1";
        }

        // 校验失败：回显教师刚填的内容，避免重新输入
        CourseEntryPage page = scoreService.teacherEntryPage(cno, me.getRefNo());
        model.addAttribute("pageTitle", page.cname());
        model.addAttribute("activeMenu", "courses");
        model.addAttribute("page", page);
        model.addAttribute("errors", errors);
        model.addAttribute("rawScores", params);
        model.addAttribute("usualRatioRaw", usualRatio);
        model.addAttribute("examRatioRaw", examRatio);
        model.addAttribute("saved", false);
        return "teacher/scores";
    }

    // 学生管理

    @GetMapping("/students")
    public String students(@RequestParam(required = false) String registered,
                           @RequestParam(required = false) String duplicateName,
                           @RequestParam(required = false) String enrolled,
                           @RequestParam(name = "skipped", required = false) Integer skipped,
                           @RequestParam(name = "dup", required = false) Integer dup,
                           @RequestParam(required = false) String removed,
                           @RequestParam(required = false) String monitor,
                           HttpSession session,
                           Model model) {
        LoginUser me = SessionUtil.require(session);
        fillStudentsModel(me, model);
        model.addAttribute("registered", registered != null);
        model.addAttribute("duplicateName", duplicateName != null);
        model.addAttribute("enrolledCount", parseCount(enrolled));
        model.addAttribute("skippedCount", skipped == null ? 0 : skipped);
        model.addAttribute("dupCount", dup == null ? 0 : dup);
        model.addAttribute("removed", removed != null);
        model.addAttribute("monitorChanged", monitor != null);
        return "teacher/students";
    }

    /** 登记新学生（只入库、不加课），这是新增学生信息的唯一入口。 */
    @PostMapping("/students/register")
    public String registerStudent(@RequestParam String name,
                                  @RequestParam String sno,
                                  @RequestParam String classNo,
                                  HttpSession session,
                                  Model model) {
        LoginUser me = SessionUtil.require(session);
        RegisterResult result = studentAdminService.registerStudent(name, sno, classNo);
        if (result.errors().isEmpty()) {
            return "redirect:/teacher/students?registered=1"
                    + (result.duplicateName() ? "&duplicateName=1" : "");
        }
        fillStudentsModel(me, model);
        model.addAttribute("errors", result.errors());
        model.addAttribute("regName", name);
        model.addAttribute("regSno", sno);
        model.addAttribute("regClassNo", classNo);
        return "teacher/students";
    }

    /** 把下拉框勾选的已登记学生加入课程（支持单选 / 多选）。 */
    @PostMapping("/students/add")
    public String addStudents(@RequestParam(name = "snos", required = false) List<String> snos,
                              @RequestParam String target,
                              HttpSession session,
                              Model model) {
        LoginUser me = SessionUtil.require(session);
        EnrollResult result = studentAdminService.addStudents(me.getRefNo(), snos, target);
        if (result.errors().isEmpty()) {
            return "redirect:/teacher/students?enrolled=" + result.added()
                    + "&skipped=" + result.skippedOtherClass()
                    + "&dup=" + result.alreadyEnrolled();
        }
        fillStudentsModel(me, model);
        model.addAttribute("errors", result.errors());
        model.addAttribute("formTarget", target);
        return "teacher/students";
    }

    /** 课程卡片上的「加入本课」，学号来自本班已登记列表。 */
    @PostMapping("/students/enroll")
    public String enrollIntoCourse(@RequestParam String cno,
                                   @RequestParam String sno,
                                   HttpSession session,
                                   Model model) {
        LoginUser me = SessionUtil.require(session);
        EnrollResult result = studentAdminService.enrollIntoCourse(me.getRefNo(), cno, sno);
        if (result.errors().isEmpty()) {
            return "redirect:/teacher/students?enrolled=" + result.added();
        }
        fillStudentsModel(me, model);
        model.addAttribute("errors", result.errors());
        return "teacher/students";
    }

    /** 只把学生从这一门课移出，账号与其它课程不受影响。 */
    @PostMapping("/students/remove")
    public String removeStudent(@RequestParam String cno,
                                @RequestParam String sno,
                                HttpSession session) {
        LoginUser me = SessionUtil.require(session);
        studentAdminService.removeStudent(me.getRefNo(), cno, sno);
        return "redirect:/teacher/students?removed=1";
    }

    /** 设为班长 / 取消班长（取消后该班可以没有班长）。 */
    @PostMapping("/students/monitor")
    public String changeMonitor(@RequestParam String sno,
                                @RequestParam String action,
                                HttpSession session) {
        LoginUser me = SessionUtil.require(session);
        if ("clear".equals(action)) {
            studentAdminService.clearMonitor(me.getRefNo(), sno);
        } else {
            studentAdminService.setMonitor(me.getRefNo(), sno);
        }
        return "redirect:/teacher/students?monitor=1";
    }

    private void fillStudentsModel(LoginUser me, Model model) {
        model.addAttribute("pageTitle", "学生管理");
        model.addAttribute("activeMenu", "students");
        model.addAttribute("courses", studentAdminService.coursesWithStudents(me.getRefNo()));
        model.addAttribute("classes", studentAdminService.listClasses());
        model.addAttribute("pickable", studentAdminService.pickableStudents(me.getRefNo()));
    }

    private int parseCount(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}
