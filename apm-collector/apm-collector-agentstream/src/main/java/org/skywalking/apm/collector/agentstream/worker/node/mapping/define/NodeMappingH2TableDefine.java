package org.skywalking.apm.collector.agentstream.worker.node.mapping.define;

import org.skywalking.apm.collector.storage.define.node.NodeMappingTable;
import org.skywalking.apm.collector.storage.h2.define.H2ColumnDefine;
import org.skywalking.apm.collector.storage.h2.define.H2TableDefine;

/**
 * @author pengys5
 */
public class NodeMappingH2TableDefine extends H2TableDefine {

    public NodeMappingH2TableDefine() {
        super(NodeMappingTable.TABLE);
    }

    @Override public void initialize() {
        addColumn(new H2ColumnDefine(NodeMappingTable.COLUMN_ID, H2ColumnDefine.Type.Varchar.name()));
        addColumn(new H2ColumnDefine(NodeMappingTable.COLUMN_APPLICATION_ID, H2ColumnDefine.Type.Int.name()));
        addColumn(new H2ColumnDefine(NodeMappingTable.COLUMN_ADDRESS_ID, H2ColumnDefine.Type.Int.name()));
        addColumn(new H2ColumnDefine(NodeMappingTable.COLUMN_ADDRESS, H2ColumnDefine.Type.Varchar.name()));
        addColumn(new H2ColumnDefine(NodeMappingTable.COLUMN_TIME_BUCKET, H2ColumnDefine.Type.Bigint.name()));
    }
}
