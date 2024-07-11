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

package com.yookue.springstarter.exceptionhandler.registrar;


import java.lang.annotation.Annotation;
import jakarta.annotation.Nonnull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import com.yookue.commonplexus.javaseutil.constant.AssertMessageConst;
import com.yookue.springstarter.exceptionhandler.annotation.EnableSimpleErrorController;
import com.yookue.springstarter.exceptionhandler.controller.SimpleBasicErrorController;


/**
 * Registrar of exception handler that allows a simple error controller
 *
 * @author David Hsing
 * @see org.springframework.context.annotation.ImportAware
 * @see org.springframework.context.annotation.ImportBeanDefinitionRegistrar
 * @see org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
 */
@AutoConfiguration(before = ErrorMvcAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@SuppressWarnings({"SpringFacetCodeInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class SimpleErrorControllerRegistrar implements ImportAware {
    private final Class<? extends Annotation> annotation = EnableSimpleErrorController.class;
    private AnnotationAttributes attributes;

    @Override
    public void setImportMetadata(@Nonnull AnnotationMetadata metadata) {
        attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annotation.getName()));
        if (attributes == null) {
            throw new IllegalArgumentException(String.format("@%s is not present on importing class: %s", annotation.getSimpleName(), metadata.getClassName()));
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorController simpleBasicErrorController(@Nonnull ErrorAttributes errors, @Nonnull ServerProperties properties) {
        Assert.notNull(attributes, AssertMessageConst.NOT_NULL);
        String viewName = attributes.getString("viewName");    // $NON-NLS-1$
        boolean useLocalizedFieldName = attributes.getBoolean("useLocalizedFieldName");    // $NON-NLS-1$
        Assert.hasText(viewName, AssertMessageConst.HAS_TEXT);
        SimpleBasicErrorController result = new SimpleBasicErrorController(errors, properties, viewName, useLocalizedFieldName);
        result.setPublishEvent(attributes.getBoolean("publishEvent"));    // $NON-NLS-1$
        return result;
    }
}
