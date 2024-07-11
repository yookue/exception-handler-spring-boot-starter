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

package com.yookue.springstarter.exceptionhandler.resolver;


import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.yookue.commonplexus.springutil.util.JsonParserWraps;
import com.yookue.commonplexus.springutil.util.WebUtilsWraps;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;


/**
 * {@link org.springframework.web.servlet.HandlerExceptionResolver} for exception handler
 *
 * @author David Hsing
 */
@Getter(value = AccessLevel.PROTECTED)
public abstract class DefaultFilterExceptionResolver extends AbstractFilterExceptionResolver {
    @Override
    @SneakyThrows
    protected void resolveRestInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nullable Object handler, @Nonnull Exception cause, @Nullable ResponseEntity<?> entity) {
        HttpStatusCode status = determineErrorStatus(request, (entity == null ? null : entity.getStatusCode()), cause);
        if (entity != null) {
            String text = JsonParserWraps.toJsonString(entity.getBody(), super.beanFactory);
            if (StringUtils.isNotEmpty(text)) {
                WebUtilsWraps.writeResponseQuietly(response, text, MediaType.APPLICATION_JSON, getServletEncoding(), status);
            }
        }
    }
}
