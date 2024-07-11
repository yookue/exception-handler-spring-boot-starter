/*
 * Copyright (c) 2023 Yookue Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yookue.springstarter.exceptionhandler.resolver;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import com.yookue.commonplexus.javaseutil.constant.AssertMessageConst;
import com.yookue.commonplexus.javaseutil.util.MapPlainWraps;
import com.yookue.commonplexus.springutil.annotation.ExceptionHandlerInvokable;
import com.yookue.commonplexus.springutil.util.BeanFactoryWraps;
import com.yookue.commonplexus.springutil.util.UriUtilsWraps;
import com.yookue.commonplexus.springutil.util.WebUtilsWraps;
import com.yookue.springstarter.exceptionhandler.facade.ErrorControllerCustomizer;
import com.yookue.springstarter.exceptionhandler.property.ExceptionHandlerProperties;
import com.yookue.springstarter.exceptionhandler.util.ErrorControllerUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * {@link org.springframework.web.servlet.HandlerExceptionResolver} for exception handler
 *
 * @author David Hsing
 * @see org.springframework.web.servlet.HandlerExceptionResolver
 * @see org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver
 * @see org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver
 * @see org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration
 */
@Configurable(autowire = Autowire.BY_TYPE, dependencyCheck = true)
@Slf4j
@SuppressWarnings({"unused", "StringConcatenationArgumentToLogCall"})
public abstract class AbstractFilterExceptionResolver extends AbstractHandlerExceptionResolver implements BeanFactoryAware, InitializingBean {
    @Autowired
    protected ServerProperties serverProperties;

    @Autowired
    protected ExceptionHandlerProperties handlerProperties;

    @Autowired(required = false)
    protected ErrorControllerCustomizer errorControllerCustomizer;

    @Setter
    protected BeanFactory beanFactory;

    private BasicErrorController errorController;
    private Map<String, HandlerInterceptor> interceptorBeans;
    private final List<String> interceptedBeans = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {
        super.setWarnLogCategory(this.getClass().getName());
    }

    @Override
    protected ModelAndView doResolveException(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nullable Object handler, @Nonnull Exception cause) {
        initInterceptorBeans();
        boolean shouldIntercept = !CollectionUtils.isEmpty(interceptorBeans) && handler != null;
        if (shouldIntercept) {
            MapPlainWraps.forEach(interceptorBeans, (key, value) -> {
                try {
                    if (value.preHandle(request, response, handler)) {
                        interceptedBeans.add(key);
                    }
                } catch (Exception ex) {
                    if (log.isWarnEnabled()) {
                        log.warn("Exception occurred during executing method preHandle of " + value.getClass().getName(), ex);
                    }
                }
            });
        }
        if (WebUtilsWraps.isRestRequest(request)) {
            ResponseEntity<?> entity = getErrorController().error(request);
            if (shouldIntercept && !interceptedBeans.isEmpty()) {
                MapPlainWraps.reverseForEach(interceptorBeans, (key, value) -> {
                    try {
                        value.postHandle(request, response, handler, null);
                    } catch (Exception ex) {
                        if (log.isWarnEnabled()) {
                            log.warn("Exception occurred during executing method 'postHandle' of " + value.getClass().getName(), ex);
                        }
                    }
                }, (key, value) -> interceptedBeans.contains(key));
            }
            resolveRestInternal(request, response, handler, cause, entity);
        } else {
            ModelAndView view = getErrorController().errorHtml(request, response);
            if (shouldIntercept && !interceptedBeans.isEmpty()) {
                MapPlainWraps.reverseForEach(interceptorBeans, (key, value) -> {
                    try {
                        value.postHandle(request, response, handler, view);
                    } catch (Exception ex) {
                        if (log.isWarnEnabled()) {
                            log.warn("Exception occurred during executing method 'postHandle' of " + value.getClass().getName(), ex);
                        }
                    }
                }, (key, value) -> interceptedBeans.contains(key));
            }
            resolveHtmlInternal(request, response, handler, cause, view);
        }
        if (shouldIntercept && !interceptedBeans.isEmpty()) {
            MapPlainWraps.reverseForEach(interceptorBeans, (key, value) -> {
                try {
                    value.afterCompletion(request, response, handler, null);
                } catch (Exception ex) {
                    if (log.isWarnEnabled()) {
                        log.warn("Exception occurred during executing method 'afterCompletion' of " + value.getClass().getName(), ex);
                    }
                }
            }, (key, value) -> interceptedBeans.contains(key));
        }
        return new ModelAndView();
    }

    @Nonnull
    @Override
    protected String buildLogMessage(@Nonnull Exception ex, @Nonnull HttpServletRequest request) {
        return super.buildLogMessage(ex, request) + ", url: " + UriUtilsWraps.getRequestUrlQueryString(request);    // $NON-NLS-1$
    }

    protected void initInterceptorBeans() {
        if (interceptorBeans == null) {
            interceptorBeans = new LinkedHashMap<>();
            MapPlainWraps.putAll(interceptorBeans, BeanFactoryWraps.getBeansWithAnnotationAs(beanFactory, ExceptionHandlerInvokable.class, HandlerInterceptor.class));
            List<Class<? extends HandlerInterceptor>> interceptors = handlerProperties.getExceptionResolver().getInvokableInterceptors();
            if (!CollectionUtils.isEmpty(interceptors)) {
                for (Class<? extends HandlerInterceptor> interceptor : interceptors) {
                    MapPlainWraps.putAll(interceptorBeans, BeanFactoryWraps.getBeansOfType(beanFactory, interceptor));
                }
            }
        }
    }

    @Nonnull
    protected HttpStatusCode determineErrorStatus(@Nonnull HttpServletRequest request, @Nullable HttpStatusCode status, @Nullable Throwable cause) {
        HttpStatusCode result = ErrorControllerUtils.determineErrorStatus(request, status, cause);
        if (errorControllerCustomizer != null) {
            result = errorControllerCustomizer.determineErrorStatus(request, result, cause);
        }
        return (result != null) ? result : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Nonnull
    protected BasicErrorController getErrorController() {
        if (errorController == null) {
            errorController = BeanFactoryWraps.getBean(beanFactory, BasicErrorController.class);
        }
        Assert.notNull(errorController, AssertMessageConst.NOT_NULL);
        return errorController;
    }

    @Nonnull
    protected Charset getServletEncoding() {
        return ObjectUtils.defaultIfNull(serverProperties.getServlet().getEncoding().getCharset(), StandardCharsets.UTF_8);
    }

    protected abstract void resolveHtmlInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nullable Object handler, @Nonnull Exception cause, @Nullable ModelAndView view);

    protected abstract void resolveRestInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nullable Object handler, @Nonnull Exception cause, @Nullable ResponseEntity<?> entity);
}
