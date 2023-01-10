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

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * Helper class to read {@link BindingResult} errors and format them.
 *
 * @author Valentin Laucht
 * @version 1.0
 */
public class BindingResultErrorFormatter {

    /**
     * Method to extract all binding result errors to an array.
     *
     * @param bindingResult the binding result
     * @return an array with all errors
     */
    public static List<String> getErrorMessages(BindingResult bindingResult) {
        return bindingResult.getAllErrors()
            .stream()
            .map(error -> {
                var defaultMessage = error.getDefaultMessage();
                if (error instanceof FieldError fieldError) {
                    return String.format("%s: %s", fieldError.getField(), defaultMessage);
                } else {
                    return defaultMessage;
                }
            })
            .collect(Collectors.toList());
    }

    /**
     * Method to serialize the binding result error array.
     *
     * @param bindingResult the binding result
     * @return a stringified error array
     * @throws JsonProcessingException if serialization fails
     */
    public static String getErrorMessagesAsJson(BindingResult bindingResult)
        throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> errors = getErrorMessages(bindingResult);
        return mapper.writeValueAsString(errors);
    }
}
