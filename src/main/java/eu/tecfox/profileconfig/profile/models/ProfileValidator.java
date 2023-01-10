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

import java.util.Iterator;

import eu.tecfox.profileconfig.config.ContextProvider;
import eu.tecfox.profileconfig.template.TemplateService;
import eu.tecfox.profileconfig.template.models.Template;
import eu.tecfox.profileconfig.template.models.section.Section;
import eu.tecfox.profileconfig.template.models.section.elements.DateElement;
import eu.tecfox.profileconfig.template.models.section.elements.DateRange;
import eu.tecfox.profileconfig.template.models.section.elements.DateRangeElement;
import eu.tecfox.profileconfig.template.models.section.elements.NestedElement;
import eu.tecfox.profileconfig.template.models.section.elements.NestedValue;
import eu.tecfox.profileconfig.template.models.section.elements.SectionElement;
import eu.tecfox.profileconfig.template.models.section.elements.StringElement;
import eu.tecfox.profileconfig.template.models.section.elements.StringListElement;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Service;

/**
 * Custom validator for {@link Profile}.
 *
 * <p>
 *     Checks if sections and section elements in the profile
 *     matches the template structure and all elements have values.
 * </p>
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Service
public class ProfileValidator implements ConstraintValidator<ValidProfile, Profile> {

    private TemplateService templateService;


    @Override
    public void initialize(ValidProfile constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        this.templateService = (TemplateService) ContextProvider.getBean(TemplateService.class);
    }

    /**
     * Method to validate a profile against the template if all
     * Sections and Elements exist and are filled with values.
     *
     * @param profile the profile to validate
     * @param context context in which the constraint is evaluated
     *
     * @return {@code true} if the profile is valid
     */
    @Override
    public boolean isValid(Profile profile, ConstraintValidatorContext context) {

        if (profile == null) {
            return false;
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        Template template = templateService.getTemplate().get();

        // check if profile has sections
        if (profile.getSections() == null || profile.getSections().isEmpty()) {
            context
                .buildConstraintViolationWithTemplate("Your profile does not contain any sections.")
                .addConstraintViolation();
            return false;
        }

        // check if profile has same amount of sections
        if (profile.getSections().size() != template.getSections().size()) {
            context
                .buildConstraintViolationWithTemplate("Your sections do not match the template.")
                .addConstraintViolation();
            return false;
        }
        Iterator<Section> templateSections = template.getSections().iterator();
        Iterator<Section> profileSections = profile.getSections().iterator();
        while (templateSections.hasNext() && profileSections.hasNext()) {
            Section templateSection = templateSections.next();
            Section profileSection = profileSections.next();

            // if sections do not match
            if (!templateSection.equals(profileSection)) {
                context
                    .buildConstraintViolationWithTemplate("Section " + templateSection.getTitle() +
                        " is missing in the profile.")
                    .addConstraintViolation();
                return false;
            } else {
                isValid = validateSection(templateSection, profileSection, context) && isValid;
            }
        }
        return isValid;
    }

    /**
     * Method to validate a section.
     *
     * <p>
     *     Checks if all elements from the template exist in the profile.
     *     Checks if only elements that are not unique have duplicates.
     * </p>
     *
     * @param templateSection the section from the template to validate against
     * @param profileSection the section of the profile
     * @param context context in which the constraint is evaluated
     * @return {@code true} if the section is valid
     */
    private boolean validateSection(Section templateSection, Section profileSection, ConstraintValidatorContext context) {
        boolean isValid = true;
        // check if section has elements
        if (profileSection.getElements() == null || profileSection.getElements().isEmpty()) {
            context
                .buildConstraintViolationWithTemplate("Section " + templateSection.getTitle() +
                    " does not contain any elements.")
                .addConstraintViolation();
            return false;
        }


        Iterator<SectionElement> templateElements = templateSection.getElements().iterator();

        SectionElement templateElement = templateElements.next();
        boolean unique = templateElement.isUnique();
        boolean allowDuplicate = false;
        String lastId = "";
        boolean isDuplicate;
        for (SectionElement profileElement: profileSection.getElements()) {
            // elements are equal
            if (profileElement.equals(templateElement)) {

                // check if last element is the same as this element
                isDuplicate = profileElement.getIdentifier().equals(lastId);
                // if no duplicate is allowed, break
                if (isDuplicate && !allowDuplicate) {
                    context
                        .buildConstraintViolationWithTemplate("Elements of section " + templateSection.getTitle() +
                            " do not match the template.")
                        .addConstraintViolation();
                    isValid = false;
                    break;
                }
                // set this element as last element for next iteration
                lastId = profileElement.getIdentifier();
                allowDuplicate = !unique;

                // validate
                isValid = (validateElement(templateElement, profileElement, context) && isValid);

                // if template allows duplicates, keep the template for the next iteration
                if (templateElements.hasNext() && unique) {
                    templateElement = templateElements.next();
                    unique = templateElement.isUnique();
                }

            }
            // elements are not equal
            else {
                // if duplicates are allowed or element not required, try next
                if ((allowDuplicate || !templateElement.isRequired()) && templateElements.hasNext()) {
                    templateElement = templateElements.next();
                    unique = templateElement.isUnique();

                    // if they match now, continue
                    if (profileElement.equals(templateElement)) {
                        isValid = (validateElement(templateElement, profileElement, context) && isValid);
                        continue;
                    }
                }
                // if nothing matches, break
                context
                    .buildConstraintViolationWithTemplate("Elements of section " + templateSection.getTitle() +
                        " do not match the template.")
                    .addConstraintViolation();
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    /**
     * Method to validate an element.
     *
     * @param templateElement the template element to validate against
     * @param profileElement the profile element
     * @param context context in which the constraint is evaluated
     * @return {@code true} if the element is valid
     */
    private boolean validateElement(SectionElement templateElement, SectionElement profileElement, ConstraintValidatorContext context) {
        if (!hasValue(templateElement, profileElement, context)) {
            context
                .buildConstraintViolationWithTemplate("Element " + templateElement.getKey() + " missing values.")
                .addConstraintViolation();
            return false;
        }
        return true;
    }

    /**
     * Method to check if an element has a value.
     *
     * @param templateElement the corresponding element of the template
     * @param profileElement the element to check for values
     * @param context context in which the constraint is evaluated
     * @return {@code true} if the element has values
     */
    private boolean hasValue(SectionElement templateElement, SectionElement profileElement, ConstraintValidatorContext context) {
        if (profileElement instanceof  DateElement) {
            return ((DateElement) profileElement).getValue() != null;
        }
        else if (profileElement instanceof StringElement) {
            return ((StringElement) profileElement).getValue() != null && !((StringElement) profileElement).getValue().isBlank();
        }
        else if (profileElement instanceof StringListElement) {
            if (((StringListElement) profileElement).getValue() == null ||
                (((StringListElement) profileElement).getValue() != null && ((StringListElement) profileElement).getValue().isEmpty())) {
                return false;
            }
            for (String entry: ((StringListElement) profileElement).getValue()) {
                if (entry.isBlank()) return false;
            }
            return true;
        }
        else if (profileElement instanceof DateRangeElement) {
            DateRange dateRange = ((DateRangeElement) profileElement).getValue();
            return dateRange.getFrom() != null && dateRange.getTo() != null;
        }
        else if (profileElement instanceof NestedElement) {
            return validateNestedElement((NestedElement) templateElement, (NestedElement) profileElement, context);
        }
        context
            .buildConstraintViolationWithTemplate("Unknown Element " + profileElement.getKey())
            .addConstraintViolation();
        return false;
    }

    /**
     * Method to validate a nested element.
     *
     * @param templateElement the element from the template
     * @param profileElement the nested element from the profile
     * @param context context in which the constraint is evaluated
     * @return {@code true} if the nested element is valid
     */
    private boolean validateNestedElement(NestedElement templateElement, NestedElement profileElement, ConstraintValidatorContext context) {
        boolean valid;
        if (!templateElement.getType().equals(profileElement.getType())) {
            context
                .buildConstraintViolationWithTemplate("Wrong element as key in " + templateElement.getKey())
                .addConstraintViolation();
        }
        valid = hasValue((SectionElement) templateElement.getKey(), (SectionElement) profileElement.getKey(), context);

        if (profileElement.getValue() == null || profileElement.getValue().isEmpty()) {
            return false;
        }

        // check if template nested values match profile nested values
        if (profileElement.getValue().size() != templateElement.getValue().size()) {
            return false;
        }

        Iterator<NestedValue> templateValues = templateElement.getValue().iterator();
        Iterator<NestedValue> profileValues = profileElement.getValue().iterator();
        while (templateValues.hasNext() && profileValues.hasNext()) {
            SectionElement profileValue = (SectionElement) profileValues.next();
            SectionElement templateValue = (SectionElement) templateValues.next();
            valid = validateElement(templateValue, profileValue, context) && valid;
        }
        return valid;

    }

}
