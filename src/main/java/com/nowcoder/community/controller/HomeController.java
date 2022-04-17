package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DisscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.parsing.PassThroughSourceExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.print.attribute.standard.PDLOverrideSupported;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DisscussPostService disscussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private  LikeService likeService;
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    //方法调用之前SpringMVC会自动实例化Model和Page，并将Page注入到Model中
    //所以可以直接访问page对象
    //若是路径中带有参数如index?current=2 current的值会自动封装到page中
    public String getIndexPage(Model model, Page page){
        page.setRows(disscussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        List<DiscussPost> list = disscussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        System.out.println(page.getOffset());
        System.out.println(page.getLimit());
        System.out.println(list);
        List<Map<String , Object>> discussPosts = new ArrayList<>();
        if(list != null){
            for(DiscussPost discussPost: list){
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                User user = userService.findUserById(discussPost.getId());
                map.put("user", user);
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount", likeCount);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        return "index";
    }
    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }
}
