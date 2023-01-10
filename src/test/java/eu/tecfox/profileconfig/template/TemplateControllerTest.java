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

import eu.tecfox.profileconfig.testdata.TestDataGenerator;
import java.nio.file.Paths;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tecfox.profileconfig.template.models.Template;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TemplateController.class)
@TestPropertySource(properties = {"spring.cloud.discovery.enabled=false"})
class TemplateControllerTest {

    @Autowired
    private MockMvc mvc;

    ObjectMapper mapper = new ObjectMapper();

    @MockBean
    TemplateService templateService;

    @Test
    void Should_GetTemplateIfExists() throws Exception {
        when(templateService.getTemplate()).thenReturn(Optional.of(new Template()));
        this.mvc.perform(get("/api/template"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
    }

    @Test
    void Should_ReturnExceptionIsNoTemplateExists() throws Exception {
        when(templateService.getTemplate()).thenReturn(Optional.empty());
        this.mvc.perform(get("/api/template"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.message").value("No template found."));
    }

    @Test
    void Should_SaveTemplate_If_TemplateIsValid() throws Exception {
        Template template = TestDataGenerator.generateValidTemplate();
        when(templateService.saveTemplate(any())).thenReturn(template);
        this.mvc.perform(post("/api/template")
                .content(mapper.writeValueAsString(template))
                .contentType("application/json"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
    }

    @Test
    void Should_ReturnException_If_TemplateIsInvalid() throws Exception {
        Template template = TestDataGenerator.generateValidTemplate();
        template.setTitle("");
        when(templateService.saveTemplate(any())).thenReturn(template);
        this.mvc.perform(post("/api/template")
                .content(mapper.writeValueAsString(template))
                .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }
}