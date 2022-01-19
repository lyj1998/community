package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LoginService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    UserService userService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private HostHolder hostHolder;
    //controller之前通过cookie中拿到ticket取用户登录信息
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //从cookie中取凭证
        String ticket = CookieUtil.getValue(request,"ticket");
        if (ticket !=null){
            //查询凭证
            LoginTicket loginTicket = loginService.findLoginTicketMapper(ticket);
            //检查凭证是否有效
            if (loginTicket!=null &&loginTicket.getStatus()!=1&&loginTicket.getExpired().after(new Date())){
                //根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户 为了防止多线程之间请求互相干扰，利用ThreadLocal
                hostHolder.setUser(user);
            }
        }
        return true;
    }
//在controller之后把用户信息传递给模板
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        User user = hostHolder.getUser();
        if(user!=null && modelAndView!=null){
            modelAndView.addObject("loginUser",user);
        }
    }
//模板转发后，移除threadLocal的信息，降低内存消耗
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        hostHolder.clear();
    }
}
