package org.skywalking.apm.collector.ui.dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.skywalking.apm.collector.core.util.Const;
import org.skywalking.apm.collector.core.util.TimeBucketUtils;
import org.skywalking.apm.collector.storage.define.jvm.MemoryPoolMetricTable;
import org.skywalking.apm.collector.storage.elasticsearch.dao.EsDAO;

/**
 * @author pengys5
 */
public class MemoryPoolMetricEsDAO extends EsDAO implements IMemoryPoolMetricDAO {

    @Override public JsonObject getMetric(int instanceId, long timeBucket, int poolType) {
        String id = timeBucket + Const.ID_SPLIT + instanceId + Const.ID_SPLIT + poolType;
        GetResponse getResponse = getClient().prepareGet(MemoryPoolMetricTable.TABLE, id).get();

        JsonObject metric = new JsonObject();
        if (getResponse.isExists()) {
            metric.addProperty("max", ((Number)getResponse.getSource().get(MemoryPoolMetricTable.COLUMN_MAX)).intValue());
            metric.addProperty("init", ((Number)getResponse.getSource().get(MemoryPoolMetricTable.COLUMN_INIT)).intValue());
            metric.addProperty("used", ((Number)getResponse.getSource().get(MemoryPoolMetricTable.COLUMN_USED)).intValue());
        } else {
            metric.addProperty("max", 0);
            metric.addProperty("init", 0);
            metric.addProperty("used", 0);
        }
        return metric;
    }

    @Override public JsonObject getMetric(int instanceId, long startTimeBucket, long endTimeBucket, int poolType) {
        MultiGetRequestBuilder prepareMultiGet = getClient().prepareMultiGet();

        long timeBucket = startTimeBucket;
        do {
            timeBucket = TimeBucketUtils.INSTANCE.addSecondForSecondTimeBucket(TimeBucketUtils.TimeBucketType.SECOND.name(), timeBucket, 1);
            String id = timeBucket + Const.ID_SPLIT + instanceId + Const.ID_SPLIT + poolType;
            prepareMultiGet.add(MemoryPoolMetricTable.TABLE, MemoryPoolMetricTable.TABLE_TYPE, id);
        }
        while (timeBucket <= endTimeBucket);

        JsonObject metric = new JsonObject();
        JsonArray usedMetric = new JsonArray();
        MultiGetResponse multiGetResponse = prepareMultiGet.get();
        for (MultiGetItemResponse response : multiGetResponse.getResponses()) {
            if (response.getResponse().isExists()) {
                metric.addProperty("max", ((Number)response.getResponse().getSource().get(MemoryPoolMetricTable.COLUMN_MAX)).longValue());
                metric.addProperty("init", ((Number)response.getResponse().getSource().get(MemoryPoolMetricTable.COLUMN_INIT)).longValue());
                usedMetric.add(((Number)response.getResponse().getSource().get(MemoryPoolMetricTable.COLUMN_USED)).longValue());
            } else {
                metric.addProperty("max", 0);
                metric.addProperty("init", 0);
                usedMetric.add(0);
            }
        }
        metric.add("used", usedMetric);
        return metric;
    }
}
