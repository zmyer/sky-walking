package org.skywalking.apm.plugin.jetty.v9.client.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.skywalking.apm.agent.core.plugin.match.HierarchyMatch.byHierarchyMatch;

/**
 * {@link CompleteListenerInstrumentation} enhance the <code>onComplete</code> method in all class of hierarchy
 * <code>org.eclipse.jetty.client.api.Response$CompleteListener</code> by <code>org.skywalking.apm.plugin.jetty.client.CompleteListenerInterceptor</code>
 *
 * @author zhangxin
 */
public class CompleteListenerInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "org.eclipse.jetty.client.api.Response$CompleteListener";
    private static final String ENHANCE_METHOD = "onComplete";
    public static final String SEND_INTERCEPTOR = "org.skywalking.apm.plugin.jetty.client.CompleteListenerInterceptor";

    @Override protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
            new InstanceMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named(ENHANCE_METHOD);
                }

                @Override
                public String getMethodsInterceptor() {
                    return SEND_INTERCEPTOR;
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }

    @Override protected ClassMatch enhanceClass() {
        return byHierarchyMatch(new String[] {ENHANCE_CLASS});
    }
}
