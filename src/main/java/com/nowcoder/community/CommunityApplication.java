package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {
	@PostConstruct
	public void init(){
		//解决netty启动冲突的问题
		//Redis底层使用了Netty，Elasticsearch也用了Netty，当注册两次就会报错
		//看Netty4Utils.setAvailabeProcessors方法
		System.setProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
