package org.skywalking.apm.plugin.spring.resttemplate.sync.define;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.skywalking.apm.agent.core.plugin.interceptor.ConstructorInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.InstanceMethodsInterceptPoint;
import org.skywalking.apm.agent.core.plugin.interceptor.enhance.ClassInstanceMethodsEnhancePluginDefine;
import org.skywalking.apm.agent.core.plugin.match.ClassMatch;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.skywalking.apm.agent.core.plugin.match.NameMatch.byName;

/**
 * {@link RestTemplateInstrumentation} enhance the <code>doExecute</code> method,<code>handleResponse</code> method and
 * <code>handleResponse</code> method of <code>org.springframework.web.client.RestTemplate</code> by
 * <code>org.skywalking.apm.plugin.spring.resttemplate.sync.RestExecuteInterceptor</code>,
 * <code>org.skywalking.apm.plugin.spring.resttemplate.sync.RestResponseInterceptor</code> and
 * <code>org.skywalking.apm.plugin.spring.resttemplate.sync.RestRequestInterceptor</code>.
 *
 * <code>org.skywalking.apm.plugin.spring.resttemplate.sync.RestResponseInterceptor</code> set context to  header for
 * propagate trace context after execute <code>createRequest</code>.
 *
 * @author zhangxin
 */
public class RestTemplateInstrumentation extends ClassInstanceMethodsEnhancePluginDefine {

    private static final String ENHANCE_CLASS = "org.springframework.web.client.RestTemplate";
    private static final String DO_EXECUTE_METHOD_NAME = "doExecute";
    private static final String DO_EXECUTE_INTERCEPTOR = "org.skywalking.apm.plugin.spring.resttemplate.sync.RestExecuteInterceptor";
    private static final String HANDLE_REQUEST_METHOD_NAME = "handleResponse";
    private static final String HAND_REQUEST_INTERCEPTOR = "org.skywalking.apm.plugin.spring.resttemplate.sync.RestResponseInterceptor";
    private static final String CREATE_REQUEST_METHOD_NAME = "createRequest";
    private static final String CREATE_REQUEST_INTERCEPTOR = "org.skywalking.apm.plugin.spring.resttemplate.sync.RestRequestInterceptor";

    @Override
    protected ConstructorInterceptPoint[] getConstructorsInterceptPoints() {
        return new ConstructorInterceptPoint[0];
    }

    @Override
    protected InstanceMethodsInterceptPoint[] getInstanceMethodsInterceptPoints() {
        return new InstanceMethodsInterceptPoint[] {
            new InstanceMethodsInterceptPoint() {
                @Override public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named(DO_EXECUTE_METHOD_NAME);
                }

                @Override public String getMethodsInterceptor() {
                    return DO_EXECUTE_INTERCEPTOR;
                }

                @Override public boolean isOverrideArgs() {
                    return false;
                }
            },
            new InstanceMethodsInterceptPoint() {
                @Override public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named(HANDLE_REQUEST_METHOD_NAME);
                }

                @Override public String getMethodsInterceptor() {
                    return HAND_REQUEST_INTERCEPTOR;
                }

                @Override public boolean isOverrideArgs() {
                    return false;
                }
            },
            new InstanceMethodsInterceptPoint() {
                @Override public ElementMatcher<MethodDescription> getMethodsMatcher() {
                    return named(CREATE_REQUEST_METHOD_NAME);
                }

                @Override public String getMethodsInterceptor() {
                    return CREATE_REQUEST_INTERCEPTOR;
                }

                @Override public boolean isOverrideArgs() {
                    return false;
                }
            }
        };
    }

    @Override
    protected ClassMatch enhanceClass() {
        return byName(ENHANCE_CLASS);
    }
}
