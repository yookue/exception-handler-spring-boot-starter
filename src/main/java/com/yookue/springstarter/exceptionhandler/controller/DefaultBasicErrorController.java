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


import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import com.yookue.commonplexus.javaseutil.exception.MaliciousAccessException;
import com.yookue.commonplexus.javaseutil.structure.PureTextStruct;
import com.yookue.commonplexus.javaseutil.util.MapPlainWraps;
import com.yookue.commonplexus.javaseutil.util.UtilDateWraps;
import com.yookue.commonplexus.springutil.constant.ErrorAttributeConst;
import com.yookue.commonplexus.springutil.constant.MiscMessageConst;
import com.yookue.commonplexus.springutil.constant.ResponseBodyConst;
import com.yookue.commonplexus.springutil.util.LocaleHolderWraps;
import com.yookue.commonplexus.springutil.util.MessageSourceWraps;
import com.yookue.commonplexus.springutil.util.ValidationUtilsWraps;
import com.yookue.springstarter.exceptionhandler.facade.ErrorControllerCustomizer;
import com.yookue.springstarter.exceptionhandler.util.ErrorControllerUtils;
import lombok.AccessLevel;
import lombok.Getter;


/**
 * Default basic error controller for global exception handling
 *
 * @author David Hsing
 */
@Configurable(autowire = Autowire.BY_TYPE, dependencyCheck = true)
@Getter(value = AccessLevel.PROTECTED)
@SuppressWarnings({"unused", "SameParameterValue"})
public class DefaultBasicErrorController extends AbstractBasicErrorController {
    private static final String NO_MESSAGE_AVAILABLE = "No message available";    // $NON-NLS-1$

    @Autowired(required = false)
    private ErrorControllerCustomizer errorControllerCustomizer;

    public DefaultBasicErrorController(@Nonnull ErrorAttributes attributes, @Nonnull ErrorProperties properties) {
        super(attributes, properties);
    }

    public DefaultBasicErrorController(@Nonnull ErrorAttributes attributes, @Nonnull ServerProperties properties) {
        super(attributes, properties.getError());
    }

    @Override
    protected String prepareErrorView(@Nonnull HttpServletRequest request, @Nullable HttpStatusCode status, @Nullable Throwable cause) {
        return errorControllerCustomizer == null ? null : errorControllerCustomizer.prepareErrorView(request, status, cause);
    }

