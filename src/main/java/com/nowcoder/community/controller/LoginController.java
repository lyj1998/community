package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LoginService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sun.security.krb5.internal.Ticket;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant{

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private Producer kaptchaProducer;
    @Autowired
    private LoginService loginService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${server.servlet.context-path}")
    String context_path;
    //用于处理表单数据
    @RequestMapping(path="/login", method = RequestMethod.POST)
    public String Login(String username, String password, String code, boolean rememberMe,Model model,
            @CookieValue("kaptchaOwner")String kaptchaOwner, HttpServletResponse response/*HttpSession session*/){
                    //String kaptcha = (String) session.getAttribute("kaptcha");
        //从redis中取
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisUtil.getKaprchaKey(kaptchaOwner); //生成redisKey
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }
                    //检查验证码，业务层只处理业务逻辑，验证码校验可以在这里直接做
        if(StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!StringUtils.equalsIgnoreCase(kaptcha, code)){
            model.addAttribute("codeMsg", "验证码不正确");
            return "site/login";
        }
        //检查账号，密码
        int expiredSeconds = rememberMe?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> msg = loginService.login(username,password,expiredSeconds);
        if (msg.containsKey("loginTicket")){
            Cookie cookie = new Cookie("ticket", (String) msg.get("loginTicket"));
            cookie.setPath(context_path);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            //重定向到首页
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",msg.get("usernameMsg"));
            model.addAttribute("passwordMsg",msg.get("passwordMsg"));
            return "site/login";
        }
    }
    @RequestMapping(path="/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }
    @RequestMapping(path="/login", method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String , Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送一封邮件，请查收并激活账号");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            //注册失败返回注册页面
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }
    //url规定这么搞：http://localhost:8080/community/activation/101/code    #101-用户id，#code-激活码
    @RequestMapping(path="/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model,
                             @PathVariable("userId")int userId,
                             @PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if(result==ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，您的账号可以使用");
            model.addAttribute("target","/login");
        }else if(result==ACTIVATION_REPREAT){
            model.addAttribute("msg","无效操作，该账号已经激活");
            model.addAttribute("target","/login");
        }else{
            model.addAttribute("msg","激活失败，激活码不正确请重新注册");
            model.addAttribute("target","/register");
        }
        return "site/operate-result";
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);
        //将验证码存入session
//        session.setAttribute("kaptcha", text);
        //存到redis中
        String kaptchaOwner = CommunityUtil.generateUUID(); //标识谁在登陆
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner); //设置cookie
        cookie.setMaxAge(60);
        cookie.setPath(context_path);
        response.addCookie(cookie);
        String redisKey = RedisUtil.getKaprchaKey(kaptchaOwner); //生成redisKey
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);
        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "png", outputStream);
        }catch (IOException e){
            logger.error("响应验证码获取失败："+e.getMessage());
        }
    }
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        loginService.logout(ticket);
        return "redirect:/login";
    }
}
