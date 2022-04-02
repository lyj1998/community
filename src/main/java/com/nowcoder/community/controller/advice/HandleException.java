package com.nowcoder.community.controller.advice;



import com.nowcoder.community.util.CommunityUtil;
import com.sun.deploy.trace.LoggerTraceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;


@ControllerAdvice(annotations = Controller.class)
public class HandleException {
    private static final Logger logger = LoggerFactory.getLogger(HandleException.class);
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常"+e.getMessage());
        for (StackTraceElement element:e.getStackTrace()){
            logger.error(element.toString());
        }
        //判断是html请求还是json请求，学习这个找发
        String xRequestWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestWith)){
            //response.setContentType("application/json");//浏览器会自动转成json数据
            //浏览器会自动返回普通字符串
            response.setContentType("application/plain;charSet=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJsonString(1, "服务器异常"));
        }else{
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }

}
