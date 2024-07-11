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


import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.Assert;
import com.yookue.commonplexus.javaseutil.constant.AssertMessageConst;
import lombok.Getter;
import lombok.Setter;


/**
 * Simple error controller that inherits from the default one
 *
 * @author David Hsing
 * @see com.yookue.springstarter.exceptionhandler.controller.DefaultBasicErrorController
 */
@Getter
@Setter
@SuppressWarnings("unused")
public class SimpleBasicErrorController extends DefaultBasicErrorController {
    private String viewName;
    private boolean useLocalizedFieldName;

    public SimpleBasicErrorController(@Nonnull ErrorAttributes attributes, @Nonnull ErrorProperties properties) {
        super(attributes, properties);
    }

    public SimpleBasicErrorController(@Nonnull ErrorAttributes attributes, @Nonnull ErrorProperties properties, @Nonnull String viewName, boolean useLocalizedFieldName) {
        super(attributes, properties);
        this.viewName = viewName;
        this.useLocalizedFieldName = useLocalizedFieldName;
    }

    public SimpleBasicErrorController(@Nonnull ErrorAttributes attributes, @Nonnull ServerProperties properties) {
        super(attributes, properties.getError());
    }

    public SimpleBasicErrorController(@Nonnull ErrorAttributes attributes, @Nonnull ServerProperties properties, @Nonnull String viewName, boolean useLocalizedFieldName) {
        super(attributes, properties.getError());
        this.viewName = viewName;
        this.useLocalizedFieldName = useLocalizedFieldName;
    }

    @Override
    protected String prepareErrorView(@Nonnull HttpServletRequest request, @Nullable HttpStatusCode status, @Nullable Throwable cause) {
        Assert.hasText(viewName, AssertMessageConst.HAS_TEXT);
        return viewName;
    }

    @Override
    protected boolean useLocalizedFieldName(@Nonnull HttpServletRequest request, @Nullable HttpStatusCode status, @Nullable Throwable cause, boolean html) {
        return useLocalizedFieldName;
    }
}
