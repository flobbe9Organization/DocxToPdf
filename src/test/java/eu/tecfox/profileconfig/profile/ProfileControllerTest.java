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

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tecfox.profileconfig.profile.models.Profile;
import eu.tecfox.profileconfig.template.TemplateService;
import eu.tecfox.profileconfig.template.models.Template;
import eu.tecfox.profileconfig.testdata.TestDataGenerator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(ProfileController.class)
@TestPropertySource(properties = {"spring.cloud.discovery.enabled=false"})
class ProfileControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    ProfileService profileService;

    @MockBean
    TemplateService templateService;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void Should_ReturnException_If_ProfileOfUserNotFound() throws Exception {
        when(profileService.findByUserId("abcd")).thenReturn(Optional.empty());
        this.mvc.perform(get("/api/profile/abcd"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.message").value("User does not have a profile yet."));
    }

    @Test
    void Should_ReturnProfile_If_ProfileOfUserFound() throws Exception {
        Profile profile = TestDataGenerator.generateValidProfile();
        when(profileService.findByUserId("abcd")).thenReturn(Optional.of(profile));
        this.mvc.perform(get("/api/profile/abcd"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
    }

    @Test
    void Should_ReturnTemplate_If_CurrentUserHasNoProfile() throws Exception {
        Template template = TestDataGenerator.generateValidTemplate();
        when(profileService.findByUserId(any())).thenReturn(Optional.empty());
        when(templateService.getTemplate()).thenReturn(Optional.of(template));
        MvcResult result =  this.mvc.perform(get("/api/profile"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andReturn();
        Template resultTemplate = mapper.readValue(result.getResponse().getContentAsString(), Template.class);
        assertEquals(template.getTitle(), resultTemplate.getTitle());
    }

    @Test
    void Should_ReturnCurrentProfile_If_CurrentUserHasProfileAndNoMergeRequested() throws Exception {
        Profile profile = TestDataGenerator.generateValidProfile();
        when(profileService.findByUserId(any())).thenReturn(Optional.of(profile));
        MvcResult result =  this.mvc.perform(get("/api/profile"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andReturn();
        Profile resultProfile = mapper.readValue(result.getResponse().getContentAsString(), Profile.class);
        assertEquals(profile.getTitle(), resultProfile.getTitle());
    }

    @Test
    void Should_ReturnMergedProfile_If_CurrentUserHasProfileAndMergeRequested() throws Exception {
        Profile profile = TestDataGenerator.generateValidProfile();
        when(profileService.findByUserId(any())).thenReturn(Optional.of(profile));
        Profile deepCopy = mapper
            .readValue(mapper.writeValueAsString(profile), Profile.class);
        deepCopy.setTitle("New Title");
        when(profileService.mergeWithTemplate(profile)).thenReturn(deepCopy);
        MvcResult result =  this.mvc.perform(get("/api/profile?merge=true"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andReturn();
        Profile resultProfile = mapper.readValue(result.getResponse().getContentAsString(), Profile.class);
        assertEquals("New Title", resultProfile.getTitle());
    }




}