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

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.tecfox.profileconfig.profile.models.Profile;
import eu.tecfox.profileconfig.template.TemplateService;
import eu.tecfox.profileconfig.template.models.Template;
import eu.tecfox.profileconfig.template.models.section.Section;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final TemplateService templateService;

    /**
     * Finds a profile by user id if it exists.
     *
     * @param id the id of the user
     * @return the profile that belongs to this user
     */
    @Override
    public Optional<Profile> findByUserId(String id) {
        return profileRepository.findByUserId(id);
    }

    /**
     * Saves a profile to the database or updates an existing profile.
     *
     * <p>
     *     Profile will be saved, if no ID exists.
     *     Profile will be updated, if ID already exists in database.
     * </p>
     * @param profile the profile to save or update
     * @return the saved profile with ID
     */
    @Override
    public Profile save(Profile profile) {
        return profileRepository.save(profile);
    }

    /**
     * Method to merge the latest template with a profile.
     *
     * <p>
     *     Gets the latest template from the database and copies it into
     *     a new profile. All values from the existing profile are copied
     *     into the corresponding element in the new profile that matches the template.
     *     This ensures, that any changes in the template will reflect in the
     *     profile.
     *     If any sections or elements are removed from the template that exist in
     *     the profile, the values will be lost.
     *     If any sections or elements are added in the template, the values will be
     *     empty in the new profile.
     * </p>
     *
     * @param profile the existing profile
     * @return a new profile that matches the template and is filled with the values of the old profile
     */
    @Override
    public Profile mergeWithTemplate(Profile profile) {
        Optional<Template> templateOptional = templateService.getTemplate();
        if (templateOptional.isEmpty()) {
            return profile;
        }
        Template template = templateOptional.get();
        Profile patchedProfile = new Profile(template);
        patchedProfile.patch(profile);

        final Map<Object, Section> sectionMap = profile.getSections().stream()
            .collect(Collectors.toMap(Section::getIdentifier, Function.identity()));

        patchedProfile.getSections().forEach(s -> s.patch(sectionMap.get(s.getIdentifier())));
        return patchedProfile;
    }

}
