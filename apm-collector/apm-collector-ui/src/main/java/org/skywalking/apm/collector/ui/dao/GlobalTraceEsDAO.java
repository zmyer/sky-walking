package org.skywalking.apm.collector.ui.dao;

import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.skywalking.apm.collector.storage.elasticsearch.dao.EsDAO;
import org.skywalking.apm.collector.storage.define.global.GlobalTraceTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pengys5
 */
public class GlobalTraceEsDAO extends EsDAO implements IGlobalTraceDAO {

    private final Logger logger = LoggerFactory.getLogger(GlobalTraceEsDAO.class);

    @Override public List<String> getGlobalTraceId(String segmentId) {
        SearchRequestBuilder searchRequestBuilder = getClient().prepareSearch(GlobalTraceTable.TABLE);
        searchRequestBuilder.setTypes(GlobalTraceTable.TABLE_TYPE);
        searchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequestBuilder.setQuery(QueryBuilders.termQuery(GlobalTraceTable.COLUMN_SEGMENT_ID, segmentId));
        searchRequestBuilder.setSize(10);

        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        List<String> globalTraceIds = new ArrayList<>();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            String globalTraceId = (String)searchHit.getSource().get(GlobalTraceTable.COLUMN_GLOBAL_TRACE_ID);
            logger.debug("segmentId: {}, global trace id: {}", segmentId, globalTraceId);
            globalTraceIds.add(globalTraceId);
        }
        return globalTraceIds;
    }

    @Override public List<String> getSegmentIds(String globalTraceId) {
        SearchRequestBuilder searchRequestBuilder = getClient().prepareSearch(GlobalTraceTable.TABLE);
        searchRequestBuilder.setTypes(GlobalTraceTable.TABLE_TYPE);
        searchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequestBuilder.setQuery(QueryBuilders.termQuery(GlobalTraceTable.COLUMN_GLOBAL_TRACE_ID, globalTraceId));
        searchRequestBuilder.setSize(10);

        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        List<String> segmentIds = new ArrayList<>();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            String segmentId = (String)searchHit.getSource().get(GlobalTraceTable.COLUMN_SEGMENT_ID);
            logger.debug("segmentId: {}, global trace id: {}", segmentId, globalTraceId);
            segmentIds.add(segmentId);
        }
        return segmentIds;
    }
}
