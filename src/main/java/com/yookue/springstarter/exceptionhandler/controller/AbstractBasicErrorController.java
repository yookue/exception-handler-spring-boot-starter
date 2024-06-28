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

package com.yookue.springstarter.exceptionhandler.controller;


import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.ModelAndView;
import com.yookue.commonplexus.javaseutil.constant.StringVariantConst;
import com.yookue.commonplexus.javaseutil.util.MapPlainWraps;
import com.yookue.commonplexus.javaseutil.util.StringUtilsWraps;
import com.yookue.commonplexus.springutil.constant.ErrorAttributeCombo;
import com.yookue.commonplexus.springutil.constant.ErrorAttributeConst;
import com.yookue.commonplexus.springutil.constant.MiscMessageConst;
import com.yookue.commonplexus.springutil.util.ErrorControllerWraps;
import com.yookue.commonplexus.springutil.util.MessageSourceWraps;
import com.yookue.commonplexus.springutil.util.UriUtilsWraps;
import com.yookue.commonplexus.springutil.util.WebUtilsWraps;
import com.yookue.springstarter.exceptionhandler.event.ServletExceptionHandledEvent;
import com.yookue.springstarter.exceptionhandler.filter.FilterExceptionHandlerFilter;
import com.yookue.springstarter.exceptionhandler.util.ErrorControllerUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * Abstract basic error controller for global exception handling
 * <p>
 * If using thymeleaf, please set the property "spring.thymeleaf.servlet.produce-partial-output-while-processing" to {@code false}
 *
 * @author David Hsing
 * @reference "http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-error-handling"
 * @reference "http://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc"
 * @see org.springframework.boot.web.servlet.error.ErrorController
 * @see org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController
 * @see org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
 * @see org.springframework.boot.web.error.ErrorAttributeOptions
 * @see org.springframework.boot.web.servlet.error.DefaultErrorAttributes
 */
@Slf4j
@SuppressWarnings({"JavadocDeclaration", "JavadocLinkAsPlainText"})
public abstract class AbstractBasicErrorController extends BasicErrorController implements ApplicationEventPublisherAware, EnvironmentAware, MessageSourceAware, InitializingBean {
    private static final String THYMELEAF_PROPERTIES = "spring.thymeleaf.servlet.produce-partial-output-while-processing";    // $NON-NLS-1$

    @Setter
    protected boolean publishEvent = true;

    @Setter
    protected ApplicationEventPublisher applicationEventPublisher;

    @Setter
    protected Environment environment;

    @Setter
    protected MessageSource messageSource;

    public AbstractBasicErrorController(@Nonnull ErrorAttributes attributes, @Nonnull ErrorProperties properties) {
        super(attributes, properties);
    }

    public AbstractBasicErrorController(@Nonnull ErrorAttributes attributes, @Nonnull ServerProperties properties) {
        super(attributes, properties.getError());
    }

    @Override
    public void afterPropertiesSet() {
        if (log.isWarnEnabled() && ClassUtils.isPresent("org.thymeleaf.Thymeleaf", null) && BooleanUtils.isNotFalse(environment.getProperty(THYMELEAF_PROPERTIES, Boolean.class))) {    // $NON-NLS-1$
            log.warn("Thymeleaf detected. Please set properties '{}' to 'false' for compatibility.", THYMELEAF_PROPERTIES);
        }
    }

    /**
     * Processes the html request
     * <p>
     * Probably &#64;GetMapping
     */
    @Override
    public ModelAndView errorHtml(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response) {
        Throwable cause = determineErrorCause(request);
        HttpStatus status = determineErrorStatus(request, super.getStatus(request), cause);
        handleErrorBehavior(request, response, status, cause, true);
        return new ModelAndView(prepareErrorView(request, status, cause), prepareErrorData(request, status, cause, true));
    }

    /**
     * Processes the ajax request
     * <p>
     * Probably &#64;RequestMapping(method = {RequestMethod.HEAD, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.TRACE})
     */
    @Override
    public ResponseEntity<Map<String, Object>> error(@Nonnull HttpServletRequest request) {
        Throwable cause = determineErrorCause(request);
        HttpStatus status = determineErrorStatus(request, super.getStatus(request), cause);
        handleErrorBehavior(request, null, status, cause, false);
        return (status == HttpStatus.NO_CONTENT) ? new ResponseEntity<>(status) : new ResponseEntity<>(prepareErrorData(request, status, cause, false), status);
    }

    /**
     * Returns the view name for template engine
     *
     * @param request the servlet request
     * @param status the http status that determined
     * @param cause the exception occurred, maybe {@code null} if http 404
     *
     * @return the view name for template engine
     */
    protected abstract String prepareErrorView(@Nonnull HttpServletRequest request, @Nullable HttpStatus status, @Nonnull Throwable cause);

    /**
     * Returns the view data for a html request, or the rest data for an async request
     *
     * @param request the servlet request
     * @param status the http status that determined
     * @param cause the exception occurred, maybe {@code null} if http 404
     * @param html indicates is a html request if true, otherwise is an async request (probably ajax)
     *
     * @return the view data for a html request, or the rest data for an async request
     */
    protected abstract Map<String, Object> prepareErrorData(@Nonnull HttpServletRequest request, @Nullable HttpStatus status, @Nonnull Throwable cause, boolean html);

