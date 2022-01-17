package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User selectById(int id);
    User selectByName(String username);
    User selectByEmail(String email);
    int insertUser(User user);
    int updateStatus(@Param("id" ) int id, @Param("status") int status);
    int updateHeader(int id, String headerUrl);
    int updatePasssword(int id, String password);
}
