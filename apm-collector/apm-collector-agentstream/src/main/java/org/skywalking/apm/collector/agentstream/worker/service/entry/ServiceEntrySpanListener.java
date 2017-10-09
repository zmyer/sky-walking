package org.skywalking.apm.collector.agentstream.worker.service.entry;

import org.skywalking.apm.collector.agentstream.worker.cache.ServiceCache;
import org.skywalking.apm.collector.agentstream.worker.segment.EntrySpanListener;
import org.skywalking.apm.collector.agentstream.worker.segment.FirstSpanListener;
import org.skywalking.apm.collector.agentstream.worker.segment.RefsListener;
import org.skywalking.apm.collector.core.framework.CollectorContextHelper;
import org.skywalking.apm.collector.core.util.Const;
import org.skywalking.apm.collector.core.util.TimeBucketUtils;
import org.skywalking.apm.collector.storage.define.service.ServiceEntryDataDefine;
import org.skywalking.apm.collector.stream.StreamModuleContext;
import org.skywalking.apm.collector.stream.StreamModuleGroupDefine;
import org.skywalking.apm.collector.stream.worker.WorkerInvokeException;
import org.skywalking.apm.collector.stream.worker.WorkerNotFoundException;
import org.skywalking.apm.network.proto.SpanObject;
import org.skywalking.apm.network.proto.TraceSegmentReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pengys5
 */
public class ServiceEntrySpanListener implements RefsListener, FirstSpanListener, EntrySpanListener {

    private final Logger logger = LoggerFactory.getLogger(ServiceEntrySpanListener.class);

    private long timeBucket;
    private boolean hasReference = false;
    private int applicationId;
    private int entryServiceId;
    private String entryServiceName;
    private boolean hasEntry = false;

    @Override
    public void parseEntry(SpanObject spanObject, int applicationId, int applicationInstanceId, String segmentId) {
        this.applicationId = applicationId;
        this.entryServiceId = spanObject.getOperationNameId();
        if (spanObject.getOperationNameId() == 0) {
            this.entryServiceName = spanObject.getOperationName();
        } else {
            this.entryServiceName = ServiceCache.getServiceName(this.entryServiceId);
        }
        this.hasEntry = true;
    }

    @Override public void parseRef(TraceSegmentReference reference, int applicationId, int applicationInstanceId,
        String segmentId) {
        hasReference = true;
    }

    @Override
    public void parseFirst(SpanObject spanObject, int applicationId, int applicationInstanceId, String segmentId) {
        timeBucket = TimeBucketUtils.INSTANCE.getMinuteTimeBucket(spanObject.getStartTime());
    }

    @Override public void build() {
        logger.debug("entry service listener build");
        StreamModuleContext context = (StreamModuleContext)CollectorContextHelper.INSTANCE.getContext(StreamModuleGroupDefine.GROUP_NAME);
        if (!hasReference && hasEntry) {
            ServiceEntryDataDefine.ServiceEntry serviceEntry = new ServiceEntryDataDefine.ServiceEntry();
            serviceEntry.setId(applicationId + Const.ID_SPLIT + entryServiceName);
            serviceEntry.setApplicationId(applicationId);
            serviceEntry.setEntryServiceId(entryServiceId);
            serviceEntry.setEntryServiceName(entryServiceName);
            serviceEntry.setRegisterTime(timeBucket);
            serviceEntry.setNewestTime(timeBucket);

            try {
                logger.debug("send to service entry aggregation worker, id: {}", serviceEntry.getId());
                context.getClusterWorkerContext().lookup(ServiceEntryAggregationWorker.WorkerRole.INSTANCE).tell(serviceEntry.toData());
            } catch (WorkerInvokeException | WorkerNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
