package org.skywalking.apm.plugin.tomcat78x;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.skywalking.apm.agent.core.context.SW3CarrierItem;
import org.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;
import org.skywalking.apm.agent.core.context.trace.LogDataEntity;
import org.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.skywalking.apm.agent.core.context.trace.TraceSegment;
import org.skywalking.apm.agent.core.context.trace.TraceSegmentRef;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.skywalking.apm.agent.test.helper.SegmentHelper;
import org.skywalking.apm.agent.test.helper.SegmentRefHelper;
import org.skywalking.apm.agent.test.helper.SpanHelper;
import org.skywalking.apm.agent.test.tools.AgentServiceRule;
import org.skywalking.apm.agent.test.tools.SegmentStorage;
import org.skywalking.apm.agent.test.tools.SegmentStoragePoint;
import org.skywalking.apm.agent.test.tools.TracingSegmentRunner;
import org.skywalking.apm.network.trace.component.ComponentsDefine;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.skywalking.apm.agent.test.tools.SpanAssert.assertComponent;
import static org.skywalking.apm.agent.test.tools.SpanAssert.assertException;
import static org.skywalking.apm.agent.test.tools.SpanAssert.assertLayer;
import static org.skywalking.apm.agent.test.tools.SpanAssert.assertTag;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(TracingSegmentRunner.class)
public class TomcatInvokeInterceptorTest {

    private TomcatInvokeInterceptor tomcatInvokeInterceptor;
    private TomcatExceptionInterceptor tomcatExceptionInterceptor;
    @SegmentStoragePoint
    private SegmentStorage segmentStorage;

    @Rule
    public AgentServiceRule serviceRule = new AgentServiceRule();

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private MethodInterceptResult methodInterceptResult;

    @Mock
    private EnhancedInstance enhancedInstance;

    private Object[] arguments;
    private Class[] argumentType;

    private Object[] exceptionArguments;
    private Class[] exceptionArgumentType;

    @Before
    public void setUp() throws Exception {
        tomcatInvokeInterceptor = new TomcatInvokeInterceptor();
        tomcatExceptionInterceptor = new TomcatExceptionInterceptor();
        when(request.getRequestURI()).thenReturn("/test/testRequestURL");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/test/testRequestURL"));
        when(response.getStatus()).thenReturn(200);
        arguments = new Object[] {request, response};
        argumentType = new Class[] {request.getClass(), response.getClass()};

        exceptionArguments = new Object[] {request, response, new RuntimeException()};
        exceptionArgumentType = new Class[] {request.getClass(), response.getClass(), new RuntimeException().getClass()};
    }

    @Test
    public void testWithoutSerializedContextData() throws Throwable {
        tomcatInvokeInterceptor.beforeMethod(enhancedInstance, null, arguments, argumentType, methodInterceptResult);
        tomcatInvokeInterceptor.afterMethod(enhancedInstance, null, arguments, argumentType, null);

        assertThat(segmentStorage.getTraceSegments().size(), is(1));
        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
        List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);
        assertHttpSpan(spans.get(0));
    }

    @Test
    public void testWithSerializedContextData() throws Throwable {
        when(request.getHeader(SW3CarrierItem.HEADER_NAME)).thenReturn("1.234.111|3|1|1|#192.168.1.8:18002|#/portal/|#/testEntrySpan|#AQA*#AQA*Et0We0tQNQA*");

        tomcatInvokeInterceptor.beforeMethod(enhancedInstance, null, arguments, argumentType, methodInterceptResult);
        tomcatInvokeInterceptor.afterMethod(enhancedInstance, null, arguments, argumentType, null);

        assertThat(segmentStorage.getTraceSegments().size(), is(1));
        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
        List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

        assertHttpSpan(spans.get(0));
        assertTraceSegmentRef(traceSegment.getRefs().get(0));
    }

    @Test
    public void testWithOccurException() throws Throwable {
        tomcatInvokeInterceptor.beforeMethod(enhancedInstance, null, arguments, argumentType, methodInterceptResult);
        tomcatInvokeInterceptor.handleMethodException(enhancedInstance, null, arguments, argumentType, new RuntimeException());
        tomcatInvokeInterceptor.afterMethod(enhancedInstance, null, arguments, argumentType, null);

        assertThat(segmentStorage.getTraceSegments().size(), is(1));
        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
        List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

        assertHttpSpan(spans.get(0));
        List<LogDataEntity> logDataEntities = SpanHelper.getLogs(spans.get(0));
        assertThat(logDataEntities.size(), is(1));
        assertException(logDataEntities.get(0), RuntimeException.class);
    }

    @Test
    public void testWithTomcatException() throws Throwable {
        tomcatInvokeInterceptor.beforeMethod(enhancedInstance, null, arguments, argumentType, methodInterceptResult);
        tomcatExceptionInterceptor.beforeMethod(enhancedInstance, null, exceptionArguments, exceptionArgumentType, null);
        tomcatInvokeInterceptor.afterMethod(enhancedInstance, null, arguments, argumentType, null);

        assertThat(segmentStorage.getTraceSegments().size(), is(1));
        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);
        List<AbstractTracingSpan> spans = SegmentHelper.getSpans(traceSegment);

        assertHttpSpan(spans.get(0));
        List<LogDataEntity> logDataEntities = SpanHelper.getLogs(spans.get(0));
        assertThat(logDataEntities.size(), is(1));
        assertException(logDataEntities.get(0), RuntimeException.class);
    }

    private void assertTraceSegmentRef(TraceSegmentRef ref) {
        assertThat(SegmentRefHelper.getEntryApplicationInstanceId(ref), is(1));
        assertThat(SegmentRefHelper.getSpanId(ref), is(3));
        assertThat(SegmentRefHelper.getTraceSegmentId(ref).toString(), is("1.234.111"));
    }

    private void assertHttpSpan(AbstractTracingSpan span) {
        assertThat(span.getOperationName(), is("/test/testRequestURL"));
        assertComponent(span, ComponentsDefine.TOMCAT);
        assertTag(span, 0, "http://localhost:8080/test/testRequestURL");
        assertThat(span.isEntry(), is(true));
        assertLayer(span, SpanLayer.HTTP);
    }
}
