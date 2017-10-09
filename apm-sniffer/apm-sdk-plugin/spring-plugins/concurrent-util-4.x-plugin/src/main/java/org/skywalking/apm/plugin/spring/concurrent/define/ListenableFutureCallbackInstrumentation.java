package org.skywalking.apm.plugin.spring.concurrent.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.skywalking.apm.plugin.spring.concurrent.match.ListenableFutureCallbackMatch.listenableFutureCallbackMatch;

/**
 * {@link ListenableFutureCallbackInstrumentation} enhance <code>onSuccess</code> method and <code>oonFailure</code>
 * that class inherited <code>org.springframework.util.concurrent.ListenableFutureCallback</code> by
 * <code>org.skywalking.apm.plugin.spring.concurrent.SuccessCallbackInterceptor</code> and
 * <code>org.skywalking.apm.plugin.spring.concurrent.FailureCallbackInterceptor</code>.
 *
 * @author zhangxin
 */
public class ListenableFutureCallbackInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    @Override
    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
            new InstanceMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named(SuccessCallbackInstrumentation.SUCCESS_METHOD_NAME);
                }

                @Override
                public String getMethodsInterceptor() {
                    return SuccessCallbackInstrumentation.SUCCESS_CALLBACK_INTERCEPTOR;
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            },
            new InstanceMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named(FailureCallbackInstrumentation.FAILURE_METHOD_NAME);
                }

                @Override
                public String getMethodsInterceptor() {
                    return FailureCallbackInstrumentation.FAILURE_CALLBACK_INTERCEPTOR;
                }

                @Override
                public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }

    @Override
    protected ClassMatch enhanceClass() {
        return listenableFutureCallbackMatch();
    }
}
