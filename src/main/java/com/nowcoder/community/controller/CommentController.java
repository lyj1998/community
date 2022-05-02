package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DisscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant{
    @Autowired
    private CommentService commmentService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private DisscussPostService disscussPostService;
    @Autowired
    private EventProducer eventProducer;
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commmentService.addComment(comment);
        //触发评论事件
        Event event = new Event();
        event.setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postID", discussPostId);
        //为了设置EntityUserId，需要进行查询
        if (comment.getEntityType()==ENTITY_TYPE_POST){
            DiscussPost targetPost = disscussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(targetPost.getUserId());
        }else if (comment.getEntityType()==ENTITY_TYPE_COMMENT){
            Comment commentById = commmentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(commentById.getUserId());
        }
        //如果是给帖子评论
        if (comment.getEntityType() == ENTITY_TYPE_POST){
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }
        eventProducer.fireEvent(event);
        return "redirect:/discuss/detail/"+discussPostId;
    }
}
