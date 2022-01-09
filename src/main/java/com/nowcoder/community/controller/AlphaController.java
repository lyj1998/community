package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @Autowired
    private AlphaService alphaService;

    @ResponseBody
    @RequestMapping("/hello")
    public String helloAlpha(){
        return "Hello SpringBoot.";
    }

    @ResponseBody
    @RequestMapping("/data")
    public String getData(){
        return alphaService.find();
    }

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response){
        //获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()){
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name+":"+value);
        }
        //请求体
        System.out.println(request.getParameter("code"));
        //返回响应数据
        response.setContentType("text/html;charset=utf-8");
        try ( PrintWriter writer = response.getWriter()){
                writer.write("<h1>牛客网</h1>");
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    // /testRequestParam?i=1&j=100
    //requestParam 经过DispatcherServlet处理后会从request对象中获取参数
    @RequestMapping(path="/testRequestParam", method = RequestMethod.GET)
    @ResponseBody
    public String testRequestParam(
            @RequestParam(name="i", required = false, defaultValue = "1") int i,
            @RequestParam(name = "j", required = false, defaultValue = "100") int j){
        System.out.println(i);
        System.out.println(j);
        return "hello world";
    }

    // @PathVaraiable("xxx") 快速获取路径上的值如上所示
    @RequestMapping(path="/testPathVariable/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String testPathVariable(@PathVariable("id") int id){
        System.out.println(id);
        return "hello world";
    }

    //POST请求
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    //填充模板数据
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView testThymeleaf(){
        ModelAndView mv = new ModelAndView();
        mv.addObject("name", "狂徒张三");
        mv.addObject("age", 100);
        mv.setViewName("/demo/teacher");
        return mv;
    }

    //填充模板数据第二种
    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model mv){
        mv.addAttribute("name","狂徒张三");
        mv.addAttribute("age", 100);
        return "/demo/teacher";
    }

    //响应json数据（异步请求）
    //Java对象->Json字符串
    @RequestMapping(path="/testJson", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> testJson(){
        Map<String, Object> map = new HashMap<>();
        map.put("name", "猪猪侠");
        map.put("age", 19);
        return map;
    }







}
