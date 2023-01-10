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

import lombok.Getter;

import org.springframework.http.HttpStatus;

/**
 * Used to throw an Api Exception that will be sent to the client.
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Getter
public class ApiRequestException extends RuntimeException {

	private final HttpStatus httpStatus;

	/**
	 * Creates the default ApiRequestException. HttpStatus 400: Bad Request will be
	 * assigned.
	 * @param message the message that is sent to the client.
	 */
	public ApiRequestException(String message) {
		super(message);
		this.httpStatus = HttpStatus.BAD_REQUEST;
	}

	/**
	 * Creates a custom ApiRequestException.
	 * @param message the message that is sent to the client.
	 * @param httpStatus the HttpStatus that is sent to the client.
	 */
	public ApiRequestException(String message, HttpStatus httpStatus) {
		super(message);
		this.httpStatus = httpStatus;
	}

}
