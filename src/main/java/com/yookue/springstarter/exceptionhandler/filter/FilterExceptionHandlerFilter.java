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

package com.yookue.springstarter.exceptionhandler.filter;


import java.lang.reflect.Method;
import java.util.Locale;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import com.yookue.commonplexus.javaseutil.constant.LogMessageConst;
import com.yookue.commonplexus.javaseutil.util.ExceptionUtilsWraps;
import com.yookue.commonplexus.springutil.util.AntPathWraps;
import com.yookue.commonplexus.springutil.util.UriUtilsWraps;
import com.yookue.commonplexus.springutil.util.WebUtilsWraps;
import com.yookue.springstarter.exceptionhandler.property.ExceptionHandlerProperties;
import lombok.extern.slf4j.Slf4j;


/**
 * {@link jakarta.servlet.Filter} for exception handler
 *
 * @author David Hsing
 * @see org.springframework.web.filter.OncePerRequestFilter
 */
@Slf4j
public class FilterExceptionHandlerFilter extends OncePerRequestFilter {
    public static final String THROWABLE_ATTRIBUTE = FilterExceptionHandlerFilter.class.getName() + ".THROWABLE";    // $NON-NLS-1$
    private final ExceptionHandlerProperties handlerProperties;
    private final HandlerExceptionResolver handlerResolver;
    private HandlerMethod handlerMethod;

    public FilterExceptionHandlerFilter(@Nonnull ExceptionHandlerProperties properties, @Nonnull HandlerExceptionResolver resolver) {
        this.handlerProperties = properties;
        this.handlerResolver = resolver;
        Method method = ReflectionUtils.findMethod(FilterExceptionHandlerFilter.class, "doFilterInternal", HttpServletRequest.class, HttpServletResponse.class, FilterChain.class);    // $NON-NLS-1$
        if (method != null) {
            handlerMethod = new HandlerMethod(this, method);
        }
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain chain) {
        try {
            Locale locale = null;
            ExceptionHandlerProperties.LocaleChange props = handlerProperties.getLocaleChange();
            if (StringUtils.isNotBlank(props.getSessionName())) {
                locale = WebUtilsWraps.getLocaleFromSession(request, props.getSessionName());
            }
            if (locale == null && StringUtils.isNotBlank(props.getCookieName())) {
                locale = WebUtilsWraps.getLocaleFromCookie(request, props.getCookieName());
            }
            if (locale != null) {
                LocaleContextHolder.setLocale(locale, true);
            }
        } catch (Exception ignored) {
        }
        try {
            chain.doFilter(request, response);
        } catch (Exception ex) {
            Exception cause = ExceptionUtilsWraps.getRootCauseAsException(ex, ex);
            if (log.isDebugEnabled()) {
                log.debug(LogMessageConst.EXCEPTION_OCCURRED, cause);
            }
            request.setAttribute(THROWABLE_ATTRIBUTE, cause);
            try {
                handlerResolver.resolveException(request, response, handlerMethod, cause);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) throws ServletException {
        if (!CollectionUtils.isEmpty(handlerProperties.getExceptionFilter().getExcludePaths())) {
            String servletPath = UriUtilsWraps.getServletPath(request);
            return AntPathWraps.matchAnyPatterns(servletPath, handlerProperties.getExceptionFilter().getExcludePaths());
        }
        return super.shouldNotFilter(request);
    }
}
