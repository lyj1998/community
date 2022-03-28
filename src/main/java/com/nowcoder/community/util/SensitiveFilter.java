package com.nowcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);
    //要替换的符号
    private static final  String REPLACEMENT = "***";
    //根节点
    private TireNode rootNode = new TireNode();
    @PostConstruct  //服务启动初始化bean时构造器之后执行
    public void init(){
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String keyword;
            while ((keyword = br.readLine())!=null){
                //添加到前缀树
                this.addKeyWord(keyword);
            }
        }catch (Exception e){
            logger.error("加载敏感词文件失败:"+e.getMessage());
        }
    }
    //将一个敏感词添加到前缀树
    private void addKeyWord(String keyword){
        if (StringUtils.isBlank(keyword)) return;
        char[] arr = keyword.toCharArray();
        TireNode tmpNode = rootNode;
        for (int i = 0; i<arr.length; i++){
            TireNode subNode = tmpNode.getSubNode(arr[i]);
            if(subNode == null){
                //初始化子节点
                subNode = new TireNode();
                tmpNode.addSubNode(arr[i], subNode);
            }
            tmpNode = subNode;
        }
        tmpNode.setKeywordEnd(true);
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的词
     * @return
     */
    public String filter(String text){
        if (StringUtils.isBlank(text)) return null;
        //指针1 指向树
        TireNode tmpNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();
        while (position<text.length()){
            char c = text.charAt(position);
            //跳过符号
            if (isSysbol(c)){
                //若指针1处于根节点，将此符号计入结果，让指针2向下走一步
                if (tmpNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论符号在开头或者中间指针3都向下走一步
                position++;
                continue;
            }
            //检查下级节点
            tmpNode = tmpNode.getSubNode(c);
            if(tmpNode == null){
                //以begin为开头的字符不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个词的判断
                position = ++begin;
                tmpNode = rootNode;
            }else if (tmpNode.isKeywordEnd()){
                //发现敏感词以begin开头，position结尾的词
                sb.append(REPLACEMENT);
                begin = ++position;
                tmpNode = rootNode;
            } else {
                //继续检查下一个字符
                position++;
            }
        }
        //将最后一批字符记录
        sb.append(text.substring(begin));
        return sb.toString();
    }
    //判断是否为符号
    private boolean isSysbol(Character c){
        //c<0x2E80 || c>0x9FFF 东亚文字之外
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80||c>0x9FFF);
    }
    //定义前缀树
    private class TireNode{
        //关键词结束的标识
        private boolean isKeywordEnd = false;
        //子节点(key是下级字符，value是下级节点)
        private Map<Character, TireNode> subNodes = new HashMap<>();
        //添加子节点
        public void addSubNode(Character c, TireNode node){
            subNodes.put(c, node);
        }
        //获取子节点
        public TireNode getSubNode(Character c){
            return subNodes.get(c);
        }

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
    }
}
