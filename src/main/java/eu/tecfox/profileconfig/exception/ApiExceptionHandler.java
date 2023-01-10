/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.tecfox.profileconfig.exception;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Handles and formats an Api Exception and sends it to the client.
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@ControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ApiRequestException.class)
	public ResponseEntity<Object> handleApiRequestException(ApiRequestException e) {

		ApiException apiException = new ApiException(
			ZonedDateTime.now(ZoneId.of("Z")),
			e.getHttpStatus().value(),
			e.getHttpStatus().getReasonPhrase(),
			e.getMessage());
		return new ResponseEntity<>(apiException, e.getHttpStatus());
	}

}
