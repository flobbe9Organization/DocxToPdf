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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.tecfox.profileconfig.template.models.elements.Footer;
import eu.tecfox.profileconfig.template.models.elements.Header;
import eu.tecfox.profileconfig.template.models.metadata.AuditMetaData;
import eu.tecfox.profileconfig.template.models.section.Section;
import eu.tecfox.profileconfig.template.models.style.Style;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity for a Profile template.
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "templates")
@ValidTemplate(groups = {ValidTemplate.class})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Template extends AuditMetaData {

    @Id
    @JsonIgnore
    private String id;

    @NotBlank(message = "Template must have a title.", groups = {ValidTemplate.class})
    private String title;

    private List<Section> sections;

    @NotNull(message = "Template does not contain style information.", groups = {ValidTemplate.class})
    @Valid
    private Style style;

    private Header header;

    private Footer footer;
}
