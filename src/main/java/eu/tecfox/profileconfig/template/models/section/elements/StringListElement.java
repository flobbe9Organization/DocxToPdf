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

import java.util.List;
import java.util.Objects;

import eu.tecfox.profileconfig.template.models.ExampleValues;

import lombok.Data;

/**
 * Implementation of a {@link SectionElement} used to
 * render a String, List of String pair.
 *
 * <p>
 *     Used to render a section element with a String as name
 *     and a List of Strings as value.
 * </p>
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Data
public class StringListElement extends SectionElement implements NestedValue  {

    private String key;
    private List<String> value;

    @Override
    public String getType() {
        return "stringList";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void generateExample() {
        if (this.getValue().isEmpty()) {
            this.setValue((List<String>) ExampleValues.EXAMPLE_LIST.getValue());
        }
    }

    @Override
    public void clearValue() {
        this.value.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StringListElement s)) {
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

    @Override
    public void patch(SectionElement entityToMerge) {
        if (!(entityToMerge instanceof StringListElement)) {
            return;
        }
        this.value = ((StringListElement) entityToMerge).getValue();
    }
}
