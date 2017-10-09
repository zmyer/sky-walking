package org.skywalking.apm.collector.agentregister.instance;

import org.skywalking.apm.collector.agentregister.worker.instance.InstanceRegisterRemoteWorker;
import org.skywalking.apm.collector.agentregister.worker.instance.dao.IInstanceDAO;
import org.skywalking.apm.collector.core.framework.CollectorContextHelper;
import org.skywalking.apm.collector.storage.dao.DAOContainer;
import org.skywalking.apm.collector.storage.define.register.InstanceDataDefine;
import org.skywalking.apm.collector.stream.StreamModuleContext;
import org.skywalking.apm.collector.stream.StreamModuleGroupDefine;
import org.skywalking.apm.collector.stream.worker.WorkerInvokeException;
import org.skywalking.apm.collector.stream.worker.WorkerNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pengys5
 */
public class InstanceIDService {

    private final Logger logger = LoggerFactory.getLogger(InstanceIDService.class);

    public int getOrCreate(int applicationId, String agentUUID, long registerTime, String osInfo) {
        logger.debug("get or create instance id, application id: {}, agentUUID: {}, registerTime: {}, osInfo: {}", applicationId, agentUUID, registerTime, osInfo);
        IInstanceDAO dao = (IInstanceDAO)DAOContainer.INSTANCE.get(IInstanceDAO.class.getName());
        int instanceId = dao.getInstanceId(applicationId, agentUUID);

        if (instanceId == 0) {
            StreamModuleContext context = (StreamModuleContext)CollectorContextHelper.INSTANCE.getContext(StreamModuleGroupDefine.GROUP_NAME);
            InstanceDataDefine.Instance instance = new InstanceDataDefine.Instance("0", applicationId, agentUUID, registerTime, 0, registerTime, osInfo);
            try {
                context.getClusterWorkerContext().lookup(InstanceRegisterRemoteWorker.WorkerRole.INSTANCE).tell(instance);
            } catch (WorkerNotFoundException | WorkerInvokeException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return instanceId;
    }

    public void recover(int instanceId, int applicationId, long registerTime, String osInfo) {
        logger.debug("instance recover, instance id: {}, application id: {}, register time: {}", instanceId, applicationId, registerTime);
        IInstanceDAO dao = (IInstanceDAO)DAOContainer.INSTANCE.get(IInstanceDAO.class.getName());

        InstanceDataDefine.Instance instance = new InstanceDataDefine.Instance(String.valueOf(instanceId), applicationId, "", registerTime, instanceId, registerTime, osInfo);
        dao.save(instance);
    }
}
