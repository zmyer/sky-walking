package org.skywalking.apm.plugin.spring.cloud.netflix.feign.v11.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * {@link NetflixFeignInstrumentation} presents that skywalking intercepts {@link org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient#execute(feign.Request,
 * feign.Request.Options)} by using {@link org.skywalking.apm.plugin.feign.http.v9.DefaultHttpClientInterceptor}.
 *
 * @author zhangxin
 */
public class NetflixFeignInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {
    /**
     * Enhance class.
     */
    private static final String ENHANCE_CLASS = "org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient";

    /**
     * Intercept class.
     */
    private static final String INTERCEPT_CLASS = "org.skywalking.apm.plugin.feign.http.v9.DefaultHttpClientInterceptor";

    @Override protected ClassMatch enhanceClass() {
        return byName(ENHANCE_CLASS);
    }

    @Override protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
            new InstanceMethodsInterceptPoint() {
                @Override public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("execute");
                }

                @Override public String getMethodsInterceptor() {
                    return INTERCEPT_CLASS;
                }

                @Override public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }
}
