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

package eu.tecfox.profileconfig.template.models.section;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tecfox.profileconfig.template.models.section.elements.NestedElement;
import eu.tecfox.profileconfig.template.models.section.elements.StringElement;
import eu.tecfox.profileconfig.testdata.TestDataGenerator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class SectionTest {

    private Section profileSection;
    private Section templateSection;


    @Test
    void Should_MergeElementsThatAreUnique() {
        profileSection = TestDataGenerator.generateMergeTestSection(true, true);
        templateSection = TestDataGenerator.generateMergeTestSection(false, true);
        templateSection.patch(profileSection);
        assertEquals(((StringElement)profileSection.getElements().get(0)).getValue(), ((StringElement)templateSection.getElements().get(0)).getValue());
        assertEquals(((StringElement)((NestedElement)profileSection.getElements().get(1)).getValue().get(0)).getValue(),
            ((StringElement)((NestedElement)templateSection.getElements().get(1)).getValue().get(0)).getValue());
    }

    @Test
    void Should_MergeElementsThatAreDuplicated() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        templateSection = TestDataGenerator.generateMergeTestSection(false, false);
        profileSection = TestDataGenerator.generateMergeTestSection(true, false);
        NestedElement deepCopy = objectMapper
            .readValue(objectMapper.writeValueAsString(profileSection.getElements().get(1)), NestedElement.class);
        ((StringElement)deepCopy.getValue().get(0)).setValue("test2");
        profileSection.getElements().add(deepCopy);
        templateSection.patch(profileSection);
        assertEquals(3, templateSection.getElements().size());
        assertEquals("test2", ((StringElement)((NestedElement)templateSection.getElements().get(2)).getValue().get(0)).getValue());
    }
}