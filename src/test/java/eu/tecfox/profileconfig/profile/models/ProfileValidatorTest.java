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

package eu.tecfox.profileconfig.profile.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import eu.tecfox.profileconfig.template.models.section.elements.StringElement;
import eu.tecfox.profileconfig.ProfileConfiguratorApplication;
import eu.tecfox.profileconfig.config.ContextProvider;
import eu.tecfox.profileconfig.template.TemplateService;
import eu.tecfox.profileconfig.template.models.Template;
import eu.tecfox.profileconfig.template.models.section.Section;
import eu.tecfox.profileconfig.template.models.section.elements.SectionElement;
import eu.tecfox.profileconfig.testdata.TestDataGenerator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {ProfileConfiguratorApplication.class, TemplateService.class, ProfileValidator.class,
    ContextProvider.class})
@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true",
    "spring.cloud.discovery.enabled=false"})
class ProfileValidatorTest {

    @MockBean
    TemplateService templateService;


    private final Validator validator;

    ProfileValidatorTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @Test
    void Should_BeInValid_If_SectionsAreMissing() {
        Profile profile = new Profile();
        when(templateService.getTemplate()).thenReturn(Optional.of(new Template()));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Profile> violation =  violations.iterator().next();
        assertEquals("Your profile does not contain any sections.", violation.getMessage());
    }

