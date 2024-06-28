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

package com.yookue.springstarter.exceptionhandler.config;


import java.util.Optional;
import javax.annotation.Nonnull;
import javax.servlet.Servlet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.view.MustacheViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.springframework.web.servlet.view.groovy.GroovyMarkupViewResolver;
import org.springframework.web.servlet.view.tiles3.TilesViewResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import com.yookue.commonplexus.javaseutil.util.CollectionPlainWraps;
import com.yookue.commonplexus.javaseutil.util.MapPlainWraps;
import com.yookue.springstarter.exceptionhandler.filter.FilterExceptionHandlerFilter;
import com.yookue.springstarter.exceptionhandler.processor.SimpleEventMulticasterProcessor;
import com.yookue.springstarter.exceptionhandler.property.ExceptionHandlerProperties;
import com.yookue.springstarter.exceptionhandler.resolver.AbstractFilterExceptionResolver;
import com.yookue.springstarter.exceptionhandler.resolver.FreeMarkerFilterExceptionResolver;
import com.yookue.springstarter.exceptionhandler.resolver.GroovyFilterExceptionResolver;
import com.yookue.springstarter.exceptionhandler.resolver.InternalFilterExceptionResolver;
import com.yookue.springstarter.exceptionhandler.resolver.MustacheFilterExceptionResolver;
import com.yookue.springstarter.exceptionhandler.resolver.ThymeleafFilterExceptionResolver;
import com.yookue.springstarter.exceptionhandler.resolver.TilesFilterExceptionResolver;


