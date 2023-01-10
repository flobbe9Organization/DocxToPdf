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

import eu.tecfox.profileconfig.template.models.section.Section;
import eu.tecfox.profileconfig.template.models.section.elements.NestedElement;
import eu.tecfox.profileconfig.template.models.section.elements.NestedValue;
import eu.tecfox.profileconfig.template.models.section.elements.SectionElement;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Custom validator for {@link Template}.
 *
 * <p>
 *     Checks if sections and section elements in the template
 *     are correctly formed. Validation of other types is done
 *     via annotations in the template class.
 * </p>
 *
 * @author Valentin Laucht
 * @version 1.0
 */
public class TemplateValidator implements ConstraintValidator<ValidTemplate, Template> {

    @Override
    public void initialize(ValidTemplate constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Template template, ConstraintValidatorContext context) {
        if (template == null) {
            return false;
        }
        context.disableDefaultConstraintViolation();
        if (template.getSections() == null || template.getSections().isEmpty()) {
            context
                .buildConstraintViolationWithTemplate("Template does not contain any sections.")
                .addConstraintViolation();
            return false;
        }
        return validateSections(template.getSections(), context);
    }

    /**
     * Validates all {@link Section} of a {@link Template}.
     *
     * <p>
     *     Validates that a section has a name, section elements
     *     and only 1 declaration.
     * </p>
     *
     * @param sections list of all sections in a template
     * @param context the validator context
     * @return {@code true} if ALL sections are valid
     */
    private boolean validateSections(List<Section> sections, ConstraintValidatorContext context) {
        boolean isValid = true;
        for (Section section: sections) {
            // check if section has title
            if (section.isShowTitle() && (section.getTitle() == null || section.getTitle().isBlank())) {
                context
                    .buildConstraintViolationWithTemplate("Section title is missing but set to show.")
                    .addConstraintViolation();
                return false;
            }
            // check if section has elements
            if (section.getElements() == null || section.getElements().isEmpty()) {
                context
                    .buildConstraintViolationWithTemplate("Section " + section.getTitle() + " does not contain any elements.")
                    .addConstraintViolation();
                isValid = false;
            }
            // check if multiple declarations available
            else if (sections.stream().filter(s -> s.getTitle().equals(section.getTitle())).toList().size() > 1) {
                context
                    .buildConstraintViolationWithTemplate("Multiple declarations of section " + section.getTitle() + ".")
                    .addConstraintViolation();
                isValid = false;
            }
            else {
                isValid = validateSectionElements(section.getElements(), section.getTitle(), context) && isValid;
            }

        }
        return isValid;
    }

    /**
     * Validates all {@link SectionElement} of a {@link Section}.
     *
     * <p>
     *     Validates that each element has a name and only 1 declaration.
     * </p>
     * @param sectionElements all elements of a section
     * @param sectionName the name of the section (for better error messages)
     * @param context the validator context
     * @return {@code true} if ALL elements are valid
     */
    private boolean validateSectionElements(List<SectionElement> sectionElements, String sectionName, ConstraintValidatorContext context) {
        boolean isValid = true;
        for (SectionElement sectionElement: sectionElements) {
            // check if element has a name
            if (!(sectionElement instanceof NestedElement)) {
                if (sectionElement.getKey() == null || sectionElement.getKey().toString().isBlank()) {
                    context
                        .buildConstraintViolationWithTemplate("Section element in section " + sectionName + " is" +
                            " missing a name.")
                        .addConstraintViolation();
                    return false;
                }
            } else {
                isValid = validateNestedElement((NestedElement) sectionElement, sectionName, context) && isValid;
            }
            // check if element is duplicated
            List<SectionElement> duplicates = sectionElements.stream().filter(s -> s.getKey().equals(sectionElement.getKey())).toList();
            if (duplicates.size() > 1) {
                context
                    .buildConstraintViolationWithTemplate("Section element " + sectionElement.getKey()+ " in section " + sectionName +
                        " has multiple declarations.")
                    .addConstraintViolation();
                isValid = false;
            }
        }
        return isValid;
    }

    /**
     * Validates a {@link NestedElement} in a {@link Section}
     *
     * <p>
     *     Validates that a nested element has at least 1 value and that
     *     each value has a name and is unique.
     * </p>
     *
     * @param element the nested element
     * @param sectionName the name of the section (for better error messages)
     * @param context the validator context
     * @return {@code true} if the element is valid
     */
    private boolean validateNestedElement(NestedElement element, String sectionName, ConstraintValidatorContext context) {
        boolean isValid = true;
        if (element.getValue() == null || element.getValue().isEmpty()) {
            context
                .buildConstraintViolationWithTemplate("Nested element in section " + sectionName + " does not have values.")
                .addConstraintViolation();
            return false;
        }
        for (NestedValue nestedValue: element.getValue()) {
            if (!((SectionElement)nestedValue).isUnique()) {
                context
                    .buildConstraintViolationWithTemplate("Values in Nested Elements must be unique.")
                    .addConstraintViolation();
            }
            if (((SectionElement)nestedValue).getKey() == null || ((SectionElement)nestedValue).getKey().toString().isBlank()) {
                context
                    .buildConstraintViolationWithTemplate("Nested element in section " + sectionName + " is missing names.")
                    .addConstraintViolation();
                isValid = false;
            }
        }
        return isValid;
    }

}
