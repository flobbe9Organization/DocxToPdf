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

package eu.tecfox.profileconfig.profile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import eu.tecfox.profileconfig.profile.models.Profile;
import eu.tecfox.profileconfig.template.TemplateRepository;
import eu.tecfox.profileconfig.template.TemplateService;
import eu.tecfox.profileconfig.template.TemplateServiceImpl;
import eu.tecfox.profileconfig.template.models.Template;
import eu.tecfox.profileconfig.template.models.section.elements.StringElement;
import eu.tecfox.profileconfig.testdata.TestDataGenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ProfileRepository.class, ProfileServiceImpl.class, TemplateServiceImpl.class,
    TemplateRepository.class})
@TestPropertySource(properties = {"spring.main.allow-bean-definition-overriding=true",
    "spring.cloud.discovery.enabled=false"})
class ProfileServiceImplTest {

    @MockBean
    ProfileRepository profileRepository;
    @MockBean
    TemplateRepository templateRepository;
    @Mock
    TemplateService templateService;

    @InjectMocks
    ProfileServiceImpl profileService;


    @Test
    void Should_ReturnProfile_If_NoTemplateFoundForMerge() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        when(templateService.getTemplate()).thenReturn(Optional.empty());
        Profile mergedProfile = profileService.mergeWithTemplate(profile);
        assertEquals(profile, mergedProfile);
    }

    @Test
    void Should_PatchBasicTemplateInformation() throws IOException {
        Profile profile = new Profile();
        profile.setSections(new ArrayList<>());
        profile.setUserId("userid");
        profile.setId("profileID");
        Template template = TestDataGenerator.generateValidTemplate();
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Profile mergedProfile = profileService.mergeWithTemplate(profile);
        assertEquals("userid", mergedProfile.getUserId());
        assertEquals("profileID", mergedProfile.getId());
        assertNotNull(mergedProfile.getTitle());
        assertNotNull(mergedProfile.getStyle());
        assertNotNull(mergedProfile.getFooter());
        assertNotNull(mergedProfile.getHeader());
    }

    @Test
    void Should_PatchSection_If_ItExistsInProfile() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        String value = ((StringElement)profile.getSections().get(0).getElements().get(0)).getValue();
        Template template = TestDataGenerator.generateValidTemplate();
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Profile patchedProfile = profileService.mergeWithTemplate(profile);
        assertEquals(value, ((StringElement)patchedProfile.getSections().get(0).getElements().get(0)).getValue());
    }

    @Test
    void Should_CopyTemplateSection_If_SectionDoesNotExistInProfile() throws IOException {
        Profile profile = TestDataGenerator.generateValidProfile();
        profile.getSections().remove(2);
        Template template = TestDataGenerator.generateValidTemplate();
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        Profile patchedProfile = profileService.mergeWithTemplate(profile);
        assertEquals(3, patchedProfile.getSections().size());
        assertEquals(1, patchedProfile.getSections().get(2).getElements().size());
    }


}