package org.skywalking.apm.agent.core.context;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skywalking.apm.agent.core.conf.RemoteDownstreamConfig;
import org.skywalking.apm.agent.core.context.tag.Tags;
import org.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;
import org.skywalking.apm.agent.core.context.trace.LogDataEntity;
import org.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.skywalking.apm.agent.core.context.trace.TraceSegment;
import org.skywalking.apm.agent.core.context.trace.TraceSegmentRef;
import org.skywalking.apm.agent.core.context.util.AbstractTracingSpanHelper;
import org.skywalking.apm.agent.core.context.util.SegmentHelper;
import org.skywalking.apm.agent.core.context.util.SpanHelper;
import org.skywalking.apm.agent.core.test.tools.AgentServiceRule;
import org.skywalking.apm.agent.core.test.tools.SegmentStorage;
import org.skywalking.apm.agent.core.test.tools.SegmentStoragePoint;
import org.skywalking.apm.agent.core.context.util.TraceSegmentRefHelper;
import org.skywalking.apm.agent.core.test.tools.TracingSegmentRunner;
import org.skywalking.apm.agent.core.dictionary.DictionaryUtil;
import org.skywalking.apm.network.proto.KeyWithStringValue;
import org.skywalking.apm.network.proto.LogMessage;
import org.skywalking.apm.network.proto.SpanObject;
import org.skywalking.apm.network.proto.SpanType;
import org.skywalking.apm.network.proto.TraceSegmentObject;
import org.skywalking.apm.network.proto.TraceSegmentReference;
import org.skywalking.apm.network.proto.UpstreamSegment;
import org.skywalking.apm.network.trace.component.ComponentsDefine;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(TracingSegmentRunner.class)
public class ContextManagerTest {

    @SegmentStoragePoint
    private SegmentStorage tracingData;

    @Rule
    public AgentServiceRule agentServiceRule = new AgentServiceRule();

    @Before
    public void setUp() throws Exception {
        RemoteDownstreamConfig.Agent.APPLICATION_ID = 1;
        RemoteDownstreamConfig.Agent.APPLICATION_INSTANCE_ID = 1;
    }

    @Test
    public void createSpanWithInvalidateContextCarrier() {
        ContextCarrier contextCarrier = new ContextCarrier().deserialize("#AQA=#AQA=4WcWe0tQNQA=|1|#127.0.0.1:8080|#/testEntrySpan|#/testEntrySpan|#AQA=#AQA=Et0We0tQNQA=");

        AbstractSpan firstEntrySpan = ContextManager.createEntrySpan("/testEntrySpan", contextCarrier);
        firstEntrySpan.setComponent(ComponentsDefine.TOMCAT);
        Tags.HTTP.METHOD.set(firstEntrySpan, "GET");
        Tags.URL.set(firstEntrySpan, "127.0.0.1:8080");
        SpanLayer.asHttp(firstEntrySpan);

        ContextManager.stopSpan();

        TraceSegment actualSegment = tracingData.getTraceSegments().get(0);
        assertNull(actualSegment.getRefs());

        List<AbstractTracingSpan> spanList = SegmentHelper.getSpan(actualSegment);
        assertThat(spanList.size(), is(1));

        AbstractTracingSpan actualEntrySpan = spanList.get(0);
        assertThat(actualEntrySpan.getOperationName(), is("/testEntrySpan"));
        assertThat(actualEntrySpan.getSpanId(), is(0));
        assertThat(AbstractTracingSpanHelper.getParentSpanId(actualEntrySpan), is(-1));
    }

