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

package eu.tecfox.profileconfig.template.models.style;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.tecfox.profileconfig.template.models.ValidTemplate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Object that holds all style information for a Profile or Section.
 *
 * <p>
 *     The {@link eu.tecfox.profileconfig.profile.models.Profile} holds the global
 *     style information that can be overwritten with more specific information at
 *     the {@link eu.tecfox.profileconfig.template.models.section.Section} level.
 * </p>
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Style {

    @NotBlank(message = "Font Type is missing.", groups = {ValidTemplate.class})
    private String fontType;

    @NotNull(groups = {ValidTemplate.class})
    @Min(value = 5, message = "Font size must be bigger than 5.", groups = {ValidTemplate.class})
    private int fontSize;

    @NotNull(groups = {ValidTemplate.class})
    @Min(value = 10, message = "Heading size must be bigger than 10.", groups = {ValidTemplate.class})
    private int headingSize;

    @NotBlank(message = "Primary Color is missing", groups = {ValidTemplate.class})
    @Pattern(regexp ="^([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$", message = "Not a valid hex code.", groups = {
        ValidTemplate.class})
    private String primaryColor;

    @NotBlank(message = "Secondary Color is missing", groups = {ValidTemplate.class})
    @Pattern(regexp ="^([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$", message = "Not a valid hex code.", groups = {
        ValidTemplate.class})
    private String secondaryColor;

    private boolean boldKeys;
}
