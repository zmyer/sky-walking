package org.skywalking.apm.collector.agentstream.jetty;

import java.util.LinkedList;
import java.util.List;
import org.skywalking.apm.collector.agentregister.jetty.handler.ApplicationRegisterServletHandler;
import org.skywalking.apm.collector.agentregister.jetty.handler.InstanceDiscoveryServletHandler;
import org.skywalking.apm.collector.agentregister.jetty.handler.ServiceNameDiscoveryServiceHandler;
import org.skywalking.apm.collector.agentstream.AgentStreamModuleDefine;
import org.skywalking.apm.collector.agentstream.AgentStreamModuleGroupDefine;
import org.skywalking.apm.collector.agentstream.jetty.handler.TraceSegmentServletHandler;
import org.skywalking.apm.collector.core.cluster.ClusterDataListener;
import org.skywalking.apm.collector.core.framework.Handler;
import org.skywalking.apm.collector.core.module.ModuleConfigParser;
import org.skywalking.apm.collector.core.module.ModuleRegistration;
import org.skywalking.apm.collector.core.server.Server;
import org.skywalking.apm.collector.server.jetty.JettyServer;

/**
 * @author pengys5
 */
public class AgentStreamJettyModuleDefine extends AgentStreamModuleDefine {

    public static final String MODULE_NAME = "jetty";

    @Override protected String group() {
        return AgentStreamModuleGroupDefine.GROUP_NAME;
    }

    @Override public String name() {
        return MODULE_NAME;
    }

    @Override protected ModuleConfigParser configParser() {
        return new AgentStreamJettyConfigParser();
    }

    @Override protected Server server() {
        return new JettyServer(AgentStreamJettyConfig.HOST, AgentStreamJettyConfig.PORT, AgentStreamJettyConfig.CONTEXT_PATH);
    }

    @Override protected ModuleRegistration registration() {
        return new AgentStreamJettyModuleRegistration();
    }

    @Override public ClusterDataListener listener() {
        return new AgentStreamJettyDataListener();
    }

    @Override public List<Handler> handlerList() {
        List<Handler> handlers = new LinkedList<>();
        handlers.add(new TraceSegmentServletHandler());
        handlers.add(new ApplicationRegisterServletHandler());
        handlers.add(new InstanceDiscoveryServletHandler());
        handlers.add(new ServiceNameDiscoveryServiceHandler());
        return handlers;
    }
}
