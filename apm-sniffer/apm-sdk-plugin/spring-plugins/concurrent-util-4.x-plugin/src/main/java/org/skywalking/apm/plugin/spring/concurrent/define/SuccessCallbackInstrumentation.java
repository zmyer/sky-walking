package org.skywalking.apm.plugin.spring.concurrent.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.skywalking.apm.plugin.spring.concurrent.match.SuccessCallbackMatch.successCallbackMatch;

/**
 * {@link SuccessCallbackInstrumentation} enhance the <code>onSuccess</code> method that class inherited
 * <code>org.springframework.util.concurrent.SuccessCallback</code> by <code>org.skywalking.apm.plugin.spring.concurrent.SuccessCallbackInterceptor</code>.
 *
 * @author zhangxin
 */
public class SuccessCallbackInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    public static final String SUCCESS_CALLBACK_INTERCEPTOR =
        "org.skywalking.apm.plugin.spring.concurrent.SuccessCallbackInterceptor";
    public static final String SUCCESS_METHOD_NAME = "onSuccess";

    @Override protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
            new InstanceMethodsInterceptPoint() {
                @Override public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named(SUCCESS_METHOD_NAME);
                }

                @Override public String getMethodsInterceptor() {
                    return SUCCESS_CALLBACK_INTERCEPTOR;
                }

                @Override public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }

    @Override protected ClassMatch enhanceClass() {
        return successCallbackMatch();
    }
}
