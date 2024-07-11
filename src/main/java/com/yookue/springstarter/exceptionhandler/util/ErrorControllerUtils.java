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

package com.yookue.springstarter.exceptionhandler.util;


import java.security.GeneralSecurityException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.BindException;
import com.yookue.commonplexus.javaseutil.exception.LawProhibitedException;
import com.yookue.commonplexus.javaseutil.exception.MaliciousAccessException;
import com.yookue.commonplexus.javaseutil.exception.ServerBusyException;
import com.yookue.commonplexus.javaseutil.exception.ServerMaintenanceException;
import com.yookue.commonplexus.javaseutil.exception.ServiceConfigException;
import com.yookue.commonplexus.javaseutil.exception.ServiceUnavailableException;
import com.yookue.commonplexus.javaseutil.exception.ServiceVersionException;
import com.yookue.commonplexus.springutil.util.ClassUtilsWraps;
import com.yookue.commonplexus.springutil.util.ErrorControllerWraps;
import com.yookue.commonplexus.springutil.util.WebUtilsWraps;
import com.yookue.springstarter.exceptionhandler.filter.FilterExceptionHandlerFilter;


/**
 * Utilities for {@link org.springframework.boot.web.servlet.error.ErrorController}
 *
 * @author David Hsing
 */
@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted", "UnusedReturnValue"})
public abstract class ErrorControllerUtils {
    public static HttpStatusCode determineErrorStatus(@Nonnull HttpServletRequest request, @Nullable HttpStatusCode status, @Nullable Throwable cause) {
        Throwable rootCause;
        if (cause == null) {
            rootCause = WebUtilsWraps.getRequestAttributeAs(request, FilterExceptionHandlerFilter.THROWABLE_ATTRIBUTE, Throwable.class);
        } else {
            rootCause = NestedExceptionUtils.getMostSpecificCause(cause);
        }
        if (rootCause != null) {
            if (rootCause instanceof BindException || rootCause instanceof ValidationException) {
                return HttpStatus.BAD_REQUEST;
            } else if (rootCause instanceof GeneralSecurityException || ClassUtilsWraps.isAssignableValue("org.springframework.security.core.AuthenticationException", rootCause)) {    // $NON-NLS-1$
                return HttpStatus.FORBIDDEN;
            } else if (rootCause instanceof MaliciousAccessException) {
                return HttpStatus.I_AM_A_TEAPOT;
            } else if (rootCause instanceof ServerBusyException) {
                return HttpStatus.TOO_MANY_REQUESTS;
            } else if (rootCause instanceof LawProhibitedException) {
                return HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS;
            } else if (rootCause instanceof ServerMaintenanceException || rootCause instanceof ServiceUnavailableException) {
                return HttpStatus.SERVICE_UNAVAILABLE;
            } else if (rootCause instanceof ServiceVersionException) {
                return HttpStatus.HTTP_VERSION_NOT_SUPPORTED;
            } else if (rootCause instanceof ServiceConfigException) {
                return HttpStatus.VARIANT_ALSO_NEGOTIATES;
            }
        }
        return (status != null) ? status : ErrorControllerWraps.getErrorStatus(request);
    }
}
