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

import eu.tecfox.profileconfig.template.models.ExampleValues;
import java.time.LocalDate;
import java.util.Objects;
import lombok.Data;

/**
 * Implementation of a {@link SectionElement} used to
 * render a Date Range pair.
 *
 * <p>
 *     Used to render a section element with a String as name
 *     and a two dates as values.
 * </p>
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Data
public class DateRangeElement extends SectionElement implements NestedKey, NestedValue {

    private String key;

    private DateRange value;

    @Override
    String getType() {
        return "dateRange";
    }

    @Override
    public void generateExample() {
        if (this.getValue() == null || this.getValue().getFrom() == null || this.getValue().getTo() == null) {
            DateRange dateRange = new DateRange();
            dateRange.setFrom((LocalDate) ExampleValues.EXAMPLE_DATE.getValue());
            dateRange.setTo((LocalDate) ExampleValues.EXAMPLE_DATE.getValue());
            this.setValue(dateRange);
        }
    }

    @Override
    public void clearValue() {
        this.setValue(null);
    }

    @Override
    public void patch(SectionElement entityToMerge) {
        if (!(entityToMerge instanceof DateRangeElement)) {
            return;
        }
        this.value.setFrom(((DateRangeElement)entityToMerge).getValue().getFrom());
        this.value.setTo(((DateRangeElement)entityToMerge).getValue().getTo());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DateRangeElement s)) {
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
