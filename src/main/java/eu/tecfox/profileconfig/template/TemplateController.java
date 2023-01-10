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

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.tecfox.profileconfig.exception.ApiRequestException;
import eu.tecfox.profileconfig.exception.BindingResultErrorFormatter;
import eu.tecfox.profileconfig.template.models.Template;
import eu.tecfox.profileconfig.template.models.ValidTemplate;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that holds all endpoints related to templates.
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/template")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Template> getTemplate() {
        Template template = templateService.getTemplate().orElseThrow(() -> new ApiRequestException("No template found."));
        return ResponseEntity.ok().body(template);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Template> saveTemplate(@RequestBody @Validated(ValidTemplate.class) Template template, BindingResult bindingResult)
        throws JsonProcessingException {
        if (bindingResult.hasErrors()) {
            throw new ApiRequestException(BindingResultErrorFormatter.getErrorMessagesAsJson(bindingResult));
        }
        return ResponseEntity.ok().body(templateService.saveTemplate(template));
    }
}
