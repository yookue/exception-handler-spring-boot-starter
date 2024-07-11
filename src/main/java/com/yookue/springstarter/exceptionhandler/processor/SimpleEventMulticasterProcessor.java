/*
 * Copyright (c) 2016 Yookue Ltd. All rights reserved.
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

package com.yookue.springstarter.exceptionhandler.processor;


import jakarta.annotation.Nonnull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.Ordered;
import org.springframework.util.ErrorHandler;
import com.yookue.commonplexus.javaseutil.constant.LogMessageConst;
import com.yookue.commonplexus.springutil.util.ReflectionUtilsWraps;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} for {@link org.springframework.context.event.SimpleApplicationEventMulticaster}
 *
 * @author David Hsing
 * @see org.springframework.context.event.SimpleApplicationEventMulticaster
 */
@Getter
@Setter
@Slf4j
public class SimpleEventMulticasterProcessor implements BeanPostProcessor, Ordered {
    private int order = 0;
    private ErrorHandler errorHandler = throwable -> log.warn(LogMessageConst.EXCEPTION_OCCURRED);

    @Override
    public Object postProcessAfterInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (bean instanceof SimpleApplicationEventMulticaster instance) {
            Object handler = ReflectionUtilsWraps.getField(SimpleApplicationEventMulticaster.class, "errorHandler", true, instance);    // $NON-NLS-1$
            if (handler == null) {
                instance.setErrorHandler(errorHandler);
            }
        }
        return bean;
    }
}
