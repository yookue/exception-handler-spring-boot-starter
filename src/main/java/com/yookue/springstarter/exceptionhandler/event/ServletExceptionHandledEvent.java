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

package com.yookue.springstarter.exceptionhandler.event;


import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEvent;
import org.springframework.http.HttpStatus;
import com.yookue.commonplexus.javaseutil.util.ObjectUtilsWraps;
import lombok.Getter;


/**
 * Event when error occurred in controllers
 *
 * @author David Hsing
 * @see org.springframework.web.context.support.ServletRequestHandledEvent
 */
@Getter
@SuppressWarnings("unused")
public class ServletExceptionHandledEvent extends ApplicationEvent {
    private HttpStatus httpStatus;
    private Throwable exception;
    private Map<String, Object> errorAttributes;

    public ServletExceptionHandledEvent(@Nonnull HttpServletRequest request) {
        super(request);
    }

    /**
     * Constructs a new {@link com.yookue.springstarter.exceptionhandler.event.ServletExceptionHandledEvent} instance
     *
     * @param request the servlet request from
     * @param status the response http status
     * @param exception the exception that occurred
     */
    public ServletExceptionHandledEvent(@Nonnull HttpServletRequest request, @Nonnull HttpStatus status, @Nullable Throwable exception) {
        super(request);
        this.httpStatus = status;
        this.exception = exception;
    }

    /**
     * Constructs a new {@link com.yookue.springstarter.exceptionhandler.event.ServletExceptionHandledEvent} instance
     *
     * @param request the servlet request from
     * @param status the response http status
     * @param exception the exception that occurred
     * @param attributes the response error attributes
     */
    public ServletExceptionHandledEvent(@Nonnull HttpServletRequest request, @Nonnull HttpStatus status, @Nullable Throwable exception, @Nullable Map<String, Object> attributes) {
        super(request);
        this.httpStatus = status;
        this.exception = exception;
        this.errorAttributes = attributes;
    }

    public HttpServletRequest getServletRequest() {
        return ObjectUtilsWraps.castAs(super.getSource(), HttpServletRequest.class);
    }
}
