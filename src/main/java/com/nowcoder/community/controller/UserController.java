package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.sun.org.apache.xml.internal.resolver.helpers.PublicId;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant{
    @Autowired
    UserService userService;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${community.path.upload}")
    private String uploadPath;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;
    private Logger logger = LoggerFactory.getLogger(UserController.class);
    @LoginRequired
    @RequestMapping(path="/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }
    //把文件存到服务器中
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeaderUrl(MultipartFile multipartFile, Model model){
        if(multipartFile == null){
            model.addAttribute("error", "您没有上传任何照片");
            return "/site/setting";
        }
        String fileName = multipartFile.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        if(StringUtils.isBlank(suffix)||!(suffix.equals("png")||suffix.equals("jpg")||suffix.equals("jpeg"))){
            model.addAttribute("error","文件格式错误，请重新上传");
            return "/site/setting";
        }
        fileName = CommunityUtil.generateUUID()+"."+suffix;
        System.out.println(fileName);
        File dst = new File(uploadPath+"/"+fileName);
        try {
            multipartFile.transferTo(dst);
        } catch (IOException e) {
            logger.error("上传失败:"+e.getMessage());
            throw new RuntimeException("服务器发生失败，上传出异常"+e);
        }
        //更新headerUrl 必须是web路径
        //http://localhost:8080/community/user/header/filename
        String headerUrl = domain+contextPath+"/user/header/"+fileName;
        User user = hostHolder.getUser();
        //System.out.println(user);
        userService.uploadHeaderUrl(user.getId(), headerUrl);
        return "redirect:/user/setting";
    }
    //当用户读取headerUrl时从本地读取后返回
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getImg(@PathVariable("filename") String filename, HttpServletResponse response){
            //服务器存放地址
        filename = uploadPath+"/"+filename;
        try (    ServletOutputStream os = response.getOutputStream();
                 InputStream is = new FileInputStream(filename);){
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len=is.read(buffer))!=-1){
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
           logger.error("读取文件失败:"+e.getMessage());
        }
    }
    @LoginRequired
    @RequestMapping(path="/changePassword",method = RequestMethod.POST)
    public String changePassword(Model model,
                                 String oldPassword,
                                 String newPassword,
                                 String confirmPassword){
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("olderror","请输入原密码");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newerror","请输入新密码");
            return  "/site/setting";
        }
        if(StringUtils.isBlank(confirmPassword)){
            model.addAttribute("confirmerror","请输入确认密码");
            return "/site/setting";
        }
        if(!newPassword.equals(confirmPassword)){
            model.addAttribute("confirmerror","两次密码不一致请重新输入");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        String password = user.getPassword();
        if(!CommunityUtil.md5(oldPassword+user.getSalt()).equals(password)){
            model.addAttribute("olderror","原密码输入错误，请重新输入");
            return "/site/setting";
        }
        newPassword = CommunityUtil.md5(newPassword+user.getSalt());
        userService.updatePassword(user.getId(),newPassword);
        return "redirect:/logout";
    }
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId")int userId, Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在");
        }
        //用户
        //关注数量
        long followeeCount = followService.findFolloweeCount(user.getId(), ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, user.getId());
        model.addAttribute("followerCount", followerCount);
        System.out.println(user.getId());
        //当前用户是否关注
        if(hostHolder.getUser()!=null){
            boolean hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
            model.addAttribute("hasFollowed", hasFollowed);
        }

        model.addAttribute("user", user);
        int likeCount = likeService.findUserLikeCount(user.getId());
        model.addAttribute("likeCount", likeCount);
        return "/site/profile";
    }
}