    /**
     * Returns the determined {@link java.lang.Throwable} of the given request
     *
     * @param request the servlet request
     *
     * @return the determined {@link java.lang.Throwable} of the given request
     */
    protected Throwable determineErrorCause(@Nonnull HttpServletRequest request) {
        Throwable result = WebUtilsWraps.getRequestAttributeAs(request, FilterExceptionHandlerFilter.THROWABLE_ATTRIBUTE, Throwable.class);
        if (result == null) {
            result = ErrorControllerWraps.getErrorCause(this, request);
        }
        return result;
    }

    /**
     * Returns the determined {@link org.springframework.http.HttpStatus} of the given request
     * <p>
     * For converting status with {@link java.lang.Throwable}
     *
     * @param request the servlet request
     * @param status the http status that determined
     * @param cause the exception occurred, maybe {@code null} if http 404
     *
     * @return the determined {@link org.springframework.http.HttpStatus} of the given request
     */
    protected HttpStatus determineErrorStatus(@Nonnull HttpServletRequest request, @Nullable HttpStatus status, @Nullable Throwable cause) {
        return ErrorControllerUtils.determineErrorStatus(request, status, cause);
    }

    /**
     * @see org.springframework.boot.web.servlet.error.DefaultErrorAttributes#getErrorAttributes(org.springframework.web.context.request.WebRequest, org.springframework.boot.web.error.ErrorAttributeOptions)
     */
    @Override
    protected Map<String, Object> getErrorAttributes(@Nonnull HttpServletRequest request, @Nonnull ErrorAttributeOptions options) {
        Throwable throwable = WebUtilsWraps.getRequestAttributeAs(request, FilterExceptionHandlerFilter.THROWABLE_ATTRIBUTE, Throwable.class);
        Map<String, Object> result = super.getErrorAttributes(request, options);
        if (throwable != null) {
            if (options.isIncluded(ErrorAttributeOptions.Include.EXCEPTION)) {
                result.computeIfAbsent(ErrorAttributeConst.EXCEPTION, element -> throwable.getClass().getName());
                if (options.isIncluded(ErrorAttributeOptions.Include.MESSAGE)) {
                    result.computeIfAbsent(ErrorAttributeConst.MESSAGE, element -> throwable.getMessage());
                }
            }
            if (options.isIncluded(ErrorAttributeOptions.Include.MESSAGE) && !result.containsKey(ErrorAttributeConst.MESSAGE)) {
                String message = MessageSourceWraps.getMessageLookup(messageSource, MiscMessageConst.SOMETHING_ERROR_TRY, LocaleContextHolder.getLocale());
                StringUtilsWraps.ifNotBlank(message, () -> result.put(ErrorAttributeConst.MESSAGE, message));
            }
        }
        if (MapPlainWraps.containsKeyValue(result, ErrorAttributeConst.STATUS, 999)) {
            // Response status node
            Throwable cause = determineErrorCause(request);
            HttpStatus status = ObjectUtils.defaultIfNull(determineErrorStatus(request, super.getStatus(request), cause), HttpStatus.INTERNAL_SERVER_ERROR);
            result.put(ErrorAttributeConst.STATUS, status.value());
            // Response error node
            String error = MapPlainWraps.getString(result, ErrorAttributeConst.ERROR);
            if (StringUtils.isBlank(error) || StringUtils.equalsIgnoreCase(error, StringVariantConst.NONE)) {
                result.put(ErrorAttributeConst.ERROR, status.getReasonPhrase());
            }
        }
        return result;
    }

    private void handleErrorBehavior(@Nonnull HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable HttpStatus status, @Nonnull Throwable cause, boolean html) {
        HttpStatus httpStatus = (status != null) ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        if (publishEvent) {
            Map<String, Object> errors = getErrorAttributes(request, ErrorAttributeCombo.ALL_OPTIONS);
            applicationEventPublisher.publishEvent(new ServletExceptionHandledEvent(request, httpStatus, cause, errors));
        }
        ErrorAttributeOptions options = super.getErrorAttributeOptions(request, html ? MediaType.TEXT_HTML : MediaType.ALL);
        Map<String, Object> attributes = getErrorAttributes(request, options);
        if (response != null) {
            response.setStatus(httpStatus.value());
        }
        if (log.isErrorEnabled()) {
            String path = MapPlainWraps.getString(attributes, ErrorAttributeConst.PATH);
            if (StringUtils.isBlank(path)) {
                path = UriUtilsWraps.getRequestUriQueryString(request);
            }
            String reason = MapPlainWraps.getString(attributes, ErrorAttributeConst.ERROR);
            if (StringUtils.isBlank(reason) || StringUtils.equalsIgnoreCase(reason, StringVariantConst.NONE)) {
                reason = httpStatus.getReasonPhrase();
            }
            log.error("Error {} path '{}', status {}, reason: {}", request.getMethod(), path, httpStatus.value(), reason);
        }
    }
}
