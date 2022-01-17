package com.nowcoder.community.util;


import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    //生成随机字符串
    public static String generateUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }
    //MD5加密
    //MD5只能加密不能解密，原串加salt拼接新串加密防破解
    public static String md5(String key){
        //null，空串 ，空格都会判空
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
