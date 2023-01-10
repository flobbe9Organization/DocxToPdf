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

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.tecfox.profileconfig.exception.ApiRequestException;
import eu.tecfox.profileconfig.exception.BindingResultErrorFormatter;
import eu.tecfox.profileconfig.profile.models.Profile;
import eu.tecfox.profileconfig.profile.models.ValidProfile;
import eu.tecfox.profileconfig.template.TemplateService;
import eu.tecfox.profileconfig.template.models.Template;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that holds all endpoints related to profiles.
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final TemplateService templateService;

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Profile> getProfileByUserId(@PathVariable("id") String id) {
        // TODO secure this endpoint so only admins can use it. Normal users should only see their own profiles.
        Profile profile = profileService.findByUserId(id).orElseThrow(() -> new ApiRequestException("User does not have a profile yet.",
            HttpStatus.NOT_FOUND));
        return ResponseEntity.ok().body(profile);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Template> getProfile(@RequestParam(value = "merge", required = false) boolean merge) {
        // TODO get user id from session
        String userId = "";
        Optional<Profile> profile = profileService.findByUserId(userId);
        if (profile.isEmpty()) {
            Template template = templateService.getTemplate().orElseThrow(() ->
                new ApiRequestException("You don't have a profile yet and there is currently no template available."));
            return ResponseEntity.ok().body(template);
        }
        Profile userProfile = profile.get();
        if (merge) {
            userProfile = profileService.mergeWithTemplate(userProfile);
        }
        return ResponseEntity.ok().body(userProfile);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Profile> saveProfile(@RequestBody @Validated(ValidProfile.class) Profile profile, BindingResult bindingResult)
        throws JsonProcessingException {
        if (bindingResult.hasErrors()) {
            throw new ApiRequestException(BindingResultErrorFormatter.getErrorMessagesAsJson(bindingResult));
        }
        String userId = ""; // TODO get user id from jwt
        // TODO get user id from session and load his profile from db and update profile id, so it can only affect his own profile
        return ResponseEntity.ok().body(profileService.save(profile));
    }
}
