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

import eu.tecfox.profileconfig.template.models.section.elements.DateRangeElement;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tecfox.profileconfig.template.models.section.elements.DateElement;
import eu.tecfox.profileconfig.template.models.section.elements.NestedElement;
import eu.tecfox.profileconfig.template.models.section.elements.SectionElement;
import eu.tecfox.profileconfig.template.models.section.elements.StringElement;
import eu.tecfox.profileconfig.template.models.section.elements.StringListElement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;



class ProfileTest {

    String json;
    ObjectMapper mapper = new ObjectMapper();

    ProfileTest() throws IOException {
        json = mapper.readTree(
            Paths.get("./src/test/java/eu/tecfox/profileconfig/testdata/testProfile.json").toFile()).toString();
    }


    @Test
    void Should_NotThrowException_When_ValidProfileIsDeserialized() {
        assertDoesNotThrow(() -> mapper.readValue(json, Profile.class));
    }

    @Test
    void Should_ParseProfileBaseData() throws JsonProcessingException {
        Profile profile = mapper.readValue(json, Profile.class);
        assertEquals("Qualifikationsprofil", profile.getTitle());
    }

    @Test
    void Should_ParseSection() throws JsonProcessingException {
        Profile profile = mapper.readValue(json, Profile.class);
        assertNotNull(profile.getSections());
        assertEquals(3, profile.getSections().size());
        assertEquals("Fähigkeiten / Kenntnisse", profile.getSections().get(1).getTitle());
    }

    @Test
    void Should_ParseStringElement() throws JsonProcessingException {
        Profile profile = mapper.readValue(json, Profile.class);
        List<SectionElement> elementList = profile.getSections().get(0).getElements();
        assertNotNull(elementList);
        assertTrue(elementList.size() > 0);
        SectionElement element = elementList.get(0);
        assertTrue(element instanceof StringElement);
        assertEquals("Name", ((StringElement) element).getKey());
        assertEquals("Max Mustermann", ((StringElement) element).getValue());
    }

    @Test
    void Should_ParseDateElement() throws JsonProcessingException {
        Profile profile = mapper.readValue(json, Profile.class);
        List<SectionElement> elementList = profile.getSections().get(0).getElements();
        assertNotNull(elementList);
        assertTrue(elementList.size() > 0);
        SectionElement element = elementList.get(4);
        assertTrue(element instanceof DateElement);
        assertEquals("Verfügbarkeit", ((DateElement) element).getKey());
        assertEquals(LocalDate.of(2022, 12, 11), ((DateElement) element).getValue());
    }

    @Test
    void Should_ParseStringListElement() throws JsonProcessingException {
        Profile profile = mapper.readValue(json, Profile.class);
        List<SectionElement> elementList = profile.getSections().get(0).getElements();
        assertNotNull(elementList);
        assertTrue(elementList.size() > 0);
        SectionElement element = elementList.get(1);
        assertTrue(element instanceof StringListElement);
        assertEquals("Sprachen", ((StringListElement) element).getKey());
        assertEquals(2, ((StringListElement) element).getValue().size());
        assertEquals("Deutsch", ((StringListElement) element).getValue().get(0));
    }

    @Test
    void Should_ParseNestedElement() throws JsonProcessingException {
        Profile profile = mapper.readValue(json, Profile.class);
        List<SectionElement> elementList = profile.getSections().get(2).getElements();
        assertNotNull(elementList);
        assertTrue(elementList.size() > 0);
        SectionElement element = elementList.get(0);
        assertTrue(element instanceof NestedElement);
        assertTrue(((NestedElement) element).getKey() instanceof DateRangeElement);
        assertEquals(LocalDate.of(2021, 1, 1), ((DateRangeElement) ((NestedElement) element).getKey()).getValue().getFrom());
        assertEquals(3, ((NestedElement) element).getValue().size());
        assertTrue(((NestedElement) element).getValue().get(0) instanceof StringElement);
        assertEquals("Software Entwickler", ((StringElement) ((NestedElement) element).getValue().get(0)).getValue());
    }

    @Test
    void Should_PatchProfile() {
        Profile newProfile = new Profile();
        Profile originalProfile = new Profile();
        originalProfile.setId("12345");
        originalProfile.setUserId("54321");
        originalProfile.setCreatedDate(LocalDate.of(2022, 1, 1).atStartOfDay());
        originalProfile.setLastModifiedDate(LocalDate.of(2022, 2, 2).atStartOfDay());
        originalProfile.setCreatedByUser("userA");
        originalProfile.setModifiedByUser("userB");
        newProfile.patch(originalProfile);
        assertEquals("12345", newProfile.getId());
        assertEquals("54321", newProfile.getUserId());
        assertEquals(LocalDate.of(2022, 1, 1).atStartOfDay(), newProfile.getCreatedDate());
        assertEquals(LocalDate.of(2022, 2, 2).atStartOfDay(), newProfile.getLastModifiedDate());
        assertEquals("userA", newProfile.getCreatedByUser());
        assertEquals("userB", newProfile.getModifiedByUser());
    }

}