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

package eu.tecfox.profileconfig.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Context provider to provide application context and beans.
 *
 * <p>
 *     Required to access {@link eu.tecfox.profileconfig.template.TemplateService} in
 *     {@link eu.tecfox.profileconfig.profile.models.ProfileValidator}.
 * </p>
 *
 * @author Valentin Laucht
 * @version 1.0
 */
@Component
public class ContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ContextProvider.applicationContext = applicationContext;
    }

    public static Object getBean(Class cls) {
        return ContextProvider.applicationContext.getBean(cls);
    }

}