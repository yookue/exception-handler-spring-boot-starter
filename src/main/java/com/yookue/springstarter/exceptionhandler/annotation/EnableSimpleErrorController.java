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

package com.yookue.springstarter.exceptionhandler.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.yookue.springstarter.exceptionhandler.registrar.SimpleErrorControllerRegistrar;


/**
 * Annotation that allows a simple error controller
 *
 * @author David Hsing
 * @see com.yookue.springstarter.exceptionhandler.controller.SimpleBasicErrorController
 * @see com.yookue.springstarter.exceptionhandler.registrar.SimpleErrorControllerRegistrar
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
@Configuration(proxyBeanMethods = false)
@Import(value = SimpleErrorControllerRegistrar.class)
@SuppressWarnings("unused")
public @interface EnableSimpleErrorController {
    /**
     * Returns the view name for template engine to process in the error controller
     * <p>
     * If using groovy template, the {@code viewName} can't starts with slash if the {@code resourceLoaderPath} already ends with slash
     *
     * @return the view name for template engine to process in the error controller
     */
    String viewName();

    /**
     * Returns whether to use the localized field name in the error controller or not
     *
     * @return whether to use the localized field name in the error controller or not
     */
    boolean useLocalizedFieldName() default false;

    /**
     * Returns whether to publish a {@link com.yookue.springstarter.exceptionhandler.event.ServletExceptionHandledEvent} on exception or not
     *
     * @return whether to publish a {@link com.yookue.springstarter.exceptionhandler.event.ServletExceptionHandledEvent} on exception or not
     */
    boolean publishEvent() default true;
}
