/*
 * Copyright (c) 2023 Unikue Ltd. All rights reserved.
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

package cn.unikue.springstarter.exceptionhandler.util;


import java.security.GeneralSecurityException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import cn.unikue.commonplexus.javaseutil.exception.BusinessValidationException;
import cn.unikue.commonplexus.javaseutil.exception.IgnorableException;
import cn.unikue.commonplexus.javaseutil.exception.LawProhibitedException;
import cn.unikue.commonplexus.javaseutil.exception.MaliciousAccessException;
import cn.unikue.commonplexus.javaseutil.exception.ServerBusyException;
import cn.unikue.commonplexus.javaseutil.exception.ServerMaintenanceException;
import cn.unikue.commonplexus.javaseutil.exception.ServiceConfigException;
import cn.unikue.commonplexus.javaseutil.exception.ServiceUnavailableException;
import cn.unikue.commonplexus.javaseutil.exception.ServiceVersionException;
import cn.unikue.commonplexus.springutil.util.ClassUtilsWraps;
import cn.unikue.commonplexus.springutil.util.ErrorControllerWraps;
import cn.unikue.commonplexus.springutil.util.WebUtilsWraps;
import cn.unikue.springstarter.exceptionhandler.filter.FilterExceptionHandlerFilter;


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
            if (rootCause instanceof IgnorableException) {
                return HttpStatus.OK;
            } else if (rootCause instanceof BindException || rootCause instanceof ValidationException || rootCause instanceof BusinessValidationException) {
                return HttpStatus.BAD_REQUEST;
            } else if (rootCause instanceof GeneralSecurityException || ClassUtilsWraps.isAssignableValue("org.springframework.security.core.AuthenticationException", rootCause)) {    // $NON-NLS-1$
                return HttpStatus.FORBIDDEN;
            } else if (rootCause instanceof NoResourceFoundException) {
                return HttpStatus.NOT_FOUND;
            } else if (rootCause instanceof MaliciousAccessException) {
                return HttpStatus.I_AM_A_TEAPOT;
            } else if (rootCause instanceof ServerBusyException || ClassUtilsWraps.isAssignableValue("cn.unikue.springstarter.ratelimiter.exception.RateLimitedException", rootCause)) {
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
