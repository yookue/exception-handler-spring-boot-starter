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


import org.mockito.exceptions.base.MockitoException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.yookue.commonplexus.javaseutil.exception.MaliciousAccessException;


@Controller
@SuppressWarnings("unused")
class MockApplicationController {
    @GetMapping(path = "/mock-418")
    public ModelAndView mock418() {
        throw new MaliciousAccessException("Don't worry, this is a mock message");
    }

    @PostMapping(path = "/mock-500")
    @ResponseBody
    public ResponseEntity<?> mock500() {
        throw new MockitoException("Don't worry, this is a mock message");
    }
}
