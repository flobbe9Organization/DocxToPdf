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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import eu.tecfox.profileconfig.profile.models.Patchable;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class of a SectionElement contained in a {@link eu.tecfox.profileconfig.template.models.section.Section}.
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DateElement.class, name = "date"),
    @JsonSubTypes.Type(value = StringElement.class, name = "string"),
    @JsonSubTypes.Type(value = StringListElement.class, name = "stringList"),
    @JsonSubTypes.Type(value = NestedElement.class, name = "nested"),
    @JsonSubTypes.Type(value = DateRangeElement.class, name = "dateRange")
})
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public abstract class SectionElement implements Patchable<SectionElement> {


    /**
     * Indicates if this element is required. If it is required, it has to be filled out
     * in a valid profile.
     */
    private boolean required;

    /**
     * A {@link java.util.UUID} that identifies this section element.
     * This is required to identify a section element even if it's name changes.
     */
    private String identifier;

    /**
     * Indicates if this element is unique. A unique element can only be declared once in
     * a section.
     */
    private boolean unique = true;

    /**
     * Method to declare the type of the concrete implementation to
     * the Jackson deserializer.
     *
     * <p>
     *     Each type in a concrete implementation needs to correspond to a
     *     type in the JsonSubTypes List at the top of this class.
     * </p>
     *
     * @return a string value that indicates the type
     */
    @JsonIgnore
    @JsonProperty("type")
    abstract String getType();

    /**
     * The object that holds the name of the section element.
     *
     * @return an object containing the name or element
     */
    public abstract Object getKey();

    /**
     * The object that holds the value(s) of the section element.
     *
     * @return an object containing all values
     */
    public abstract Object getValue();

    /**
     * Method to fill the SectionElement with example values.
     */
    public abstract void generateExample();

    /**
     * Method to clear all values from this SectionElement.
     */
    public abstract void clearValue();

}
