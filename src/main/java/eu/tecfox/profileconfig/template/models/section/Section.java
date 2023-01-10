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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.tecfox.profileconfig.profile.models.Patchable;
import eu.tecfox.profileconfig.template.models.section.elements.SectionElement;
import eu.tecfox.profileconfig.template.models.style.Style;

import lombok.Data;

/**
 * A Section used in {@link eu.tecfox.profileconfig.template.models.Template} that
 * indicates a cluster of {@link SectionElement} that belong together.
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Section implements Patchable<Section> {

    private String title;
    private List<SectionElement> elements;
    private boolean showTitle;
    private Style style;
    private String identifier;

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Section s)) {
            return false;
        }
        if (s.getIdentifier() == null || this.getIdentifier() == null) {
            return false;
        }
        return s.getIdentifier().equals(this.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, getIdentifier());
    }


    @Override
    public void patch(Section entityToMerge) {
        if (entityToMerge == null) {
            return;
        }
        entityToMerge.getElements().retainAll(this.elements);
        final Map<String, SectionElement> mergeValuesMap = entityToMerge.getElements()
            .stream()
            .distinct()
            .collect(Collectors.toMap(SectionElement::getIdentifier, Function.identity()));
        for (int i = 0; i < this.elements.size(); i++) {
            if (this.elements.get(i).isUnique()) {
                this.elements.get(i).patch(mergeValuesMap.get(this.elements.get(i).getIdentifier()));
            } else {
                int finalI = i;
                List<SectionElement> patched =
                    entityToMerge.getElements()
                        .stream()
                        .filter(s -> s.equals(this.elements.get(finalI)))
                        .toList()
                        .stream()
                        .map(multiEntryElement -> {
                            this.elements.get(finalI).patch(multiEntryElement);
                            return this.elements.get(finalI);
                        }).toList();
                int idx = this.elements.indexOf(this.elements.get(i));
                this.elements.remove(this.elements.get(i));
                this.elements.addAll(idx, patched);
                i += patched.size() - 1;
            }
        }

    }
}
