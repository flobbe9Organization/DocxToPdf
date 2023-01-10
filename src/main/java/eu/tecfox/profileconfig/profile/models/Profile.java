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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.tecfox.profileconfig.template.models.Template;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entity of a Profile.
 *
 * <p>
 *     A Profile extends a {@link Template} and holds
 *     all values that a user fills in as well as the ID
 *     of the user.
 * </p>
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Data
@Document(collection = "profiles")
@EqualsAndHashCode(callSuper = true)
@ValidProfile(groups = {ValidProfile.class})
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class Profile extends Template implements Patchable<Profile> {


    /**
     * Constructor to create a profile based on a template.
     *
     * @param template the template to create the profile from
     */
    public Profile(Template template) {
        this.setStyle(template.getStyle());
        this.setSections(template.getSections());
        this.setHeader(template.getHeader());
        this.setFooter(template.getFooter());
        this.setTitle(template.getTitle());
    }

    /**
     * The ID of the user that owns this profile.
     */
    @JsonIgnore
    private String userId;


    /**
     * Patches all base data of a profile.
     *
     * <p>
     *     Copies the base values of a profile into this
     *     profile. Does NOT touch the sections. This
     *     is used to copy data from an existing profile into
     *     a template for updating.
     * </p>
     *
     * @param entityToMerge the profile that holds the relevant data
     */
    @Override
    public void patch(Profile entityToMerge) {
        this.setId(entityToMerge.getId());
        this.userId = entityToMerge.getUserId();
        this.setCreatedByUser(entityToMerge.getCreatedByUser());
        this.setModifiedByUser(entityToMerge.getModifiedByUser());
        this.setCreatedDate(entityToMerge.getCreatedDate());
        this.setLastModifiedDate(entityToMerge.getLastModifiedDate());
    }

    @Override
    public Object getIdentifier() {
        return userId;
    }
}
