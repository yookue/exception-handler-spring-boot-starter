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

package com.yookue.springstarter.exceptionhandler.property;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerInterceptor;
import com.yookue.springstarter.exceptionhandler.config.ExceptionHandlerAutoConfiguration;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


/**
 * Properties for exception handler
 *
 * @author David Hsing
 */
@ConfigurationProperties(prefix = ExceptionHandlerAutoConfiguration.PROPERTIES_PREFIX)
@Getter
@Setter
@ToString
public class ExceptionHandlerProperties implements Serializable {
    /**
     * Indicates whether to enable this starter or not
     * <p>
     * Default is {@code true}
     */
    private Boolean enabled = true;

    /**
     * Handle exception filter attributes
     */
    private final ExceptionFilter exceptionFilter = new ExceptionFilter();

    /**
     * Handle exception resolver attributes
     */
    private final ExceptionResolver exceptionResolver = new ExceptionResolver();

    /**
     * Locale change attributes
     */
    private final LocaleChange localeChange = new LocaleChange();

    /**
     * Handle application event listener attributes
     */
    private final EventListener eventListener = new EventListener();


    /**
     * Properties for handle exception filter
     *
     * @author David Hsing
     * @see com.yookue.springstarter.exceptionhandler.filter.FilterExceptionHandlerFilter
     */
    @Getter
    @Setter
    @ToString
    public static class ExceptionFilter implements Serializable {
        /**
         * The priority order of the filter
         * <p>
         * Default is {@code Ordered.HIGHEST_PRECEDENCE + 10}
         */
        private Integer filerOrder = Ordered.HIGHEST_PRECEDENCE + 10;

        /**
         * The url patterns of the filter
         */
        private Set<String> filterPaths;

        /**
         * The init parameters of the filter
         */
        private Map<String, String> filterParams;

        /**
         * The url patterns that ignored by the filter
         */
        private Set<String> excludePaths;
    }


    /**
     * Properties for handle exception resolver
     *
     * @author David Hsing
     * @see com.yookue.springstarter.exceptionhandler.resolver.AbstractFilterExceptionResolver
     */
    @Getter
    @Setter
    @ToString
    public static class ExceptionResolver implements Serializable {
        /**
         * The interceptor classes that are allowed to be invoked by the error resolver
         *
         * @see com.yookue.commonplexus.springutil.annotation.ExceptionHandlerInvokable
         */
        private List<Class<? extends HandlerInterceptor>> invokableInterceptors;

        /**
         * The priority order of the resolver
         * <p>
         * Default is {@code Ordered.HIGHEST_PRECEDENCE + 10}
         */
        private Integer resolverOrder = Ordered.HIGHEST_PRECEDENCE + 10;
    }


    /**
     * Properties for locale change
     * <p>
     * Used to detect the request locale for displaying multilingual errors
     *
     * @author David Hsing
     * @reference "https://github.com/yookueltd/locale-change-spring-boot-starter"
     */
    @Getter
    @Setter
    @ToString
    @SuppressWarnings({"JavadocDeclaration", "JavadocLinkAsPlainText"})
    public static class LocaleChange implements Serializable {
        /**
         * Specifies a name for the cookie
         */
        private String cookieName;

        /**
         * Specifies a name for the session
         */
        private String sessionName;
    }


    /**
     * Properties for handling application event listener
     *
     * @author David Hsing
     */
    @Getter
    @Setter
    @ToString
    public static class EventListener implements Serializable {
        /**
         * Indicates whether to enable proxy a {@link org.springframework.context.event.SimpleApplicationEventMulticaster} or not
         * <p>
         * Default is {@code true}
         */
        private Boolean handleEventMulticaster = true;

        /**
         * The priority order of processor that processes event multicaster processor
         * <p>
         * Default is {@code Ordered.LOWEST_PRECEDENCE - 1000}
         */
        private Integer processorOrder = Ordered.LOWEST_PRECEDENCE - 1000;
    }
}
