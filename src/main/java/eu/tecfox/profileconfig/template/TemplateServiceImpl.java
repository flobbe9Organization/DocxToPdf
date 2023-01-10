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

package eu.tecfox.profileconfig.template;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tecfox.profileconfig.template.models.Template;
import eu.tecfox.profileconfig.template.models.section.Section;
import eu.tecfox.profileconfig.template.models.section.elements.NestedElement;
import eu.tecfox.profileconfig.template.models.section.elements.NestedValue;
import eu.tecfox.profileconfig.template.models.section.elements.SectionElement;
import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link TemplateService} interface with all business
 * logic related to templates.
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;

    /**
     * Method to load an initial template if the database is empty.
     * @throws IOException if json deserialization fails.
     */
    @PostConstruct
    private void initTemplate() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Optional<Template> template = getTemplate();
        if (template.isEmpty()) {
            String json = mapper.readTree(
                Paths.get("./src/main/java/eu/tecfox/profileconfig/template/profileTemplate.json").toFile()).toString();
            Template initTemplate = mapper.readValue(json, Template.class);
            saveTemplate(initTemplate);
        }
    }

    /**
     * Get the template from the database.
     *
     * <p>
     *     Only one template can exist at a time. Thus, get the first entry.
     * </p>
     *
     * @return an optional with the template if it exists
     */
    @Override
    public Optional<Template> getTemplate() {
        List<Template> templates = templateRepository.findAll();
        return templates.isEmpty() ? Optional.empty() : Optional.of(templates.get(0));
    }

    /**
     * Save the template to the database.
     *
     * @param template the template
     * @return the saved template
     */
    @Override
    public Template saveTemplate(Template template) {
        prepareTemplate(template);

        return templateRepository.save(template);
    }

    /**
     * Method to prepare a template before it is saved to the database.
     *
     * <p>
     *     Ensures that the id matches the id of an existing template so that
     *     it gets overwritten.
     *     Fills identifiers for each section element, if no identifier exists.
     *     Identifiers are used to identify an element, even if its name changes.
     * </p>
     *
     * @param template the template to prepare
     */
    @Override
    public void prepareTemplate(Template template) {
        // set template id so it overrides the existing template
        getTemplate().ifPresent(value -> template.setId(value.getId()));

        // prepare identifiers for new elements
        for (Section section: template.getSections()) {
            if (section.getIdentifier() == null || section.getIdentifier().isBlank()) {
                section.setIdentifier(UUID.randomUUID().toString());
            }
            for (SectionElement sectionElement: section.getElements()) {
                if (sectionElement.getIdentifier() == null || sectionElement.getIdentifier().isBlank()) {
                    sectionElement.setIdentifier(UUID.randomUUID().toString());
                }
                if (sectionElement instanceof NestedElement) {
                    if (((SectionElement)sectionElement.getKey()).getIdentifier() == null ||
                        ((SectionElement)sectionElement.getKey()).getIdentifier().isBlank()) {
                        ((SectionElement)sectionElement.getKey()).setIdentifier(UUID.randomUUID().toString());
                    }
                    for (NestedValue nestedValue: ((NestedElement)sectionElement).getValue()) {
                        ((SectionElement)nestedValue).setUnique(true);
                        if (((SectionElement)nestedValue).getIdentifier() == null ||
                            ((SectionElement)nestedValue).getIdentifier().isBlank()) {
                            ((SectionElement)nestedValue).setIdentifier(UUID.randomUUID().toString());
                        }
                    }
                }
            }
        }

        // clear all values from elements
        clearValues(template);
    }

    /**
     * Clear all values from the template.
     *
     * <p>
     *     Used to remove all values from the template. Templates
     *     should not be stored with example values.
     * </p>
     *
     * @param template the template to clear
     */
    private void clearValues(Template template) {
        template.getSections().forEach(section -> section.getElements().forEach(SectionElement::clearValue));
    }
}
