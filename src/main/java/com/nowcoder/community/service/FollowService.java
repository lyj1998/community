package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant{
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    public void follow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisUtil.getFollowerKey(entityId, entityType);
                operations.multi();
                operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });

    }
    public void unfollow(int userId, int entityType, int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisUtil.getFollowerKey(entityId, entityType);
                operations.multi();
                operations.opsForZSet().remove(followeeKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().remove(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }
    //查询关注的实体的数量
    public long findFolloweeCount(int userId, int entityType){
        String followKey = RedisUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followKey);
    }
    //查询的实体的粉丝数量
    public long findFollowerCount(int entityType, int entityId){
        String followerKey = RedisUtil.getFollowerKey(entityId, entityType);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }
    //查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId, int entityType, int entityId){
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId)!=null;
    }
    //查询某个用户关注的人
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit){
        String followee = RedisUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followee, offset, offset+limit-1);
        if (targetIds== null){
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for(Integer id:targetIds){
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            //用户
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followee, id);
            //关注时间
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    //查询某个用户的粉丝
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit){
        String follower = RedisUtil.getFollowerKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(follower, offset, offset+limit-1);
        if (targetIds== null){
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for(Integer id:targetIds){
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            //用户
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(follower, id);
            //关注时间
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}
