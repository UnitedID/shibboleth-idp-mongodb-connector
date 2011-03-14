/*
 * Copyright (c) 2011 United ID.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Stefan Wold <stefan.wold@unitedid.org>
 */

package org.unitedid.shibboleth.config.attribute.resolver.dataConnector;

import edu.internet2.middleware.shibboleth.common.config.BaseSpringNamespaceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A spring namespace handler for United ID's shibboleth data connector namespace
 */
public class UIDDataConnectorNamespaceHandler extends BaseSpringNamespaceHandler {
    /** Class logger */
    private static final Logger log = LoggerFactory.getLogger(UIDDataConnectorNamespaceHandler.class);

    /** Namespace for this handler */
    public static final String NAMESPACE = "http://dev.unitedid.org/NS/mongodb-connector";

    /** {@inheritDoc} */
    public void init() {
        log.debug("Registering a MongoDbDataConnectorBeanDefinitionParser against QName {}",
                MongoDbDataConnectorBeanDefinitionParser.NAME_TYPE);
        registerBeanDefinitionParser(MongoDbDataConnectorBeanDefinitionParser.NAME_TYPE,
                new MongoDbDataConnectorBeanDefinitionParser());
    }
}
