package com.markerhub;

import com.markerhub.entity.Records;
import com.markerhub.entity.User;
import com.markerhub.service.UserService;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MyTest {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RestHighLevelClient restHighLevelClient;
    @Autowired
    UserService userService;
    @Test
    public void conRedis(){
        Records records = new Records();
        records.setUserId("12");
        redisTemplate.opsForValue().set("aa",records);
        Object aa = redisTemplate.opsForValue().get("aa");
        System.out.println(aa);
    }
    @Test
    public void createIndex() throws IOException {

        CreateIndexRequest req = new CreateIndexRequest("rec");
        req.settings(Settings.builder()
                .put("analysis.analyzer.default.tokenizer","ik_max_word")
                .put("index.number_of_shards",3)
                .put("index.number_of_replicas", 2));
        req.mapping(
                "{\n" + "\"properties\":{\n" +
                        "        \"id\":{\n" +
                        "            \"type\":\"long\",\n" +
                        "            \"store\":true\n" +
                        "        },\n" +
                        "        \"userId\":{\n" +
                        "            \"type\":\"long\",\n" +
                        "            \"store\":true\n" +
                        "        },\n" +
                        "        \"conType\":{\n" +
                        "            \"type\":\"keyword\",\n" +
                        "            \"store\":true\n" +
                        "        },\n" +
                        "        \"title\":{\n" +
                        "            \"type\":\"text\",\n" +
                        "            \"store\":true,\n" +
                        "            \"analyzer\":\"ik_max_word\"\n" +
                        "        },\n" +
                        "        \"conMoney\":{\n" +
                        "            \"type\":\"text\",\n" +
                        "            \"store\":true,\n" +
                        "            \"analyzer\":\"ik_max_word\"\n" +
                        "        },\n" +
                        "        \"conTime\":{\n" +
                        "            \"type\":\"text\",\n" +
                        "            \"store\":true,\n" +
                        "            \"analyzer\":\"ik_max_word\"\n" +
                        "        },\n" +
                        "        \"remark\":{\n" +
                        "            \"type\":\"text\",\n" +
                        "            \"store\":true,\n" +
                        "            \"analyzer\":\"ik_max_word\"\n" +
                        "        },\n" +
                        "        \"crtTime\":{\n" +
                        "            \"type\":\"keyword\",\n" +
                        "            \"store\":true\n" +
                        "        },\n" +
                        "        \"updTime\":{\n" +
                        "            \"type\":\"keyword\",\n" +
                        "            \"store\":true\n" +
                        "        }\n" +
                        "    }\n" +
                        "}",
                XContentType.JSON);

        CreateIndexResponse response = restHighLevelClient.indices().create(req, RequestOptions.DEFAULT);
        boolean acknowledged = response.isAcknowledged();
        boolean shardsAcknowledged = response.isShardsAcknowledged();
        System.out.println(acknowledged);
        System.out.println(shardsAcknowledged);
    }

    @Test
    public void delIndex() throws IOException {
        DeleteIndexRequest req = new DeleteIndexRequest("rec");
        AcknowledgedResponse response = restHighLevelClient.indices().delete(req, RequestOptions.DEFAULT);
        boolean acknowledged = response.isAcknowledged();
        System.out.println(acknowledged);

    }
    @Test
    public void saveDoc() throws IOException {
        Map<String, Object> map = new HashMap<>();
        IndexRequest request = new IndexRequest("rec");
        map.put("id",223);
        request.source(map);
        IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        String id = response.getId();
        System.out.println(id);
    }

    @Test
    public void searchDoc() throws IOException {
        SearchRequest rec = new SearchRequest("rec");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.multiMatchQuery("看","title","conMoney","conTime","remark"));
        rec.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(rec, RequestOptions.DEFAULT);
        RestStatus status = search.status();
        System.out.println(status);
        SearchHits hits = search.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit doc : hits1) {
            String id = doc.getId();
            Map<String, Object> sourceAsMap = doc.getSourceAsMap();

            System.out.println(sourceAsMap);
        }
    }

    @Test
    public void saveUser() throws IOException {
        User user = new User();
        user.setUsername("qq");
        user.setPassword("123");
        userService.save(user);
    }
}