    @Override
    protected Map<String, Object> prepareErrorData(@Nonnull HttpServletRequest request, @Nullable HttpStatusCode status, @Nullable Throwable cause, boolean html) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (useDefaultErrorData(request, status, cause, html)) {
            result.putAll(generateDefaultData(request, status, cause, html));
        }
        if (errorControllerCustomizer != null) {
            MapPlainWraps.putAllIfAllNotNull(result, errorControllerCustomizer.prepareErrorData(request, status, cause, html));
        }
        return result;
    }

    @Override
    protected HttpStatusCode determineErrorStatus(@Nonnull HttpServletRequest request, @Nullable HttpStatusCode status, @Nullable Throwable cause) {
        if (errorControllerCustomizer != null) {
            return errorControllerCustomizer.determineErrorStatus(request, status, cause);
        }
        return ErrorControllerUtils.determineErrorStatus(request, status, cause);
    }

    protected boolean useDefaultErrorData(@Nonnull HttpServletRequest request, @Nullable HttpStatusCode status, @Nullable Throwable cause, boolean html) {
        return errorControllerCustomizer == null || errorControllerCustomizer.useDefaultErrorData(request, status, cause, html);
    }

    protected boolean useLocalizedFieldName(@Nonnull HttpServletRequest request, @Nullable HttpStatusCode status, @Nullable Throwable cause, boolean html) {
        return errorControllerCustomizer != null && errorControllerCustomizer.useLocalizedFieldName(request, status, cause, html);
    }

    @Nonnull
    @SuppressWarnings("DataFlowIssue")
    private Map<String, Object> generateDefaultData(@Nonnull HttpServletRequest request, @Nullable HttpStatusCode status, @Nullable Throwable cause, boolean html) {
        Map<String, Object> result = new LinkedHashMap<>();
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(cause);
        ErrorAttributeOptions options = super.getErrorAttributeOptions(request, html ? MediaType.TEXT_HTML : MediaType.ALL);
        Map<String, Object> attributes = super.getErrorAttributes(request, options);
        Date timestamp = MapPlainWraps.getUtilDate(attributes, ErrorAttributeConst.TIMESTAMP, UtilDateWraps.getCurrentDateTime());
        result.put(html ? ResponseBodyConst.HTML_STATUS : ResponseBodyConst.REST_STATUS, status.value());
        if (html) {
            String reason = (status instanceof HttpStatus instance) ? instance.getReasonPhrase() : null;
            String phrase = MessageSourceWraps.getMessageLookup(super.messageSource, "HttpStatus." + status.value(), null, reason, LocaleContextHolder.getLocale());    // $NON-NLS-1$
            result.put(ResponseBodyConst.HTML_PHRASE, phrase);
        }
        String rootMessage = null;
        if (rootCause instanceof ValidationException) {
            rootMessage = rootCause.getMessage();
        } else if (rootCause instanceof BindException) {
            BindingResult binding = ((BindException) rootCause).getBindingResult();
            List<String> reasons;
            if (useLocalizedFieldName(request, status, rootCause, html)) {
                reasons = ValidationUtilsWraps.formatReasons(binding.getAllErrors(), element -> {
                    String fieldName = MessageSourceWraps.getMessageLookup(super.messageSource, element.getField(), null, element.getField(), LocaleContextHolder.getLocale());
                    return StringUtils.join(fieldName, LocaleHolderWraps.getOptionalSpace(), element.getDefaultMessage());
                });
            } else {
                reasons = ValidationUtilsWraps.formatReasons(binding.getAllErrors());
            }
            PureTextStruct struct = new PureTextStruct(reasons);
            rootMessage = struct.getCompositeTextOrdering(StringUtils.SPACE);
        } else if (rootCause instanceof MaliciousAccessException) {
            rootMessage = MessageSourceWraps.getMessageLookup(super.messageSource, MiscMessageConst.MALICIOUS_ACCESS_LOG, null, rootCause.getMessage(), LocaleContextHolder.getLocale());
        } else {
            if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
                String placeholder = options.isIncluded(ErrorAttributeOptions.Include.EXCEPTION) ? rootCause.getMessage() : null;
                rootMessage = MessageSourceWraps.getMessageLookup(super.messageSource, MiscMessageConst.SERVER_ERROR_TRY, null, placeholder, LocaleContextHolder.getLocale());
            }
        }
        if (StringUtils.isNotBlank(rootMessage)) {
            result.put(html ? ResponseBodyConst.HTML_MESSAGE : ResponseBodyConst.REST_MESSAGE, rootMessage);
        }
        if (!result.containsKey(html ? ResponseBodyConst.HTML_MESSAGE : ResponseBodyConst.REST_MESSAGE)) {
            String attrMessage = MapPlainWraps.getString(attributes, ErrorAttributeConst.MESSAGE);
            if (StringUtils.isNotBlank(attrMessage) && !StringUtils.equalsIgnoreCase(attrMessage, NO_MESSAGE_AVAILABLE)) {
                result.put(html ? ResponseBodyConst.HTML_MESSAGE : ResponseBodyConst.REST_MESSAGE, attrMessage);
            }
        }
        if (options.isIncluded(ErrorAttributeOptions.Include.STACK_TRACE)) {
            MapPlainWraps.removeByKeys(attributes, ErrorAttributeConst.STATUS, ErrorAttributeConst.TIMESTAMP);
            MapPlainWraps.removeIf(attributes, (key, value) -> StringUtils.equals(key, ErrorAttributeConst.MESSAGE) && value instanceof String && StringUtils.equalsIgnoreCase((String) value, NO_MESSAGE_AVAILABLE));
            attributes.computeIfAbsent(ErrorAttributeConst.TRACE, key -> ExceptionUtils.getStackTrace(rootCause));
            result.put(html ? ResponseBodyConst.HTML_DATA : ResponseBodyConst.REST_DATA, attributes);
        }
        result.put(html ? ResponseBodyConst.HTML_TIMESTAMP : ResponseBodyConst.REST_TIMESTAMP, timestamp);
        return result;
    }
}
