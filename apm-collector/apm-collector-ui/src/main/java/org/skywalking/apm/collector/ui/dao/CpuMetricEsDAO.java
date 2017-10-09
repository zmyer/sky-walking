package org.skywalking.apm.collector.ui.dao;

import com.google.gson.JsonArray;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.skywalking.apm.collector.core.util.Const;
import org.skywalking.apm.collector.core.util.TimeBucketUtils;
import org.skywalking.apm.collector.storage.define.jvm.CpuMetricTable;
import org.skywalking.apm.collector.storage.elasticsearch.dao.EsDAO;

/**
 * @author pengys5
 */
public class CpuMetricEsDAO extends EsDAO implements ICpuMetricDAO {

    @Override public int getMetric(int instanceId, long timeBucket) {
        String id = timeBucket + Const.ID_SPLIT + instanceId;
        GetResponse getResponse = getClient().prepareGet(CpuMetricTable.TABLE, id).get();

        if (getResponse.isExists()) {
            return ((Number)getResponse.getSource().get(CpuMetricTable.COLUMN_USAGE_PERCENT)).intValue();
        }
        return 0;
    }

    @Override public JsonArray getMetric(int instanceId, long startTimeBucket, long endTimeBucket) {
        MultiGetRequestBuilder prepareMultiGet = getClient().prepareMultiGet();

        long timeBucket = startTimeBucket;
        do {
            timeBucket = TimeBucketUtils.INSTANCE.addSecondForSecondTimeBucket(TimeBucketUtils.TimeBucketType.SECOND.name(), timeBucket, 1);
            String id = timeBucket + Const.ID_SPLIT + instanceId;
            prepareMultiGet.add(CpuMetricTable.TABLE, CpuMetricTable.TABLE_TYPE, id);
        }
        while (timeBucket <= endTimeBucket);

        JsonArray metrics = new JsonArray();
        MultiGetResponse multiGetResponse = prepareMultiGet.get();
        for (MultiGetItemResponse response : multiGetResponse.getResponses()) {
            if (response.getResponse().isExists()) {
                double cpuUsed = ((Number)response.getResponse().getSource().get(CpuMetricTable.COLUMN_USAGE_PERCENT)).doubleValue();
                metrics.add((int)(cpuUsed * 100));
            } else {
                metrics.add(0);
            }
        }
        return metrics;
    }
}
