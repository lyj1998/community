package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    //测试发送普通邮件
    @Test
    public void testMail(){
        mailClient.sendMail("3301056761@qq.com", "Test", "hello world");
    }
    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","lyj1998");
        String html = templateEngine.process("mail/demo", context);
        //System.out.println(html);
        mailClient.sendMail("3301056761@qq.com","TestHtml",html);
    }
}
