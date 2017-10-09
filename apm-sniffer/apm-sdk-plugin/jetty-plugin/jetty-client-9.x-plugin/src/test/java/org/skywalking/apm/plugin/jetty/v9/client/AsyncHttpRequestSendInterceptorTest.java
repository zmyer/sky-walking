package org.skywalking.apm.plugin.jetty.v9.client;

import java.net.URI;
import java.util.List;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.skywalking.apm.agent.core.context.trace.AbstractTracingSpan;
import org.skywalking.apm.agent.core.context.trace.TraceSegment;
import org.skywalking.apm.agent.core.context.util.KeyValuePair;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.skywalking.apm.agent.test.helper.SegmentHelper;
import org.skywalking.apm.agent.test.helper.SpanHelper;
import org.skywalking.apm.agent.test.tools.AgentServiceRule;
import org.skywalking.apm.agent.test.tools.SegmentStorage;
import org.skywalking.apm.agent.test.tools.SegmentStoragePoint;
import org.skywalking.apm.agent.test.tools.TracingSegmentRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.skywalking.apm.agent.test.tools.SpanAssert.assertException;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(TracingSegmentRunner.class)
public class AsyncHttpRequestSendInterceptorTest {

    @SegmentStoragePoint
    private SegmentStorage segmentStorage;
    @Rule
    public AgentServiceRule serviceRule = new AgentServiceRule();
    @Mock
    private HttpClient httpClient;
    @Mock
    private EnhancedInstance callBackEnhanceInstance;

    private Object[] allArguments;
    private Class[] argumentTypes;
    private MockHttpRequest enhancedInstance;
    private AsyncHttpRequestSendInterceptor interceptor;
    private URI uri = URI.create("http://localhost:8080/test");

    @Before
    public void setUp() throws Exception {
        enhancedInstance = new MockHttpRequest(httpClient, uri);
        allArguments = new Object[] {"OperationKey", "OperationValue"};
        argumentTypes = new Class[] {String.class, String.class};

        interceptor = new AsyncHttpRequestSendInterceptor();
        allArguments = new Object[] {callBackEnhanceInstance};
    }

    @Test
    public void testMethodsAround() throws Throwable {
        interceptor.beforeMethod(enhancedInstance, null, allArguments, argumentTypes, null);
        interceptor.afterMethod(enhancedInstance, null, allArguments, argumentTypes, null);

        assertThat(segmentStorage.getTraceSegments().size(), is(1));
        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);

        Assert.assertEquals(1, SegmentHelper.getSpans(traceSegment).size());
        AbstractTracingSpan finishedSpan = SegmentHelper.getSpans(traceSegment).get(0);

        List<KeyValuePair> tags = SpanHelper.getTags(finishedSpan);
        assertThat(tags.size(), is(2));
        assertThat(tags.get(0).getValue(), is("POST"));
        assertThat(tags.get(1).getValue(), is(uri.toString()));

        Assert.assertEquals(false, SpanHelper.getErrorOccurred(finishedSpan));
    }

    @Test
    public void testMethodsAroundError() throws Throwable {
        interceptor.beforeMethod(enhancedInstance, null, allArguments, argumentTypes, null);
        interceptor.handleMethodException(enhancedInstance, null, allArguments, argumentTypes, new RuntimeException());
        interceptor.afterMethod(enhancedInstance, null, allArguments, argumentTypes, null);

        assertThat(segmentStorage.getTraceSegments().size(), is(1));
        TraceSegment traceSegment = segmentStorage.getTraceSegments().get(0);

        Assert.assertEquals(1, SegmentHelper.getSpans(traceSegment).size());
        AbstractTracingSpan finishedSpan = SegmentHelper.getSpans(traceSegment).get(0);

        List<KeyValuePair> tags = SpanHelper.getTags(finishedSpan);
        assertThat(tags.size(), is(2));
        assertThat(tags.get(0).getValue(), is("POST"));
        assertThat(tags.get(1).getValue(), is(uri.toString()));

        Assert.assertEquals(true, SpanHelper.getErrorOccurred(finishedSpan));
        assertException(SpanHelper.getLogs(finishedSpan).get(0), RuntimeException.class);

    }

    private class MockHttpRequest extends HttpRequest implements EnhancedInstance {
        public MockHttpRequest(HttpClient httpClient, URI uri) {
            super(httpClient, uri);
        }

        @Override public Object getSkyWalkingDynamicField() {
            return null;
        }

        @Override public void setSkyWalkingDynamicField(Object value) {

        }

        @Override public HttpMethod getMethod() {
            return HttpMethod.POST;
        }

        @Override public URI getURI() {
            return uri;
        }
    }
}
