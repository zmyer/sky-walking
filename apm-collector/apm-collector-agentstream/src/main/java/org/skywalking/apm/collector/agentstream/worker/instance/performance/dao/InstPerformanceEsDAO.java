package org.skywalking.apm.collector.agentstream.worker.instance.performance.dao;

import java.util.HashMap;
import java.util.Map;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.skywalking.apm.collector.core.stream.Data;
import org.skywalking.apm.collector.storage.define.DataDefine;
import org.skywalking.apm.collector.storage.define.instance.InstPerformanceTable;
import org.skywalking.apm.collector.storage.elasticsearch.dao.EsDAO;
import org.skywalking.apm.collector.stream.worker.impl.dao.IPersistenceDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pengys5
 */
public class InstPerformanceEsDAO extends EsDAO implements IInstPerformanceDAO, IPersistenceDAO<IndexRequestBuilder, UpdateRequestBuilder> {

    private final Logger logger = LoggerFactory.getLogger(InstPerformanceEsDAO.class);

    @Override public Data get(String id, DataDefine dataDefine) {
        GetResponse getResponse = getClient().prepareGet(InstPerformanceTable.TABLE, id).get();
        if (getResponse.isExists()) {
            logger.debug("id: {} is exist", id);
            Data data = dataDefine.build(id);
            Map<String, Object> source = getResponse.getSource();
            data.setDataInteger(0, (Integer)source.get(InstPerformanceTable.COLUMN_APPLICATION_ID));
            data.setDataInteger(1, (Integer)source.get(InstPerformanceTable.COLUMN_INSTANCE_ID));
            data.setDataInteger(2, (Integer)source.get(InstPerformanceTable.COLUMN_CALLS));
            data.setDataLong(0, ((Number)source.get(InstPerformanceTable.COLUMN_COST_TOTAL)).longValue());
            data.setDataLong(1, ((Number)source.get(InstPerformanceTable.COLUMN_TIME_BUCKET)).longValue());
            return data;
        } else {
            return null;
        }
    }

    @Override public IndexRequestBuilder prepareBatchInsert(Data data) {
        Map<String, Object> source = new HashMap<>();
        source.put(InstPerformanceTable.COLUMN_APPLICATION_ID, data.getDataInteger(0));
        source.put(InstPerformanceTable.COLUMN_INSTANCE_ID, data.getDataInteger(1));
        source.put(InstPerformanceTable.COLUMN_CALLS, data.getDataInteger(2));
        source.put(InstPerformanceTable.COLUMN_COST_TOTAL, data.getDataLong(0));
        source.put(InstPerformanceTable.COLUMN_TIME_BUCKET, data.getDataLong(1));

        return getClient().prepareIndex(InstPerformanceTable.TABLE, data.getDataString(0)).setSource(source);
    }

    @Override public UpdateRequestBuilder prepareBatchUpdate(Data data) {
        Map<String, Object> source = new HashMap<>();
        source.put(InstPerformanceTable.COLUMN_APPLICATION_ID, data.getDataInteger(0));
        source.put(InstPerformanceTable.COLUMN_INSTANCE_ID, data.getDataInteger(1));
        source.put(InstPerformanceTable.COLUMN_CALLS, data.getDataInteger(2));
        source.put(InstPerformanceTable.COLUMN_COST_TOTAL, data.getDataLong(0));
        source.put(InstPerformanceTable.COLUMN_TIME_BUCKET, data.getDataLong(1));

        return getClient().prepareUpdate(InstPerformanceTable.TABLE, data.getDataString(0)).setDoc(source);
    }
}
