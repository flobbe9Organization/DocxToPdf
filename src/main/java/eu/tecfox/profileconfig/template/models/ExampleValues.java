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

package eu.tecfox.profileconfig.template.models;

import java.util.List;
import java.time.LocalDate;

import lombok.Getter;

/**
 * Enum that holds some example values to fill in the template.
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Getter
public enum ExampleValues {
    EXAMPLE_DATE(LocalDate.now()),
    EXAMPLE_STRING("Lorem ipsum"),
    EXAMPLE_LIST(List.of("dolor", "sit amet", "consetetur",
        "sadipscing", "nonumy", "tempor", "invidunt"));

    private final Object value;
    ExampleValues(Object value) {
        this.value = value;
    }
}
