package org.skywalking.apm.collector.agentstream.worker.node.component.dao;

import java.util.HashMap;
import java.util.Map;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.skywalking.apm.collector.core.stream.Data;
import org.skywalking.apm.collector.storage.define.DataDefine;
import org.skywalking.apm.collector.storage.define.node.NodeComponentTable;
import org.skywalking.apm.collector.storage.elasticsearch.dao.EsDAO;
import org.skywalking.apm.collector.stream.worker.impl.dao.IPersistenceDAO;

/**
 * @author pengys5
 */
public class NodeComponentEsDAO extends EsDAO implements INodeComponentDAO, IPersistenceDAO<IndexRequestBuilder, UpdateRequestBuilder> {

    @Override public Data get(String id, DataDefine dataDefine) {
        GetResponse getResponse = getClient().prepareGet(NodeComponentTable.TABLE, id).get();
        if (getResponse.isExists()) {
            Data data = dataDefine.build(id);
            Map<String, Object> source = getResponse.getSource();
            data.setDataInteger(0, ((Number)source.get(NodeComponentTable.COLUMN_COMPONENT_ID)).intValue());
            data.setDataString(1, (String)source.get(NodeComponentTable.COLUMN_COMPONENT_NAME));
            data.setDataInteger(1, ((Number)source.get(NodeComponentTable.COLUMN_PEER_ID)).intValue());
            data.setDataString(2, (String)source.get(NodeComponentTable.COLUMN_PEER));
            data.setDataLong(0, (Long)source.get(NodeComponentTable.COLUMN_TIME_BUCKET));
            return data;
        } else {
            return null;
        }
    }

    @Override public IndexRequestBuilder prepareBatchInsert(Data data) {
        Map<String, Object> source = new HashMap<>();
        source.put(NodeComponentTable.COLUMN_COMPONENT_ID, data.getDataInteger(0));
        source.put(NodeComponentTable.COLUMN_COMPONENT_NAME, data.getDataString(1));
        source.put(NodeComponentTable.COLUMN_PEER_ID, data.getDataInteger(1));
        source.put(NodeComponentTable.COLUMN_PEER, data.getDataString(2));
        source.put(NodeComponentTable.COLUMN_TIME_BUCKET, data.getDataLong(0));

        return getClient().prepareIndex(NodeComponentTable.TABLE, data.getDataString(0)).setSource(source);
    }

    @Override public UpdateRequestBuilder prepareBatchUpdate(Data data) {
        Map<String, Object> source = new HashMap<>();
        source.put(NodeComponentTable.COLUMN_COMPONENT_ID, data.getDataInteger(0));
        source.put(NodeComponentTable.COLUMN_COMPONENT_NAME, data.getDataString(1));
        source.put(NodeComponentTable.COLUMN_PEER_ID, data.getDataInteger(1));
        source.put(NodeComponentTable.COLUMN_PEER, data.getDataString(2));
        source.put(NodeComponentTable.COLUMN_TIME_BUCKET, data.getDataLong(0));

        return getClient().prepareUpdate(NodeComponentTable.TABLE, data.getDataString(0)).setDoc(source);
    }
}
