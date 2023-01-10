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

package eu.tecfox.profileconfig.template.models.section.elements;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import eu.tecfox.profileconfig.template.models.ExampleValues;

import lombok.Data;

/**
 * Implementation of a {@link SectionElement} used to
 * render a String, Date pair.
 *
 * <p>
 *     Used to render a section element with a String as name
 *     and a Date as value.
 * </p>
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Data
public class DateElement extends SectionElement implements NestedKey, NestedValue {


    private String key;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate value;

    @Override
    public String getType() {
        return "date";
    }

    @Override
    public void generateExample() {
        if (this.getValue() == null) {
            this.setValue((LocalDate) ExampleValues.EXAMPLE_DATE.getValue());
        }
    }

    @Override
    public void clearValue() {
        this.setValue(null);
    }

    @Override
    public void patch(SectionElement entityToMerge) {
        if (!(entityToMerge instanceof DateElement)) {
            return;
        }
        this.value = (LocalDate) entityToMerge.getValue();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DateElement s)) {
            return false;
        }
        if (s.getIdentifier() == null || this.getIdentifier() == null) {
            return false;
        }
        return s.getIdentifier().equals(this.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, getIdentifier());
    }
}