    @Test
    public void createMultipleEntrySpan() {
        ContextCarrier contextCarrier = new ContextCarrier().deserialize("1.2343.234234234|1|1|1|#127.0.0.1:8080|#/portal/|#/testEntrySpan|1.2343.234234234");
        assertTrue(contextCarrier.isValid());

        AbstractSpan firstEntrySpan = ContextManager.createEntrySpan("/testFirstEntry", contextCarrier);
        firstEntrySpan.setComponent(ComponentsDefine.TOMCAT);
        Tags.HTTP.METHOD.set(firstEntrySpan, "GET");
        Tags.URL.set(firstEntrySpan, "127.0.0.1:8080");
        SpanLayer.asHttp(firstEntrySpan);

        AbstractSpan secondEntrySpan = ContextManager.createEntrySpan("/testSecondEntry", contextCarrier);
        secondEntrySpan.setComponent(ComponentsDefine.DUBBO);
        Tags.URL.set(firstEntrySpan, "dubbo://127.0.0.1:8080");
        SpanLayer.asRPCFramework(secondEntrySpan);

        ContextCarrier injectContextCarrier = new ContextCarrier();
        AbstractSpan exitSpan = ContextManager.createExitSpan("/textExitSpan", injectContextCarrier, "127.0.0.1:12800");
        exitSpan.errorOccurred();
        exitSpan.log(new RuntimeException("exception"));
        exitSpan.setComponent(ComponentsDefine.HTTPCLIENT);

        ContextManager.stopSpan();
        ContextManager.stopSpan();
        SpanLayer.asHttp(firstEntrySpan);
        firstEntrySpan.setOperationName("/testFirstEntry-setOperationName");
        ContextManager.stopSpan();

        assertThat(tracingData.getTraceSegments().size(), is(1));

        TraceSegment actualSegment = tracingData.getTraceSegments().get(0);
        assertThat(actualSegment.getRefs().size(), is(1));

        TraceSegmentRef ref = actualSegment.getRefs().get(0);
        assertThat(TraceSegmentRefHelper.getPeerHost(ref), is("127.0.0.1:8080"));
        assertThat(ref.getEntryOperationName(), is("/portal/"));
        assertThat(ref.getEntryOperationId(), is(0));

        List<AbstractTracingSpan> spanList = SegmentHelper.getSpan(actualSegment);
        assertThat(spanList.size(), is(2));

        AbstractTracingSpan actualEntrySpan = spanList.get(1);
        assertThat(actualEntrySpan.getOperationName(), is("/testSecondEntry"));
        assertThat(actualEntrySpan.getSpanId(), is(0));
        assertThat(AbstractTracingSpanHelper.getParentSpanId(actualEntrySpan), is(-1));
        assertThat(SpanHelper.getComponentId(actualEntrySpan), is(ComponentsDefine.DUBBO.getId()));
        assertThat(SpanHelper.getLayer(actualEntrySpan), is(SpanLayer.RPC_FRAMEWORK));

        AbstractTracingSpan actualExitSpan = spanList.get(0);
        assertThat(actualExitSpan.getOperationName(), is("/textExitSpan"));
        assertThat(actualExitSpan.getSpanId(), is(1));
        assertThat(AbstractTracingSpanHelper.getParentSpanId(actualExitSpan), is(0));

        List<LogDataEntity> logs = AbstractTracingSpanHelper.getLogs(actualExitSpan);
        assertThat(logs.size(), is(1));
        assertThat(logs.get(0).getLogs().size(), is(4));

        assertThat(injectContextCarrier.getSpanId(), is(1));
        assertThat(injectContextCarrier.getEntryOperationName(), is("#/portal/"));
        assertThat(injectContextCarrier.getPeerHost(), is("#127.0.0.1:12800"));
    }

    @Test
    public void createMultipleExitSpan() {
        AbstractSpan entrySpan = ContextManager.createEntrySpan("/testEntrySpan", null);
        entrySpan.setComponent(ComponentsDefine.TOMCAT);
        Tags.HTTP.METHOD.set(entrySpan, "GET");
        Tags.URL.set(entrySpan, "127.0.0.1:8080");
        SpanLayer.asHttp(entrySpan);

        ContextCarrier firstExitSpanContextCarrier = new ContextCarrier();
        AbstractSpan firstExitSpan = ContextManager.createExitSpan("/testFirstExit", firstExitSpanContextCarrier, "127.0.0.1:8080");
        firstExitSpan.setComponent(ComponentsDefine.DUBBO);
        Tags.URL.set(firstExitSpan, "dubbo://127.0.0.1:8080");
        SpanLayer.asRPCFramework(firstExitSpan);

        ContextCarrier secondExitSpanContextCarrier = new ContextCarrier();
        AbstractSpan secondExitSpan = ContextManager.createExitSpan("/testSecondExit", secondExitSpanContextCarrier, "127.0.0.1:9080");
        secondExitSpan.setComponent(ComponentsDefine.TOMCAT);
        Tags.HTTP.METHOD.set(secondExitSpan, "GET");
        Tags.URL.set(secondExitSpan, "127.0.0.1:8080");
        SpanLayer.asHttp(secondExitSpan);
        secondExitSpan.setOperationName("/testSecondExit-setOperationName");

        ContextManager.stopSpan();
        ContextManager.stopSpan();
        ContextManager.stopSpan();

        assertThat(tracingData.getTraceSegments().size(), is(1));
        TraceSegment actualSegment = tracingData.getTraceSegments().get(0);
        assertNull(actualSegment.getRefs());

        List<AbstractTracingSpan> spanList = SegmentHelper.getSpan(actualSegment);
        assertThat(spanList.size(), is(2));

        AbstractTracingSpan actualFirstExitSpan = spanList.get(0);
        assertThat(actualFirstExitSpan.getOperationName(), is("/testFirstExit"));
        assertThat(actualFirstExitSpan.getSpanId(), is(1));
        assertThat(AbstractTracingSpanHelper.getParentSpanId(actualFirstExitSpan), is(0));
        assertThat(SpanHelper.getComponentId(actualFirstExitSpan), is(ComponentsDefine.DUBBO.getId()));
        assertThat(SpanHelper.getLayer(actualFirstExitSpan), is(SpanLayer.RPC_FRAMEWORK));

        AbstractTracingSpan actualEntrySpan = spanList.get(1);
        assertThat(actualEntrySpan.getOperationName(), is("/testEntrySpan"));
        assertThat(actualEntrySpan.getSpanId(), is(0));
        assertThat(AbstractTracingSpanHelper.getParentSpanId(actualEntrySpan), is(-1));

        assertThat(firstExitSpanContextCarrier.getPeerHost(), is("#127.0.0.1:8080"));
        assertThat(firstExitSpanContextCarrier.getSpanId(), is(1));
        assertThat(firstExitSpanContextCarrier.getEntryOperationName(), is("#/testEntrySpan"));

        assertThat(secondExitSpanContextCarrier.getPeerHost(), is("#127.0.0.1:8080"));
        assertThat(secondExitSpanContextCarrier.getSpanId(), is(1));
        assertThat(secondExitSpanContextCarrier.getEntryOperationName(), is("#/testEntrySpan"));

    }

