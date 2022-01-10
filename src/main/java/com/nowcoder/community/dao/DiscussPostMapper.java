package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    /**
     *
     * @param userId 考虑查看我的帖子的情况下设置动态sql，没有则默认为0
     * @param offset
     * @param limit
     * @return
     */
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);
    //如果需要动态拼接条件（<if>里使用）并且这个方法有且只有一个参数需要用@Param起别名
    //@Param用于给参数起别名
    int selectDiscussPostRows(@Param("userId") int userId);
}
