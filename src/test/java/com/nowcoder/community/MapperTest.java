package com.nowcoder.community;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private MessageMapper messageMapper;
    @Test
    public  void  InsertTest(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }
    @Test
    public void testSelectTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testUpdateLoginticket(){
        loginTicketMapper.updateStatus("abc",1);
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

    }
    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);
    }
    @Test
    public void testSelectEmail(){
        User user = userMapper.selectByEmail("3301056761@qq.com");
        System.out.println(user);
    }

    @Test
    public void testselectCommentsByEntity(){
        List<Comment> comment = commentMapper.selectCommentsByEntity(1, 228, 0, 2);
        System.out.println(comment);
    }

    @Test
    public void testselectConversation(){
        List<Message> messages = messageMapper.selectConversation(111, 0, 20);
        for (Message message: messages){
            System.out.println(message);
        }
    }

    @Test
    public void testselectConversationCount(){
        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);
    }

    @Test
    public void testselectLetter(){
        List<Message> messages = messageMapper.selectLetter("111_112", 0, 20);
        for (Message message: messages){
            System.out.println(message);
        }
        int count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);
        int unreadCount = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(unreadCount);

    }
}


