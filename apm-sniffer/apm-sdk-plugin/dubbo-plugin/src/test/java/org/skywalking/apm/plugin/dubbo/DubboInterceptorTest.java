package org.skywalking.apm.plugin.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.skywalking.apm.agent.core.conf.Config;
import org.skywalking.apm.agent.core.context.SW3CarrierItem;
import org.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;
import org.skywalking.apm.agent.core.context.trace.LogDataEntity;
import org.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.skywalking.apm.agent.core.context.trace.TraceSegment;
import org.skywalking.apm.agent.core.context.trace.TraceSegmentRef;
import org.skywalking.apm.agent.core.context.util.KeyValuePair;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.skywalking.apm.agent.test.helper.SegmentHelper;
import org.skywalking.apm.agent.test.helper.SegmentRefHelper;
import org.skywalking.apm.agent.test.helper.SpanHelper;
import org.skywalking.apm.agent.test.tools.AgentServiceRule;
import org.skywalking.apm.agent.test.tools.SegmentStorage;
import org.skywalking.apm.agent.test.tools.SegmentStoragePoint;
import org.skywalking.apm.agent.test.tools.TracingSegmentRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(TracingSegmentRunner.class)
@PrepareForTest({RpcContext.class})
public class DubboInterceptorTest {

    @SegmentStoragePoint
    private SegmentStorage segmentStorage;

    @Rule
    public AgentServiceRule agentServiceRule = new AgentServiceRule();

    @Mock
    private EnhancedInstance enhancedInstance;

    private DubboInterceptor dubboInterceptor;

    @Mock
    private RpcContext rpcContext;
    @Mock
    private Invoker invoker;
    @Mock
    private Invocation invocation;
    @Mock
    private MethodInterceptResult methodInterceptResult;
    @Mock
    private Result result;

    private Object[] allArguments;
    private Class[] argumentTypes;

    @Before
    public void setUp() throws Exception {
        dubboInterceptor = new DubboInterceptor();

        PowerMockito.mockStatic(RpcContext.class);

        when(invoker.getUrl()).thenReturn(URL.valueOf("dubbo://127.0.0.1:20880/org.skywalking.apm.test.TestDubboService"));
        when(invocation.getMethodName()).thenReturn("test");
        when(invocation.getParameterTypes()).thenReturn(new Class[] {String.class});
        when(invocation.getArguments()).thenReturn(new Object[] {"abc"});
        PowerMockito.when(RpcContext.getContext()).thenReturn(rpcContext);
        when(rpcContext.isConsumerSide()).thenReturn(true);
        allArguments = new Object[] {invoker, invocation};
        argumentTypes = new Class[] {invoker.getClass(), invocation.getClass()};
        Config.Agent.APPLICATION_CODE = "DubboTestCases-APP";
    }


    @Test
    public void testConsumerWithAttachment() throws Throwable {
        dubboInterceptor.beforeMethod(enhancedInstance, null, allArguments, argumentTypes, methodInterceptResult);
        dubboInterceptor.afterMethod(enhancedInstance, null, allArguments, argumentTypes, result);

        assertThat(segmentStorage.getTraceSegments().size(), is(1));
        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
        List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);
        assertThat(spans.size(), is(1));
        assertConsumerSpan(spans.get(0));
    }

    @Test
    public void testConsumerWithException() throws Throwable {
        dubboInterceptor.beforeMethod(enhancedInstance, null, allArguments, argumentTypes, methodInterceptResult);
        dubboInterceptor.handleMethodException(enhancedInstance, null, allArguments, argumentTypes, new RuntimeException());
        dubboInterceptor.afterMethod(enhancedInstance, null, allArguments, argumentTypes, result);
        assertThat(segmentStorage.getTraceSegments().size(), is(1));
        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
        assertConsumerTraceSegmentInErrorCase(traceSegment);
    }

    @Test
    public void testConsumerWithResultHasException() throws Throwable {
        when(result.getException()).thenReturn(new RuntimeException());

        dubboInterceptor.beforeMethod(enhancedInstance, null, allArguments, argumentTypes, methodInterceptResult);
        dubboInterceptor.afterMethod(enhancedInstance, null, allArguments, argumentTypes, result);

        assertThat(segmentStorage.getTraceSegments().size(), is(1));
        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
        assertConsumerTraceSegmentInErrorCase(traceSegment);
    }

    @Test
    public void testProviderWithAttachment() throws Throwable {
        when(rpcContext.isConsumerSide()).thenReturn(false);
        when(rpcContext.getAttachment(SW3CarrierItem.HEADER_NAME)).thenReturn("1.323.4433|3|1|1|#192.168.1.8 :18002|#/portal/|#/testEntrySpan|#AQA*#AQA*Et0We0tQNQA*");

        dubboInterceptor.beforeMethod(enhancedInstance, null, allArguments, argumentTypes, methodInterceptResult);
        dubboInterceptor.afterMethod(enhancedInstance, null, allArguments, argumentTypes, result);
        assertProvider();
    }


    private void assertConsumerTraceSegmentInErrorCase(
        TraceSegment traceSegment) {
        List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);
        assertThat(spans.size(), is(1));
        assertConsumerSpan(spans.get(0));
        AbstractTracingSpan span = spans.get(0);
        assertThat(SpanHelper.getLogs(span).size(), is(1));
        assertErrorLog(SpanHelper.getLogs(span).get(0));
    }

    private void assertErrorLog(LogDataEntity logData) {
        assertThat(logData.getLogs().size(), is(4));
        assertThat(logData.getLogs().get(0).getValue(), CoreMatchers.<Object>is("error"));
        assertThat(logData.getLogs().get(1).getValue(), CoreMatchers.<Object>is(RuntimeException.class.getName()));
        assertNull(logData.getLogs().get(2).getValue());
    }

    private void assertProvider() {
        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
        assertThat(SegmentHelper.getSpans(traceSegment).size(), is(1));
        assertProviderSpan(SegmentHelper.getSpans(traceSegment).get(0));
        assertTraceSegmentRef(traceSegment.getRefs().get(0));
    }

    private void assertTraceSegmentRef(TraceSegmentRef actual) {
        assertThat(SegmentRefHelper.getSpanId(actual), is(3));
        assertThat(SegmentRefHelper.getEntryApplicationInstanceId(actual), is(1));
        assertThat(SegmentRefHelper.getTraceSegmentId(actual).toString(), is("1.323.4433"));
    }

    private void assertProviderSpan(AbstractTracingSpan span) {
        assertCommonsAttribute(span);
        assertTrue(span.isEntry());
    }

    private void assertConsumerSpan(AbstractTracingSpan span) {
        assertCommonsAttribute(span);
        assertTrue(span.isExit());
    }

    private void assertCommonsAttribute(AbstractTracingSpan span) {
        List<KeyValuePair> tags = SpanHelper.getTags(span);
        assertThat(tags.size(), is(1));
        assertThat(SpanHelper.getLayer(span), is(SpanLayer.RPC_FRAMEWORK));
        assertThat(SpanHelper.getComponentId(span), is(3));
        assertThat(tags.get(0).getValue(), is("dubbo://127.0.0.1:20880/org.skywalking.apm.test.TestDubboService.test(String)"));
        assertThat(span.getOperationName(), is("org.skywalking.apm.test.TestDubboService.test(String)"));
    }
}
