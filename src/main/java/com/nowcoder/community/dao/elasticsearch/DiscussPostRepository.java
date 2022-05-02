package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
//泛型中为<处理的实体类，实体类中主键是谁>
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer>{
}
