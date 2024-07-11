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


import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.servlet.view.MustacheView;
import org.springframework.boot.web.servlet.view.MustacheViewResolver;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import com.yookue.commonplexus.javaseutil.constant.AssertMessageConst;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


/**
 * {@link org.springframework.web.servlet.HandlerExceptionResolver} of mustache for exception handler
 *
 * @author David Hsing
 * @see org.springframework.boot.autoconfigure.mustache.MustacheAutoConfiguration
 * @see org.springframework.boot.web.servlet.view.MustacheView
 * @see org.springframework.boot.web.servlet.view.MustacheViewResolver
 */
@RequiredArgsConstructor
@Getter(value = AccessLevel.PROTECTED)
public class MustacheFilterExceptionResolver extends DefaultFilterExceptionResolver {
    private final MustacheViewResolver viewResolver;

    @Override
    @SneakyThrows
    protected void resolveHtmlInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nullable Object handler, @Nonnull Exception cause, @Nullable ModelAndView view) {
        HttpStatusCode status = super.determineErrorStatus(request, (view == null ? null : view.getStatus()), cause);
        response.setStatus(status.value());
        if (view != null && StringUtils.isNotBlank(view.getViewName())) {
            MustacheView resolvedView = (MustacheView) viewResolver.resolveViewName(view.getViewName(), LocaleContextHolder.getLocale());
            Assert.notNull(resolvedView, AssertMessageConst.NOT_NULL);
            resolvedView.render(view.getModel(), request, response);
        }
    }
}
