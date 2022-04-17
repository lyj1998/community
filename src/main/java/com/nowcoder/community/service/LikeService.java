package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;
    //点赞
    public void like(int userId, int entityType, int entityId, int entityUserId){
        //第一次点是点赞，第二次是取消赞
        //先判断是否点过赞，value是存userId的set集合
        /*
        String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (isMember){
            //说明点过赞，这次是取消赞
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        }else {
            //说明是第一次点赞
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }*/
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisUtil.getUserLikeKey(entityUserId);
                //判断当前用户有没有点赞，这一步应该在事务开启前执行，因为在事务中的查询不会立即得到结果
                Boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);
                //事务开启
                operations.multi();
                if(isMember){
                    //说明点过赞，这次是取消赞
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    //被点赞的用户点赞数量减一
                    redisTemplate.opsForValue().decrement(userLikeKey);
                }else{
                    //说明是第一次点赞
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    //被点赞的用户点赞量加一
                    redisTemplate.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });
    }

    //查询实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }
    //查询某人对某实体的点赞状态，返回int为了以后业务扩展，比如点了踩就是-1
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        int status = redisTemplate.opsForSet().isMember(entityLikeKey, userId)?1:0;
        return status;
    }
    //查询某个用户获得赞的数量
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count==null?0:count.intValue();
    }
}