    @Test
    void Should_BeInvalid_If_ProfileAndTemplateSectionsDoNotMatch() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        profile.getSections().remove(2);
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Profile> violation =  violations.iterator().next();
        assertEquals("Your sections do not match the template.", violation.getMessage());
    }

    @Test
    void Should_BeInvalid_If_SectionIsMissing() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        profile.getSections().get(0).setIdentifier("12345");
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Profile> violation =  violations.iterator().next();
        assertEquals("Section Allgemein is missing in the profile.", violation.getMessage());
    }

    @Test
    void Should_BeInvalid_If_SectionDoesNotContainElements() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        profile.getSections().get(0).setElements(new ArrayList<>());
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Profile> violation =  violations.iterator().next();
        assertEquals("Section Allgemein does not contain any elements.", violation.getMessage());
    }

    /*
     * Element is unique.
     */

    // 1. Element exists --> valid
    @Test
    void Should_BeValid_If_TemplateElementIsUniqueAndRequiredAndExistsOnce() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(UUID.randomUUID().toString());
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(true, true, "Name");
        SectionElement secondElement = TestDataGenerator.generateSectionStringElement(true,  true,"Name2");
        sections.get(0).setElements(Arrays.asList(firstElement, secondElement));
        template.setSections(sections);
        profile.setSections(sections);
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertTrue(violations.isEmpty());
    }

    // 2. Element exists more than once --> invalid
    @Test
    void Should_BeInValid_If_TemplateElementIsUniqueAndRequiredAndIsDuplicated() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(true, true, "Name");
        SectionElement secondElement = TestDataGenerator.generateSectionStringElement(true, true, "Name2");
        template.setSections(sections);
        template.getSections().get(0).setElements(Arrays.asList(firstElement, secondElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(Arrays.asList(firstElement, firstElement, secondElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Profile> violation =  violations.iterator().next();
        assertEquals("Elements of section Section 1 do not match the template.", violation.getMessage());
    }

    // 3. Element does not exist --> invalid
    @Test
    void Should_BeInValid_If_TemplateElementIsUniqueAndRequiredAndDoesNotExist() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(true, true, "Name");
        SectionElement secondElement = TestDataGenerator.generateSectionStringElement(true,  true,"Name2");
        template.setSections(sections);
        template.getSections().get(0).setElements(Arrays.asList(firstElement, secondElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(List.of(secondElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Profile> violation =  violations.iterator().next();
        assertEquals("Elements of section Section 1 do not match the template.", violation.getMessage());
    }

    @Test
    void Should_BeInValid_If_TemplateElementIsUniqueAndRequiredAndIsDuplicatedWithSingleTemplate() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(true,  true,"Name");
        template.setSections(sections);
        template.getSections().get(0).setElements(List.of(firstElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(Arrays.asList(firstElement, firstElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Profile> violation =  violations.iterator().next();
        assertEquals("Elements of section Section 1 do not match the template.", violation.getMessage());
    }


    /*
     * Element is not unique.
     */

    // 1. Element exists --> valid
    @Test
    void Should_BeValid_If_TemplateElementIsNotUniqueButRequiredAndExists() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(false, true, "Name");
        SectionElement secondElement = TestDataGenerator.generateSectionStringElement(true,  true,"Name2");
        template.setSections(sections);
        template.getSections().get(0).setElements(Arrays.asList(firstElement, secondElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(List.of(firstElement, secondElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertTrue(violations.isEmpty());
    }

    // 2. Element exists more than once --> valid
    @Test
    void Should_BeValid_If_TemplateElementIsNotUniqueButRequiredAndIsDuplicated() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(false, true, "Name");
        SectionElement secondElement = TestDataGenerator.generateSectionStringElement(true,  true,"Name2");
        template.setSections(sections);
        template.getSections().get(0).setElements(Arrays.asList(firstElement, secondElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(List.of(firstElement, firstElement, secondElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertTrue(violations.isEmpty());
    }

    // 3. Element does not exist --> invalid
    @Test
    void Should_BeInValid_If_TemplateElementIsNotUniqueButRequiredAndDoesNotExist() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(false, true, "Name");
        SectionElement secondElement = TestDataGenerator.generateSectionStringElement(true,  true,"Name2");
        template.setSections(sections);
        template.getSections().get(0).setElements(Arrays.asList(firstElement, secondElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(List.of(secondElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Profile> violation =  violations.iterator().next();
        assertEquals("Elements of section Section 1 do not match the template.", violation.getMessage());
    }

    @Test
    void Should_BeValid_If_TemplateElementIsNotUniqueButRequiredAndIsDuplicatedWithSingleTemplate() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(false,  true,"Name");
        template.setSections(sections);
        template.getSections().get(0).setElements(List.of(firstElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(List.of(firstElement, firstElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertTrue(violations.isEmpty());
    }

    /*
     * Element is unique but not required.
     */

    // 1. Element exists --> valid
    @Test
    void Should_BeValid_If_TemplateElementIsUniqueAndNotRequiredAndExists() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(true, false, "Name");
        SectionElement secondElement = TestDataGenerator.generateSectionStringElement(true, true, "Name2");
        template.setSections(sections);
        template.getSections().get(0).setElements(Arrays.asList(firstElement, secondElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(List.of(firstElement, secondElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertTrue(violations.isEmpty());
    }

    // 2. Element exists more than once --> invalid
    @Test
    void Should_BeInValid_If_TemplateElementIsUniqueButNotRequiredAndIsDuplicated() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(true, false, "Name");
        SectionElement secondElement = TestDataGenerator.generateSectionStringElement(true, true, "Name2");
        template.setSections(sections);
        template.getSections().get(0).setElements(Arrays.asList(firstElement, secondElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(List.of(firstElement, firstElement, secondElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Profile> violation =  violations.iterator().next();
        assertEquals("Elements of section Section 1 do not match the template.", violation.getMessage());
    }

    // 3. Element does not exist --> valid
    @Test
    void Should_BeValid_If_TemplateElementIsUniqueAndNotRequiredAndDoesNotExist() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(true, false, "Name");
        SectionElement secondElement = TestDataGenerator.generateSectionStringElement(true, true, "Name2");
        template.setSections(sections);
        template.getSections().get(0).setElements(Arrays.asList(firstElement, secondElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(List.of(secondElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void Should_BeInValid_If_TemplateElementIsUniqueButNotRequiredAndIsDuplicatedWithSingleTemplate() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(true, false, "Name");
        template.setSections(sections);
        template.getSections().get(0).setElements(List.of(firstElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(List.of(firstElement, firstElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Profile> violation =  violations.iterator().next();
        assertEquals("Elements of section Section 1 do not match the template.", violation.getMessage());
    }

    /*
     * Element is not unique and not required.
     */
    // 1. Element exists --> valid
    @Test
    void Should_BeValid_If_TemplateElementIsNotUniqueAndNotRequiredAndExists() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(false, false, "Name");
        SectionElement secondElement = TestDataGenerator.generateSectionStringElement(true, true, "Name2");
        template.setSections(sections);
        template.getSections().get(0).setElements(Arrays.asList(firstElement, secondElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(List.of(firstElement, secondElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertTrue(violations.isEmpty());
    }

    // 2. Element exists more than once --> valid
    @Test
    void Should_BeValid_If_TemplateElementIsNotUniqueAndNotRequiredAndIsDuplicated() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(false, false, "Name");
        SectionElement secondElement = TestDataGenerator.generateSectionStringElement(true, true, "Name2");
        template.setSections(sections);
        template.getSections().get(0).setElements(Arrays.asList(firstElement, secondElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(List.of(firstElement, firstElement, secondElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertTrue(violations.isEmpty());
    }

    // 3. Element does not exist --> valid
    @Test
    void Should_BeValid_If_TemplateElementIsNotUniqueAndNotRequiredAndDoesNotExist() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement firstElement = TestDataGenerator.generateSectionStringElement(false, false, "Name");
        SectionElement secondElement = TestDataGenerator.generateSectionStringElement(true, true, "Name2");
        template.setSections(sections);
        template.getSections().get(0).setElements(Arrays.asList(firstElement, secondElement));
        List<Section> profileSections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        profile.setSections(profileSections);
        profile.getSections().get(0).setElements(List.of(secondElement));
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void Should_BeValid_If_ValidProfileIsValidated() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void Should_BeInvalid_If_StringElementIsRequiredButHasNoValue() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        Template template = TestDataGenerator.generateValidTemplate();
        String uuid = UUID.randomUUID().toString();
        List<Section> sections = TestDataGenerator.generateEmptyTemplateSection(uuid);
        SectionElement element = TestDataGenerator.generateSectionStringElement(true, true, "Name");
        ((StringElement) element).setValue("");
        sections.get(0).setElements(List.of(element));
        profile.setSections(sections);
        template.setSections(sections);
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Set<ConstraintViolation<Profile>> violations = validator.validate(profile, ValidProfile.class);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        ConstraintViolation<Profile> violation =  violations.iterator().next();
        assertEquals("Element Name missing values.", violation.getMessage());
    }


}