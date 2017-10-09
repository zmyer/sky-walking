package org.skywalking.apm.collector.cluster.standalone;

import org.skywalking.apm.collector.client.h2.H2Client;
import org.skywalking.apm.collector.cluster.ClusterModuleDefine;
import org.skywalking.apm.collector.cluster.ClusterModuleGroupDefine;
import org.skywalking.apm.collector.core.client.Client;
import org.skywalking.apm.collector.core.client.DataMonitor;
import org.skywalking.apm.collector.core.cluster.ClusterModuleRegistrationReader;
import org.skywalking.apm.collector.core.module.ModuleConfigParser;

/**
 * @author pengys5
 */
public class ClusterStandaloneModuleDefine extends ClusterModuleDefine {

    public static final String MODULE_NAME = "standalone";

    private final ClusterStandaloneDataMonitor dataMonitor;

    public ClusterStandaloneModuleDefine() {
        this.dataMonitor = new ClusterStandaloneDataMonitor();
    }

    @Override public String group() {
        return ClusterModuleGroupDefine.GROUP_NAME;
    }

    @Override public String name() {
        return MODULE_NAME;
    }

    @Override public boolean defaultModule() {
        return true;
    }

    @Override protected ModuleConfigParser configParser() {
        return new ClusterStandaloneConfigParser();
    }

    @Override public DataMonitor dataMonitor() {
        return dataMonitor;
    }

    @Override protected Client createClient() {
        return new H2Client();
    }

    @Override public ClusterModuleRegistrationReader registrationReader() {
        return new ClusterStandaloneModuleRegistrationReader(dataMonitor);
    }
}
