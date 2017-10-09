package org.skywalking.apm.toolkit.activation.trace;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.skywalking.apm.agent.core.plugin.interceptor.StaticMethodsInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassStaticMethodsEnhancePluginDefine;
import org.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * Active the toolkit class "org.skywalking.apm.toolkit.trace.TraceContext".
 * Should not dependency or import any class in "skywalking-toolkit-trace-context" module.
 * Activation's classloader is diff from "org.skywalking.apm.toolkit.trace.TraceContext",
 * using direct will trigger classloader issue.
 * <p>
 * Created by xin on 2016/12/15.
 */
public class TraceContextActivation extends ClassStaticMethodsEnhancePluginDefine {
    /**
     * @return the target class, which needs active.
     */
    @Override
    protected ClassMatch enhanceClass() {
        return byName("org.skywalking.apm.toolkit.trace.TraceContext");
    }

    /**
     * @return the collection of {@link StaticMethodsInterceptPoint}, represent the intercepted methods and their
     * interceptors.
     */
    @Override
    protected StaticMethodsInterceptPoint[] getStaticMethodsInterceptPoints() {
        return new StaticMethodsInterceptPoint[] {
            new StaticMethodsInterceptPoint() {
                @Override
                public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named("traceId");
                }

                @Override
                public String getMethodsInterceptor() {
                    return "org.skywalking.apm.toolkit.activation.trace.TraceContextInterceptor";
                }

                @Override public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }
}
