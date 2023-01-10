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

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Data;

/**
 * Implementation of a {@link SectionElement} used to
 * build a Nested SectionElement.
 *
 * <p>
 *     Used to build a section element with a NestedKey
 *     as key and a List of NestedValue as value.
 * </p>
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Data
public class NestedElement extends SectionElement {
    private NestedKey key;
    private List<NestedValue> value;
    private boolean highlightNestedKeys;

    @Override
    public String getType() {
        return "nested";
    }

    @Override
    public void generateExample() {
        key.generateExample();
        value.forEach(NestedValue::generateExample);
    }

    @Override
    public void clearValue() {
        key.clearValue();
        value.forEach(NestedValue::clearValue);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof NestedElement s)) {
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
        if (!(entityToMerge instanceof NestedElement)) {
            return;
        }
        SectionElement entityToMergeKey = (SectionElement) entityToMerge.getKey();
        SectionElement thisEntityKey = (SectionElement) this.getKey();
        thisEntityKey.patch(entityToMergeKey);

        List<NestedValue> thisEntityValues = this.getValue();
        List<NestedValue> entityToMergeValues = ((NestedElement)entityToMerge).getValue();
        entityToMergeValues.retainAll(thisEntityValues);

        final Map<Object, NestedValue> mergeValuesMap = entityToMergeValues.stream()
            .collect(Collectors.toMap(NestedValue::getIdentifier, Function.identity()));

        for (NestedValue sectionElement: thisEntityValues) {
            ((SectionElement) sectionElement).patch(
                (SectionElement) mergeValuesMap.get(sectionElement.getIdentifier()));
        }
    }
}
