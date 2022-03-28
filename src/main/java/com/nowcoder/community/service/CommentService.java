package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private DisscussPostService disscussPostService;
    @Autowired
    private SensitiveFilter sensitiveFilter;
    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }
    public int findCommentsCount(int entityType, int entityId){
        return commentMapper.selectCountByEntity(entityType, entityId);
    }
    public int addComment(Comment comment){
        if (comment == null){
            throw new IllegalArgumentException("评论不能为空");
        }
        //处理内容
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        //添加评论
        int rows = commentMapper.insertComment(comment);
        //如果是给帖子回复
        if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(CommunityConstant.ENTITY_TYPE_POST, comment.getEntityId());
            disscussPostService.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }
}
