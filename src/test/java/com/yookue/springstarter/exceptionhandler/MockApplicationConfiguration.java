/*
 * Copyright (c) 2020 Yookue Ltd. All rights reserved.
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

package com.yookue.springstarter.exceptionhandler;


import java.nio.charset.StandardCharsets;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import com.yookue.springstarter.exceptionhandler.annotation.EnableSimpleErrorController;


/**
 * @see org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration
 * @see org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration
 */
@TestConfiguration(proxyBeanMethods = false)
@EnableSimpleErrorController(viewName = "mock-error")
class MockApplicationConfiguration {
    @Bean
    public MockMvcBuilderCustomizer mockMvcResponseCustomizer() {
        return builder -> builder.defaultResponseCharacterEncoding(StandardCharsets.UTF_8);
    }
}
