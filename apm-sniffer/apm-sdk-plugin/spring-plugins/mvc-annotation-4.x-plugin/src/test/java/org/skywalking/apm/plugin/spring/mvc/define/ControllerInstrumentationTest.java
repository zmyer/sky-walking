package org.skywalking.apm.plugin.spring.mvc.define;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.skywalking.apm.agent.test.tools.TracingSegmentRunner;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(TracingSegmentRunner.class)
public class ControllerInstrumentationTest {
    private ControllerInstrumentation controllerInstrumentation;

    @Before
    public void setUp() throws Exception {
        controllerInstrumentation = new ControllerInstrumentation();
    }

    @Test
    public void testGetEnhanceAnnotations() throws Throwable {
        Assert.assertArrayEquals(new String[] {ControllerInstrumentation.ENHANCE_ANNOTATION},
                        controllerInstrumentation.getEnhanceAnnotations());
    }

    @Test
    public void testGetInstanceMethodsInterceptPoints() throws Throwable {
        InstanceMethodsInterceptPoint[] methodPoints = controllerInstrumentation.getInstanceMethodsInterceptPoints();
        assertThat(methodPoints.length, is(2));
        assertThat(methodPoints[0].getMethodsInterceptor(), is("org.skywalking.apm.plugin.spring.mvc.RequestMappingMethodInterceptor"));
        assertThat(methodPoints[1].getMethodsInterceptor(), is("org.skywalking.apm.plugin.spring.mvc.RestMappingMethodInterceptor"));

        Assert.assertFalse(methodPoints[0].isOverrideArgs());
        Assert.assertFalse(methodPoints[1].isOverrideArgs());

        Assert.assertNotNull(methodPoints[0].getMethodsMatcher());
        Assert.assertNotNull(methodPoints[1].getMethodsMatcher());

    }

    @Test
    public void testGetConstructorsInterceptPoints() throws Throwable {
        ConstructorInterceptPoint[] cips = controllerInstrumentation.getConstructorsInterceptPoints();
        Assert.assertEquals(cips.length, 1);
        ConstructorInterceptPoint cip = cips[0];
        Assert.assertNotNull(cip);

        Assert.assertEquals(cip.getConstructorInterceptor(), "org.skywalking.apm.plugin.spring.mvc.ControllerConstructorInterceptor");
        Assert.assertTrue(cip.getConstructorMatcher().equals(ElementMatchers.any()));
    }
}
