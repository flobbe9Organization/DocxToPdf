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

import eu.tecfox.profileconfig.testdata.TestDataGenerator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import eu.tecfox.profileconfig.template.models.section.elements.NestedElement;
import eu.tecfox.profileconfig.template.models.section.elements.StringElement;
import eu.tecfox.profileconfig.template.models.section.elements.StringListElement;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplateValidatorTest {

    private final Validator validator;

    TemplateValidatorTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void Should_BeInValid_If_TitleIsMissing() throws IOException {
        Template template = TestDataGenerator.generateValidTemplate();
        template.setTitle("");
        Set<ConstraintViolation<Template>> violations = validator.validate(template, ValidTemplate.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Template> violation =  violations.iterator().next();
        assertEquals("Template must have a title.", violation.getMessage());
    }

    @Test
    void Should_BeInValid_If_NoSectionsProvided() throws IOException {
        Template template = TestDataGenerator.generateValidTemplate();
        template.setSections(null);
        Set<ConstraintViolation<Template>> violations = validator.validate(template, ValidTemplate.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Template> violation =  violations.iterator().next();
        assertEquals("Template does not contain any sections.", violation.getMessage());
    }

    @Test
    void Should_BeInvalid_If_SectionHasNoTitle() throws IOException {
        Template template = TestDataGenerator.generateValidTemplate();
        template.getSections().get(0).setTitle(null);
        template.getSections().get(0).setShowTitle(true);
        Set<ConstraintViolation<Template>> violations = validator.validate(template, ValidTemplate.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Template> violation =  violations.iterator().next();
        assertEquals("Section title is missing but set to show.", violation.getMessage());
    }

    @Test
    void Should_BeInvalid_If_SectionMissingElements() throws IOException {
        Template template = TestDataGenerator.generateValidTemplate();
        template.getSections().get(0).setTitle("Section");
        template.getSections().get(0).setElements(null);
        Set<ConstraintViolation<Template>> violations = validator.validate(template, ValidTemplate.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Template> violation =  violations.iterator().next();
        assertEquals("Section " + template.getSections().get(0).getTitle() +
            " does not contain any elements.", violation.getMessage());
    }

    @Test
    void Should_BeInvalid_If_MultipleDeclarationsOfSectionProvided() throws IOException {
        Template template = TestDataGenerator.generateValidTemplate();
        template.getSections().get(0).setTitle("Section");
        template.getSections().get(1).setTitle("Section");
        Set<ConstraintViolation<Template>> violations = validator.validate(template, ValidTemplate.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Template> violation =  violations.iterator().next();
        assertEquals("Multiple declarations of section Section.", violation.getMessage());
    }

    @Test
    void Should_BeInvalid_If_SectionElementHasNoName() throws IOException {
        Template template = TestDataGenerator.generateValidTemplate();
        template.getSections().get(0).setTitle("Section");
        ((StringElement)template.getSections().get(0).getElements().get(0)).setKey("");
        Set<ConstraintViolation<Template>> violations = validator.validate(template, ValidTemplate.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Template> violation =  violations.iterator().next();
        assertEquals("Section element in section Section is missing a name.", violation.getMessage());
    }

    @Test
    void Should_BeInvalid_If_SectionElementHasDuplicates() throws IOException {
        Template template = TestDataGenerator.generateValidTemplate();
        template.getSections().get(0).setTitle("Section");
        ((StringElement)template.getSections().get(0).getElements().get(0)).setKey("Element");
        ((StringListElement)template.getSections().get(0).getElements().get(1)).setKey("Element");
        Set<ConstraintViolation<Template>> violations = validator.validate(template, ValidTemplate.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Template> violation =  violations.iterator().next();
        assertEquals("Section element Element in section Section has multiple declarations.", violation.getMessage());
    }

    @Test
    void Should_BeInvalid_If_NestedElementHasNoValues() throws IOException {
        Template template = TestDataGenerator.generateValidTemplate();
        template.getSections().get(2).setTitle("Section");
        ((NestedElement)template.getSections().get(2).getElements().get(0)).setValue(new ArrayList<>());
        Set<ConstraintViolation<Template>> violations = validator.validate(template, ValidTemplate.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Template> violation =  violations.iterator().next();
        assertEquals("Nested element in section Section does not have values.", violation.getMessage());
    }

    @Test
    void Should_BeInvalid_If_NestedElementValueHasNoName() throws IOException {
        Template template = TestDataGenerator.generateValidTemplate();
        template.getSections().get(2).setTitle("Section");
        ((StringElement)((NestedElement)template.getSections().get(2).getElements().get(0)).getValue().get(0)).setKey("");
        Set<ConstraintViolation<Template>> violations = validator.validate(template, ValidTemplate.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Template> violation =  violations.iterator().next();
        assertEquals("Nested element in section Section is missing names.", violation.getMessage());
    }

    @Test
    void Should_BeValid_If_ValidProfileProvided() throws IOException {
        Template template = TestDataGenerator.generateValidTemplate();
        Set<ConstraintViolation<Template>> violations = validator.validate(template, ValidTemplate.class);
        assertTrue(violations.isEmpty());
    }

}