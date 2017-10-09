package org.skywalking.apm.collector.ui.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.skywalking.apm.collector.core.util.StringUtils;
import org.skywalking.apm.collector.storage.define.node.NodeMappingTable;
import org.skywalking.apm.collector.storage.elasticsearch.dao.EsDAO;
import org.skywalking.apm.collector.ui.cache.ApplicationCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pengys5
 */
public class NodeMappingEsDAO extends EsDAO implements INodeMappingDAO {

    private final Logger logger = LoggerFactory.getLogger(NodeMappingEsDAO.class);

    @Override public JsonArray load(long startTime, long endTime) {
        SearchRequestBuilder searchRequestBuilder = getClient().prepareSearch(NodeMappingTable.TABLE);
        searchRequestBuilder.setTypes(NodeMappingTable.TABLE_TYPE);
        searchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        searchRequestBuilder.setQuery(QueryBuilders.rangeQuery(NodeMappingTable.COLUMN_TIME_BUCKET).gte(startTime).lte(endTime));
        searchRequestBuilder.setSize(0);

        searchRequestBuilder.addAggregation(
            AggregationBuilders.terms(NodeMappingTable.COLUMN_APPLICATION_ID).field(NodeMappingTable.COLUMN_APPLICATION_ID).size(100)
                .subAggregation(AggregationBuilders.terms(NodeMappingTable.COLUMN_ADDRESS_ID).field(NodeMappingTable.COLUMN_ADDRESS_ID).size(100))
                .subAggregation(AggregationBuilders.terms(NodeMappingTable.COLUMN_ADDRESS).field(NodeMappingTable.COLUMN_ADDRESS).size(100)));
        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        Terms applicationIdTerms = searchResponse.getAggregations().get(NodeMappingTable.COLUMN_APPLICATION_ID);

        JsonArray nodeMappingArray = new JsonArray();
        for (Terms.Bucket applicationIdBucket : applicationIdTerms.getBuckets()) {
            int applicationId = applicationIdBucket.getKeyAsNumber().intValue();
            String applicationCode = ApplicationCache.getForUI(applicationId);
            Terms addressIdTerms = applicationIdBucket.getAggregations().get(NodeMappingTable.COLUMN_ADDRESS_ID);
            for (Terms.Bucket addressIdBucket : addressIdTerms.getBuckets()) {
                int addressId = addressIdBucket.getKeyAsNumber().intValue();
                String address = ApplicationCache.getForUI(addressId);

                if (addressId != 0) {
                    JsonObject nodeMappingObj = new JsonObject();
                    nodeMappingObj.addProperty("applicationCode", applicationCode);
                    nodeMappingObj.addProperty("address", address);
                    nodeMappingArray.add(nodeMappingObj);
                }
            }

            Terms addressTerms = applicationIdBucket.getAggregations().get(NodeMappingTable.COLUMN_ADDRESS);
            for (Terms.Bucket addressBucket : addressTerms.getBuckets()) {
                String address = addressBucket.getKeyAsString();

                if (StringUtils.isNotEmpty(address)) {
                    JsonObject nodeMappingObj = new JsonObject();
                    nodeMappingObj.addProperty("applicationCode", applicationCode);
                    nodeMappingObj.addProperty("address", address);
                    nodeMappingArray.add(nodeMappingObj);
                }
            }
        }
        logger.debug("node mapping data: {}", nodeMappingArray.toString());
        return nodeMappingArray;
    }
}
