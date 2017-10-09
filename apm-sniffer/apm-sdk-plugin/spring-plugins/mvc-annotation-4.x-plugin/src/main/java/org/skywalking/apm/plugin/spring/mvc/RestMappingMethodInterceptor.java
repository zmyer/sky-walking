package org.skywalking.apm.plugin.spring.mvc;

import java.lang.reflect.Method;

import org.springframework.web.bind.annotation.*;

/**
 * The <code>RestMappingMethodInterceptor</code> only use the first mapping value.
 * it will inteceptor with
 * <code>@GetMapping</code>, <code>@PostMapping</code>, <code>@PutMapping</code>
 * <code>@DeleteMapping</code>, <code>@PatchMapping</code>
 * @author clevertension
 */
public class RestMappingMethodInterceptor extends AbstractMethodInteceptor {
    @Override
    public String getRequestURL(Method method) {
        String requestURL = "";
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        PutMapping putMapping = method.getAnnotation(PutMapping.class);
        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
        if (getMapping != null) {
            if (getMapping.value().length > 0) {
                requestURL = getMapping.value()[0];
            } else if (getMapping.path().length > 0) {
                requestURL = getMapping.path()[0];
            }
        } else if (postMapping != null) {
            if (postMapping.value().length > 0) {
                requestURL = postMapping.value()[0];
            } else if (postMapping.path().length > 0) {
                requestURL = postMapping.path()[0];
            }
        } else if (putMapping != null) {
            if (putMapping.value().length > 0) {
                requestURL = putMapping.value()[0];
            } else if (putMapping.path().length > 0) {
                requestURL = putMapping.path()[0];
            }
        } else if (deleteMapping != null) {
            if (deleteMapping.value().length > 0) {
                requestURL = deleteMapping.value()[0];
            } else if (deleteMapping.path().length > 0) {
                requestURL = deleteMapping.path()[0];
            }
        } else if (patchMapping != null) {
            if (patchMapping.value().length > 0) {
                requestURL = patchMapping.value()[0];
            } else if (patchMapping.path().length > 0) {
                requestURL = patchMapping.path()[0];
            }
        }
        return requestURL;
    }
}
