package org.skywalking.apm.collector.agentstream.worker.service.entry.define;

import org.skywalking.apm.collector.storage.define.service.ServiceEntryTable;
import org.skywalking.apm.collector.storage.h2.define.H2ColumnDefine;
import org.skywalking.apm.collector.storage.h2.define.H2TableDefine;

/**
 * @author pengys5
 */
public class ServiceEntryH2TableDefine extends H2TableDefine {

    public ServiceEntryH2TableDefine() {
        super(ServiceEntryTable.TABLE);
    }

    @Override public void initialize() {
        addColumn(new H2ColumnDefine(ServiceEntryTable.COLUMN_ID, H2ColumnDefine.Type.Varchar.name()));
        addColumn(new H2ColumnDefine(ServiceEntryTable.COLUMN_APPLICATION_ID, H2ColumnDefine.Type.Int.name()));
        addColumn(new H2ColumnDefine(ServiceEntryTable.COLUMN_ENTRY_SERVICE_ID, H2ColumnDefine.Type.Int.name()));
        addColumn(new H2ColumnDefine(ServiceEntryTable.COLUMN_ENTRY_SERVICE_NAME, H2ColumnDefine.Type.Varchar.name()));
        addColumn(new H2ColumnDefine(ServiceEntryTable.COLUMN_REGISTER_TIME, H2ColumnDefine.Type.Bigint.name()));
        addColumn(new H2ColumnDefine(ServiceEntryTable.COLUMN_NEWEST_TIME, H2ColumnDefine.Type.Bigint.name()));
    }
}
