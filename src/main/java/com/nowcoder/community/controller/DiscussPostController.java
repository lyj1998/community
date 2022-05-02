package com.nowcoder.community.controller;

import com.nowcoder.community.entity.*;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DisscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_COMMENT;
import static com.nowcoder.community.util.CommunityConstant.ENTITY_TYPE_POST;
import static com.nowcoder.community.util.CommunityConstant.TOPIC_PUBLISH;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {
    @Autowired
    private DisscussPostService disscussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if (user == null){
            //返回Json数据
            return CommunityUtil.getJsonString(403, "您还没有登录!");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        disscussPostService.addDiscussPost(post);
        //发帖异步处理为了在es中增加帖子
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);
        //报错的情况统一处理
        return CommunityUtil.getJsonString(0, "发布成功");
    }
    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId")int discussPostId, Model model, Page page){
        //查询这个帖子
        DiscussPost post = disscussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",post);
        //根据userId查名字
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //帖子点赞状况
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        //点赞状态
        int likeStatus = hostHolder.getUser()==null?
                0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStauts", likeStatus);
        //查评论的分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/"+discussPostId);
        page.setRows(post.getCommentCount());//帖子相关字段中冗余了一个commentcount
        //帖子的评论：称为--评论
        //评论的评论：称为--回复
        //评论列表
        List<Comment> comments = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(comments!=null){
            for(Comment c:comments){
                Map<String, Object> commentVo = new HashMap<>();
                //放评论
                commentVo.put("comment", c);
                //放作者
                commentVo.put("user", userService.findUserById(c.getUserId()));
                 likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, c.getId());
                commentVo.put("likeCount", likeCount);
                //点赞状态
                 likeStatus = hostHolder.getUser()==null?
                        0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, c.getId());
                commentVo.put("likeStatus", likeStatus);
                //回复列表
                List<Comment> replys = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, c.getId(), 0, Integer.MAX_VALUE);
                //回复的Vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replys!=null){
                    for (Comment r: replys){
                        Map<String, Object> replyVo = new HashMap<>();
                        //放回复
                        replyVo.put("reply", r);
                        //放回复者user
                        replyVo.put("user", userService.findUserById(r.getUserId()));
                         likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, r.getId());
                        replyVo.put("likeCount", likeCount);
                        //点赞状态
                         likeStatus = hostHolder.getUser()==null?
                                0:likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, r.getId());
                        replyVo.put("likeStatus", likeStatus);
                        //放被回复者，如果有的话
                        User target = r.getTargetId()==0?null:userService.findUserById(r.getTargetId());
                        replyVo.put("target", target);
                        replyVoList.add(replyVo);
                    }
                }
                //回复加入进来
                commentVo.put("replys", replyVoList);
                //一条评论回复的数量
                int replyCount = commentService.findCommentsCount(ENTITY_TYPE_COMMENT, c.getId());
                commentVo.put("replyCount", replyCount);
                //加入评论vo
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        return "site/discuss-detail";
    }


}
