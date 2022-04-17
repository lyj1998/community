package com.nowcoder.community.util;

public class RedisUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPRCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_User = "user";

    //某个实体的赞
    //like:entity:entityType:entityId -->set(userId)由方法参数传来的参数组成的key值
    //value是个set:存的是用户id，为了统计谁给我点了赞
    public static  String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }
    //某个用户的赞
    //key-->like:user:userId -->int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

    //某个用户关注的实体
    //followee:userId:entityType ->zset(entityId, now) 以当前时间作为分数
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }
    //某个实体拥有的粉丝
    //follower：entityType：entityId->zset(userId, now)以当前时间作为分数
    public static String getFollowerKey(int entityId, int entityType){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }
    //owener标识是哪个用户正在登录，使用redis前会向cookie中传入一个标识
    public static String getKaprchaKey(String owner){
        return PREFIX_KAPRCHA+SPLIT+owner;
    }
    //登录的凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET+SPLIT+ticket;
    }
    //用户
    public static String getUserKey(int userId){
        return PREFIX_User+SPLIT+userId;
    }
}
