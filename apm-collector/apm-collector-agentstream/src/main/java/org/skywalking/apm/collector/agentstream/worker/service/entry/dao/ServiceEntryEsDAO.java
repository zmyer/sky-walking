package org.skywalking.apm.collector.agentstream.worker.service.entry.dao;

import java.util.HashMap;
import java.util.Map;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.skywalking.apm.collector.core.stream.Data;
import org.skywalking.apm.collector.storage.define.DataDefine;
import org.skywalking.apm.collector.storage.define.service.ServiceEntryTable;
import org.skywalking.apm.collector.storage.elasticsearch.dao.EsDAO;
import org.skywalking.apm.collector.stream.worker.impl.dao.IPersistenceDAO;

/**
 * @author pengys5
 */
public class ServiceEntryEsDAO extends EsDAO implements IServiceEntryDAO, IPersistenceDAO<IndexRequestBuilder, UpdateRequestBuilder> {

    @Override public Data get(String id, DataDefine dataDefine) {
        GetResponse getResponse = getClient().prepareGet(ServiceEntryTable.TABLE, id).get();
        if (getResponse.isExists()) {
            Data data = dataDefine.build(id);
            Map<String, Object> source = getResponse.getSource();
            data.setDataInteger(0, ((Number)source.get(ServiceEntryTable.COLUMN_APPLICATION_ID)).intValue());
            data.setDataInteger(1, ((Number)source.get(ServiceEntryTable.COLUMN_ENTRY_SERVICE_ID)).intValue());
            data.setDataString(1, (String)source.get(ServiceEntryTable.COLUMN_ENTRY_SERVICE_NAME));
            data.setDataLong(0, ((Number)source.get(ServiceEntryTable.COLUMN_REGISTER_TIME)).longValue());
            data.setDataLong(1, ((Number)source.get(ServiceEntryTable.COLUMN_NEWEST_TIME)).longValue());
            return data;
        } else {
            return null;
        }
    }

    @Override public IndexRequestBuilder prepareBatchInsert(Data data) {
        Map<String, Object> source = new HashMap<>();
        source.put(ServiceEntryTable.COLUMN_APPLICATION_ID, data.getDataInteger(0));
        source.put(ServiceEntryTable.COLUMN_ENTRY_SERVICE_ID, data.getDataInteger(1));
        source.put(ServiceEntryTable.COLUMN_ENTRY_SERVICE_NAME, data.getDataString(1));
        source.put(ServiceEntryTable.COLUMN_REGISTER_TIME, data.getDataLong(0));
        source.put(ServiceEntryTable.COLUMN_NEWEST_TIME, data.getDataLong(1));
        return getClient().prepareIndex(ServiceEntryTable.TABLE, data.getDataString(0)).setSource(source);
    }

    @Override public UpdateRequestBuilder prepareBatchUpdate(Data data) {
        Map<String, Object> source = new HashMap<>();
        source.put(ServiceEntryTable.COLUMN_APPLICATION_ID, data.getDataInteger(0));
        source.put(ServiceEntryTable.COLUMN_ENTRY_SERVICE_ID, data.getDataInteger(1));
        source.put(ServiceEntryTable.COLUMN_ENTRY_SERVICE_NAME, data.getDataString(1));
        source.put(ServiceEntryTable.COLUMN_REGISTER_TIME, data.getDataLong(0));
        source.put(ServiceEntryTable.COLUMN_NEWEST_TIME, data.getDataLong(1));

        return getClient().prepareUpdate(ServiceEntryTable.TABLE, data.getDataString(0)).setDoc(source);
    }
}
