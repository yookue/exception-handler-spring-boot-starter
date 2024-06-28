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

package com.yookue.springstarter.exceptionhandler.facade;


import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import com.yookue.springstarter.exceptionhandler.util.ErrorControllerUtils;


/**
 * Facade interface for customizing {@link com.yookue.springstarter.exceptionhandler.controller.DefaultBasicErrorController}
 *
 * @author David Hsing
 * @see com.yookue.springstarter.exceptionhandler.controller.DefaultBasicErrorController
 */
@SuppressWarnings("unused")
public interface ErrorControllerCustomizer {
    /**
     * Returns the view name for template engine
     *
     * @param request the servlet request
     * @param status the http status that determined
     * @param cause the exception occurred, maybe {@code null} if http 404
     *
     * @return the view name for template engine
     */
    String prepareErrorView(@Nonnull HttpServletRequest request, @Nullable HttpStatus status, @Nullable Throwable cause);

    /**
     * Returns the view data for a html request, or the rest data for an async request
     * <p>
     * if {@code useDefaultErrorData} is {@code true}, this will be appended to the generated default error data
     * <br>
     * Otherwise, this will replace the default error data totally
     *
     * @param request the servlet request
     * @param status the http status that determined
     * @param cause the exception occurred, maybe {@code null} if http 404
     * @param html indicates is a html request if true, otherwise is an async request (probably ajax)
     *
     * @return the view data for a html request, or the rest data for an async request
     */
    default Map<String, Object> prepareErrorData(@Nonnull HttpServletRequest request, @Nullable HttpStatus status, @Nullable Throwable cause, boolean html) {
        return null;
    }

    /**
     * Returns the determined {@link org.springframework.http.HttpStatus}
     * <p>
     * For converting status with {@link java.lang.Throwable}
     *
     * @param request the servlet request
     * @param status the http status that determined
     * @param cause the exception occurred, maybe {@code null} if http 404
     *
     * @return the determined {@link org.springframework.http.HttpStatus}
     */
    default HttpStatus determineErrorStatus(@Nonnull HttpServletRequest request, @Nullable HttpStatus status, @Nullable Throwable cause) {
        return ErrorControllerUtils.determineErrorStatus(request, status, cause);
    }

    /**
     * Returns whether to use the default error data in the controller or not
     *
     * @param request the servlet request
     * @param status the http status that determined
     * @param cause the exception occurred, maybe {@code null} if http 404
     * @param html indicates is a html request if true, otherwise is an async request (probably ajax)
     *
     * @return whether to use the default error data in the controller or not
     */
    default boolean useDefaultErrorData(@Nonnull HttpServletRequest request, @Nullable HttpStatus status, @Nullable Throwable cause, boolean html) {
        return true;
    }

    /**
     * Returns whether to use the localized field name in the controller or not
     * <p>
     * Depends on {@code useDefaultErrorData} is {@code true}
     *
     * @param request the servlet request
     * @param status the http status that determined
     * @param cause the exception occurred, maybe {@code null} if http 404
     * @param html indicates is a html request if true, otherwise is an async request (probably ajax)
     *
     * @return whether to use the localized field name in the controller or not
     */
    default boolean useLocalizedFieldName(@Nonnull HttpServletRequest request, @Nullable HttpStatus status, @Nullable Throwable cause, boolean html) {
        return false;
    }
}
