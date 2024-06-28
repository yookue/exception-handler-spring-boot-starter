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


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.groovy.GroovyMarkupView;
import org.springframework.web.servlet.view.groovy.GroovyMarkupViewResolver;
import com.yookue.commonplexus.javaseutil.constant.AssertMessageConst;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


/**
 * {@link org.springframework.web.servlet.HandlerExceptionResolver} of groovy for exception handler
 *
 * @author David Hsing
 * @see org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration
 * @see org.springframework.web.servlet.view.groovy.GroovyMarkupView
 * @see org.springframework.web.servlet.view.groovy.GroovyMarkupViewResolver
 */
@RequiredArgsConstructor
@Getter(value = AccessLevel.PROTECTED)
public class GroovyFilterExceptionResolver extends DefaultFilterExceptionResolver {
    private final GroovyMarkupViewResolver viewResolver;

    @Override
    @SneakyThrows
    protected void resolveHtmlInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nullable Object handler, @Nonnull Exception cause, @Nullable ModelAndView view) {
        HttpStatus status = super.determineErrorStatus(request, (view == null ? null : view.getStatus()), cause);
        response.setStatus(status.value());
        if (view != null && StringUtils.isNotBlank(view.getViewName())) {
            GroovyMarkupView resolvedView = (GroovyMarkupView) viewResolver.resolveViewName(view.getViewName(), LocaleContextHolder.getLocale());
            Assert.notNull(resolvedView, AssertMessageConst.NOT_NULL);
            resolvedView.render(view.getModel(), request, response);
        }
    }
}
