package org.skywalking.apm.plugin.jetty.v9.client.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;
import static org.skywalking.apm.agent.core.plugin.bytebuddy.ArgumentTypeNameMatch.takesArgumentWithType;
import static org.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * {@link HttpRequestInstrumentation} enhance the <code>send</code> method without argument in
 * <code>org.eclipse.jetty.client.HttpRequest</code> by <code>org.skywalking.apm.plugin.jetty.client.SyncHttpRequestSendInterceptor</code>
 * and enhance the <code>send</code> with <code>org.eclipse.jetty.client.api.Response$CompleteListener</code> parameter
 * by <code>org.skywalking.apm.plugin.jetty.client.AsyncHttpRequestSendInterceptor</code>
 *
 * @author zhangxin
 */
public class HttpRequestInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "org.eclipse.jetty.client.HttpRequest";
    private static final String ENHANCE_CLASS_NAME = "send";
    public static final String ASYNC_SEND_INTERCEPTOR = "org.skywalking.apm.plugin.jetty.client.AsyncHttpRequestSendInterceptor";
    public static final String SYNC_SEND_INTERCEPTOR = "org.skywalking.apm.plugin.jetty.client.SyncHttpRequestSendInterceptor";

    @Override protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
            new InstanceMethodsInterceptPoint() {
                //sync call interceptor point
                @Override public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named(ENHANCE_CLASS_NAME).and(takesArguments(0));
                }

                @Override public String getMethodsInterceptor() {
                    return SYNC_SEND_INTERCEPTOR;
                }

                @Override public boolean isOverrideArgs() {
                    return false;
                }
            },
            new InstanceMethodsInterceptPoint() {
                //async call interceptor point
                @Override public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named(ENHANCE_CLASS_NAME).and(takesArgumentWithType(0, "org.eclipse.jetty.client.api.Response$CompleteListener"));
                }

                @Override public String getMethodsInterceptor() {
                    return ASYNC_SEND_INTERCEPTOR;
                }

                @Override public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }

    @Override protected ClassMatch enhanceClass() {
        return byName(ENHANCE_CLASS);
    }
}