/**
 * Configuration of exception handler
 *
 * @author David Hsing
 * @see org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
 * @see org.springframework.boot.web.servlet.support.ErrorPageFilter
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = ExceptionHandlerAutoConfiguration.PROPERTIES_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(value = {Servlet.class, DispatcherServlet.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureOrder(value = Ordered.LOWEST_PRECEDENCE - 10)
@Import(value = {ExceptionHandlerAutoConfiguration.Entry.class, ExceptionHandlerAutoConfiguration.Resolver.class, ExceptionHandlerAutoConfiguration.Filter.class, ExceptionHandlerAutoConfiguration.Listener.class})
public class ExceptionHandlerAutoConfiguration {
    public static final String PROPERTIES_PREFIX = "spring.exception-handler";    // $NON-NLS-1$
    public static final String EXCEPTION_RESOLVER = "filterHandlerExceptionResolver";    // $NON-NLS-1$

    @Order(value = 0)
    @EnableConfigurationProperties(value = ExceptionHandlerProperties.class)
    static class Entry {
    }


    @Order(value = 1)
    static class Resolver {
        @Bean(name = EXCEPTION_RESOLVER)
        @ConditionalOnProperty(prefix = "spring.thymeleaf", name = "enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnClass(name = "org.thymeleaf.Thymeleaf")
        @ConditionalOnBean(value = ThymeleafViewResolver.class)
        @ConditionalOnMissingBean(name = EXCEPTION_RESOLVER)
        public HandlerExceptionResolver thymeleafFilterExceptionResolver(@Nonnull ThymeleafViewResolver resolver, @Nonnull ExceptionHandlerProperties properties) {
            AbstractFilterExceptionResolver result = new ThymeleafFilterExceptionResolver(resolver);
            Optional.ofNullable(properties.getExceptionResolver().getResolverOrder()).ifPresent(result::setOrder);
            return result;
        }

        @Bean(name = EXCEPTION_RESOLVER)
        @ConditionalOnProperty(prefix = "spring.freemarker", name = "enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnClass(name = "freemarker.template.Template")
        @ConditionalOnBean(value = FreeMarkerViewResolver.class)
        @ConditionalOnMissingBean(name = EXCEPTION_RESOLVER)
        public HandlerExceptionResolver freeMarkerFilterExceptionResolver(@Nonnull FreeMarkerViewResolver resolver, @Nonnull ExceptionHandlerProperties properties) {
            AbstractFilterExceptionResolver result = new FreeMarkerFilterExceptionResolver(resolver);
            Optional.ofNullable(properties.getExceptionResolver().getResolverOrder()).ifPresent(result::setOrder);
            return result;
        }

        @Bean(name = EXCEPTION_RESOLVER)
        @ConditionalOnProperty(prefix = "spring.groovy.template", name = "enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnClass(name = {"groovy.text.Template", "org.codehaus.groovy.tools.GroovyClass"})
        @ConditionalOnBean(value = GroovyMarkupViewResolver.class)
        @ConditionalOnMissingBean(name = EXCEPTION_RESOLVER)
        public HandlerExceptionResolver groovyFilterExceptionResolver(@Nonnull GroovyMarkupViewResolver resolver, @Nonnull ExceptionHandlerProperties properties) {
            AbstractFilterExceptionResolver result = new GroovyFilterExceptionResolver(resolver);
            Optional.ofNullable(properties.getExceptionResolver().getResolverOrder()).ifPresent(result::setOrder);
            return result;
        }

        @Bean(name = EXCEPTION_RESOLVER)
        @ConditionalOnProperty(prefix = "spring.mustache", name = "enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnClass(name = "com.samskivert.mustache.Template")
        @ConditionalOnBean(value = MustacheViewResolver.class)
        @ConditionalOnMissingBean(name = EXCEPTION_RESOLVER)
        public HandlerExceptionResolver mustacheFilterExceptionResolver(@Nonnull MustacheViewResolver resolver, @Nonnull ExceptionHandlerProperties properties) {
            AbstractFilterExceptionResolver result = new MustacheFilterExceptionResolver(resolver);
            Optional.ofNullable(properties.getExceptionResolver().getResolverOrder()).ifPresent(result::setOrder);
            return result;
        }

        @Bean(name = EXCEPTION_RESOLVER)
        @ConditionalOnClass(name = "org.apache.tiles.startup.TilesInitializer")
        @ConditionalOnBean(value = TilesViewResolver.class)
        @ConditionalOnMissingBean(name = EXCEPTION_RESOLVER)
        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
        public HandlerExceptionResolver tilesFilterExceptionResolver(@Nonnull TilesViewResolver resolver, @Nonnull ExceptionHandlerProperties properties) {
            AbstractFilterExceptionResolver result = new TilesFilterExceptionResolver(resolver);
            Optional.ofNullable(properties.getExceptionResolver().getResolverOrder()).ifPresent(result::setOrder);
            return result;
        }

        @Bean(name = EXCEPTION_RESOLVER)
        @ConditionalOnProperty(prefix = "spring.mvc.view", name = "suffix")
        @ConditionalOnBean(value = InternalResourceViewResolver.class)
        @ConditionalOnMissingBean(name = EXCEPTION_RESOLVER)
        public HandlerExceptionResolver internalFilterExceptionResolver(@Nonnull InternalResourceViewResolver resolver, @Nonnull ExceptionHandlerProperties properties) {
            AbstractFilterExceptionResolver result = new InternalFilterExceptionResolver(resolver);
            Optional.ofNullable(properties.getExceptionResolver().getResolverOrder()).ifPresent(result::setOrder);
            return result;
        }
    }


    @Order(value = 2)
    static class Filter {
        @Bean
        @ConditionalOnBean(name = EXCEPTION_RESOLVER)
        @ConditionalOnMissingBean(value = FilterExceptionHandlerFilter.class, parameterizedContainer = FilterRegistrationBean.class)
        public FilterRegistrationBean<FilterExceptionHandlerFilter> filterExceptionHandlerFilterRegistration(@Nonnull ExceptionHandlerProperties properties, @Qualifier(value = EXCEPTION_RESOLVER) HandlerExceptionResolver resolver) {
            FilterExceptionHandlerFilter filter = new FilterExceptionHandlerFilter(properties, resolver);
            FilterRegistrationBean<FilterExceptionHandlerFilter> result = new FilterRegistrationBean<>(filter);
            ExceptionHandlerProperties.ExceptionFilter props = properties.getExceptionFilter();
            Optional.ofNullable(props.getFilerOrder()).ifPresent(result::setOrder);
            CollectionPlainWraps.ifNotEmpty(props.getFilterPaths(), result::setUrlPatterns);
            MapPlainWraps.ifNotEmpty(props.getFilterParams(), result::setInitParameters);
            return result;
        }
    }


    @Order(value = 3)
    static class Listener {
        @Bean
        @ConditionalOnProperty(prefix = PROPERTIES_PREFIX + ".event-listener", name = "handle-event-multicaster", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean
        public SimpleEventMulticasterProcessor simpleEventMulticasterProcessor(@Nonnull ExceptionHandlerProperties properties) {
            SimpleEventMulticasterProcessor result = new SimpleEventMulticasterProcessor();
            Optional.ofNullable(properties.getEventListener().getProcessorOrder()).ifPresent(result::setOrder);
            return result;
        }
    }
}
