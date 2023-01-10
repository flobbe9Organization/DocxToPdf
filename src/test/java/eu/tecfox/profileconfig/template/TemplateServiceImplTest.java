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

import eu.tecfox.profileconfig.template.models.section.elements.NestedElement;
import eu.tecfox.profileconfig.template.models.section.elements.SectionElement;
import eu.tecfox.profileconfig.testdata.TestDataGenerator;
import java.io.IOException;
import java.util.Optional;

import eu.tecfox.profileconfig.ProfileConfiguratorApplication;
import eu.tecfox.profileconfig.template.models.Template;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ProfileConfiguratorApplication.class, TemplateRepository.class, TemplateServiceImpl.class})
@TestPropertySource(properties = {"spring.cloud.discovery.enabled=false"})
class TemplateServiceImplTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");
    private final TemplateService templateService;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    TemplateServiceImplTest(@Autowired TemplateRepository templateRepository) {
        this.templateService = new TemplateServiceImpl(templateRepository);
    }

    @Test
    void Should_GetTemplateFromDatabase() {
        Optional<Template> template = templateService.getTemplate();
        assertTrue(template.isPresent());
        Template result = template.get();
        assertNotNull(result.getId());
    }

    @Test
    void Should_SaveTemplateToDatabase() throws IOException {
        Template template = TestDataGenerator.generateValidTemplate();
        Template result = templateService.saveTemplate(template);
        assertNotNull(result.getLastModifiedDate());
        assertNotNull(result.getId());
    }

    @Test
    void Should_PrepareTemplateWithIdentifiers() throws IOException {
        Template template = TestDataGenerator.generateValidTemplate();
        template.getSections().get(0).setIdentifier("");
        template.getSections().get(0).getElements().get(0).setIdentifier(null);
        template.getSections().get(2).getElements().get(0).setIdentifier("");
        NestedElement nestedElement =((NestedElement) template.getSections().get(2).getElements().get(0));
        ((SectionElement)nestedElement.getKey()).setIdentifier("");
        ((SectionElement)nestedElement.getValue().get(0)).setIdentifier("");
        assertTrue(template.getSections().get(0).getIdentifier().isBlank());
        templateService.prepareTemplate(template);
        assertFalse(template.getSections().get(0).getIdentifier().isBlank());
        assertFalse(template.getSections().get(0).getElements().get(0).getIdentifier().isBlank());
        String keyIdentifier = ((SectionElement)nestedElement.getKey()).getIdentifier();
        String valueIdentifier = ((SectionElement)nestedElement.getValue().get(0)).getIdentifier();
        assertFalse(keyIdentifier.isBlank());
        assertFalse(valueIdentifier.isBlank());
    }
}