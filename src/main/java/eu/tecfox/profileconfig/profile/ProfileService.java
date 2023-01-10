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

import eu.tecfox.profileconfig.profile.models.Profile;

/**
 * Interface of the Profile Service for all business logic related to profiles.
 *
 * @author Valentin Laucht
 * @version 1.0
 */
public interface ProfileService {

    /**
     * Find a Profile of a specific user by its ID.
     *
     * @param id the id of the user
     * @return optional with the profile, if it exists
     */
    Optional<Profile> findByUserId(String id);

    /**
     * Method to save or update a profile.
     *
     * @param profile the profile to save or update
     * @return the saved profile
     */
    Profile save(Profile profile);


    /**
     * Method to merge a profile with the current template.
     *
     * @param profile the profile to merge
     * @return the merges profile
     */
    Profile mergeWithTemplate(Profile profile);
}
