package com.markerhub.util;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.ml.GetFiltersRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Component
public class EsUtil {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    public void saveDoc(Map<String, Object> map) throws IOException {
        try {
            UpdateRequest req = new UpdateRequest("rec", String.valueOf(map.get("id")));
            req.doc(map).upsert(map);
            UpdateResponse response = restHighLevelClient.update(req, RequestOptions.DEFAULT);
            String id = response.getId();
            System.out.println(id);
        }catch (Exception e){

        }finally {
            restHighLevelClient.close();
        }
    }


    public List<Map<String, Object>> searchRecDoc(String text,String userId) throws IOException {
        List<Map<String, Object>> resList = new ArrayList<>();
        try { if (StringUtils.isBlank(text)){
                return resList;
            }
            SearchRequest rec = new SearchRequest("rec");
            SearchSourceBuilder ssb = new SearchSourceBuilder();
            BoolQueryBuilder builder = new BoolQueryBuilder();
            builder.must(QueryBuilders.termQuery("userId",userId))
            .must(QueryBuilders.multiMatchQuery(text,"title","conMoney","conTime","remark"));
            ssb.query(builder);
            rec.source(ssb);
            SearchResponse search = restHighLevelClient.search(rec, RequestOptions.DEFAULT);
            RestStatus status = search.status();
            SearchHits hits = search.getHits();
            SearchHit[] hits1 = hits.getHits();
            for (SearchHit doc : hits1) {
                Map<String, Object> sourceAsMap = doc.getSourceAsMap();
                resList.add(sourceAsMap);
            }
        }catch (Exception e){

        }finally {

        }return resList;
    }
}
