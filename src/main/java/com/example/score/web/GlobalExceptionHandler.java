package com.example.score.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.example.score.common.BizException;
import com.example.score.common.SessionUtil;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** 统一异常处理：把异常转成友好错误页，而不是白页 500。 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ModelAndView handleBiz(BizException ex, HttpSession session, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return errorView("操作未完成", ex.getMessage(), session);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNotFound(NoResourceFoundException ex, HttpSession session,
                                       HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return errorView("页面不存在", "你访问的页面不存在，可能链接已经失效。", session);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleOther(Exception ex, HttpSession session, HttpServletResponse response) {
        log.error("请求处理失败", ex);
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return errorView("服务器开小差了", "处理请求时发生异常：" + ex.getMessage(), session);
    }

    private ModelAndView errorView(String title, String message, HttpSession session) {
        ModelAndView mav = new ModelAndView("error/biz");
        mav.addObject("errorTitle", title);
        mav.addObject("errorMessage", message);
        mav.addObject("me", SessionUtil.get(session));
        return mav;
    }
}
