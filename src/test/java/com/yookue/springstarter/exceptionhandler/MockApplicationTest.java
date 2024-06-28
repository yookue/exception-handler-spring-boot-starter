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


import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.yookue.commonplexus.javaseutil.constant.HttpHeaderConst;
import com.yookue.commonplexus.javaseutil.util.StackTraceWraps;
import lombok.extern.slf4j.Slf4j;


@SpringBootTest(classes = MockApplicationInitializer.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(value = MockApplicationConfiguration.class)
@AutoConfigureMockMvc
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@Slf4j
@SuppressWarnings("unused")
abstract class MockApplicationTest {
    @Autowired
    private MockMvc mockMvc;

    @LocalServerPort
    private Integer serverPort;

    @BeforeAll
    void beforeAll() {
        String methodName = StackTraceWraps.getExecutingMethodName();
        log.info("{}: Server port: {}", methodName, serverPort);
    }

    @Test
    void error418() throws Exception {
        String methodName = StackTraceWraps.getExecutingMethodName();
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get(URI.create("/mock-418"));    // $NON-NLS-1$
        MvcResult result = mockMvc.perform(builder).andExpect(MockMvcResultMatchers.status().isIAmATeapot()).andReturn();
        String content = result.getResponse().getContentAsString();
        log.info("{}: Response:{}{}", methodName, StringUtils.repeat(System.lineSeparator(), 2), content);
        Assertions.assertNotNull(content);
    }

    @Test
    void error500() throws Exception {
        String methodName = StackTraceWraps.getExecutingMethodName();
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(URI.create("/mock-500")).header(HttpHeaderConst.X_REQUESTED_WITH, HttpHeaderConst.XML_HTTP_REQUEST);    // $NON-NLS-1$
        MvcResult result = mockMvc.perform(builder).andExpect(MockMvcResultMatchers.status().isInternalServerError()).andReturn();
        String content = result.getResponse().getContentAsString();
        log.info("{}: Response:{}{}", methodName, StringUtils.repeat(System.lineSeparator(), 2), content);
        Assertions.assertNotNull(content);
    }
}