    @After
    public void tearDown() throws Exception {
        RemoteDownstreamConfig.Agent.APPLICATION_ID = DictionaryUtil.nullValue();
        RemoteDownstreamConfig.Agent.APPLICATION_INSTANCE_ID = DictionaryUtil.nullValue();
    }

    @Test
    public void testTransform() throws InvalidProtocolBufferException {
        ContextCarrier contextCarrier = new ContextCarrier().deserialize("1.234.1983829|3|1|1|#127.0.0.1:8080|#/portal/|#/testEntrySpan|1.2343.234234234");
        assertTrue(contextCarrier.isValid());

        AbstractSpan firstEntrySpan = ContextManager.createEntrySpan("/testFirstEntry", contextCarrier);
        firstEntrySpan.setComponent(ComponentsDefine.TOMCAT);
        Tags.HTTP.METHOD.set(firstEntrySpan, "GET");
        Tags.URL.set(firstEntrySpan, "127.0.0.1:8080");
        SpanLayer.asHttp(firstEntrySpan);

        AbstractSpan secondEntrySpan = ContextManager.createEntrySpan("/testSecondEntry", contextCarrier);
        secondEntrySpan.setComponent(ComponentsDefine.DUBBO);
        Tags.URL.set(firstEntrySpan, "dubbo://127.0.0.1:8080");
        SpanLayer.asRPCFramework(secondEntrySpan);

        ContextCarrier injectContextCarrier = new ContextCarrier();
        AbstractSpan exitSpan = ContextManager.createExitSpan("/textExitSpan", injectContextCarrier, "127.0.0.1:12800");
        exitSpan.errorOccurred();
        exitSpan.log(new RuntimeException("exception"));
        exitSpan.setComponent(ComponentsDefine.HTTPCLIENT);
        SpanLayer.asHttp(exitSpan);

        ContextManager.stopSpan();
        ContextManager.stopSpan();
        ContextManager.stopSpan();

        TraceSegment actualSegment = tracingData.getTraceSegments().get(0);

        UpstreamSegment upstreamSegment = actualSegment.transform();
        assertThat(upstreamSegment.getGlobalTraceIdsCount(), is(1));
        TraceSegmentObject traceSegmentObject = TraceSegmentObject.parseFrom(upstreamSegment.getSegment());
        TraceSegmentReference reference = traceSegmentObject.getRefs(0);

        assertThat(reference.getEntryServiceName(), is("/portal/"));
        assertThat(reference.getNetworkAddress(), is("127.0.0.1:8080"));
        assertThat(reference.getParentSpanId(), is(3));

        assertThat(traceSegmentObject.getApplicationId(), is(1));
        assertThat(traceSegmentObject.getRefsCount(), is(1));

        assertThat(traceSegmentObject.getSpansCount(), is(2));

        SpanObject actualSpan = traceSegmentObject.getSpans(1);
        assertThat(actualSpan.getComponentId(), is(3));
        assertThat(actualSpan.getComponent(), is(""));

        assertThat(actualSpan.getOperationName(), is("/testSecondEntry"));
        assertThat(actualSpan.getParentSpanId(), is(-1));
        assertThat(actualSpan.getSpanId(), is(0));
        assertThat(actualSpan.getSpanType(), is(SpanType.Entry));

        SpanObject exitSpanObject = traceSegmentObject.getSpans(0);
        assertThat(exitSpanObject.getComponentId(), is(2));
        assertThat(exitSpanObject.getComponent(), is(""));
        assertThat(exitSpanObject.getSpanType(), is(SpanType.Exit));

        assertThat(exitSpanObject.getOperationName(), is("/textExitSpan"));
        assertThat(exitSpanObject.getParentSpanId(), is(0));
        assertThat(exitSpanObject.getSpanId(), is(1));

        assertThat(exitSpanObject.getLogsCount(), is(1));
        LogMessage logMessage = exitSpanObject.getLogs(0);
        assertThat(logMessage.getDataCount(), is(4));
        List<KeyWithStringValue> values = logMessage.getDataList();

        assertThat(values.get(0).getValue(), is("error"));
        assertThat(values.get(1).getValue(), is(RuntimeException.class.getName()));
        assertThat(values.get(2).getValue(), is("exception"));
        assertTrue(values.get(2).getValue().length() <= 4000);
    }
}
