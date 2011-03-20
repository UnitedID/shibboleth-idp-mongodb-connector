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

import com.mongodb.ServerAddress;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector.BaseDataConnectorBeanDefinitionParser;
import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.unitedid.shibboleth.attribute.resolver.provider.dataConnector.MongoDbKeyAttributeMapper;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Spring definition configuration parser for MongoDB data connector */
public class MongoDbDataConnectorBeanDefinitionParser extends BaseDataConnectorBeanDefinitionParser {

    /** Data connector type name. */
    public static final QName NAME_TYPE = new QName(UIDDataConnectorNamespaceHandler.NAMESPACE, "MongoDbDataConnector");

    /** Name of QueryTemplate. */
    public static final QName QUERY_TEMPLATE_ELEMENT_NAME = new QName(UIDDataConnectorNamespaceHandler.NAMESPACE, "QueryTemplate");

    /** Name of Key tag. */
    public static final QName KEY_ELEMENT_NAME = new QName(UIDDataConnectorNamespaceHandler.NAMESPACE, "Key");

    /** Name of MongoHost tag. */
    public static final QName HOST_ELEMENT_NAME = new QName(UIDDataConnectorNamespaceHandler.NAMESPACE, "MongoHost");

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(MongoDbDataConnectorBeanDefinitionParser.class);

    /** {@inheritDoc} */
    protected Class getBeanClass(Element element) {
        return MongoDbDataConnectorFactoryBean.class;

    }

    /** {@inheritDoc} */
    protected void doParse(String pluginId, Element pluginConfig, Map<QName, List<Element>> pluginConfigChildren,
                           BeanDefinitionBuilder pluginBuilder, ParserContext parserContext) {
        super.doParse(pluginId, pluginConfig, pluginConfigChildren, pluginBuilder, parserContext);

        String mongoDbName = pluginConfig.getAttributeNS(null, "mongoDbName");
        log.info("Data connector {} MONGODB DATABASE: {}", pluginId, mongoDbName);
        pluginBuilder.addPropertyValue("mongoDbName", mongoDbName);

        String mongoCollection = pluginConfig.getAttributeNS(null, "mongoCollection");
        log.info("Data connector {} MONGODB COLLECTION: {}", pluginId, mongoCollection);
        pluginBuilder.addPropertyValue("mongoCollection", mongoCollection);

        if (pluginConfig.hasAttributeNS(null, "mongoUser")) {
            String mongoUser = pluginConfig.getAttributeNS(null, "mongoUser");
            log.info("Data connector {} MONGODB USERNAME: {}", pluginId, mongoUser);
            pluginBuilder.addPropertyValue("mongoUser", mongoUser);
        }

        if (pluginConfig.hasAttributeNS(null, "mongoPassword")) {
            String mongoPassword = pluginConfig.getAttributeNS(null, "mongoPassword");
            pluginBuilder.addPropertyValue("mongoPassword", mongoPassword);
        }

        boolean cacheResults = false;
        if (pluginConfig.hasAttributeNS(null, "cacheResults")) {
            cacheResults = XMLHelper.getAttributeValueAsBoolean(pluginConfig.getAttributeNodeNS(null, "cacheResults"));
        }
        log.info("Data connector {} cache results: {}", pluginId, cacheResults);
        pluginBuilder.addPropertyValue("cacheResults", cacheResults);

        List<ServerAddress> hosts = parseMongoHostNames(pluginId, pluginConfigChildren, pluginBuilder);
        log.debug("Data connector {} hosts {}", pluginId, hosts.toString());
        pluginBuilder.addPropertyValue("mongoHost", hosts);

        List<MongoDbKeyAttributeMapper> keyAttributeMaps = parseAttributeMappings(pluginId, pluginConfigChildren, pluginBuilder);
        pluginBuilder.addPropertyValue("keyAttributeMap", keyAttributeMaps);

        String queryTemplate = pluginConfigChildren.get(QUERY_TEMPLATE_ELEMENT_NAME).get(0).getTextContent();
        queryTemplate = DatatypeHelper.safeTrimOrNullString(queryTemplate);
        log.debug("Data connector {} MONGODB Query template: {}", pluginId, queryTemplate);
        pluginBuilder.addPropertyValue("queryTemplate", queryTemplate);

        String templateEngineRef = pluginConfig.getAttributeNS(null, "templateEngine");
        pluginBuilder.addPropertyReference("templateEngine", templateEngineRef);
    }

    /**
     * Parse mongodb Key entries
     *
     * @param pluginId the id of this connector
     * @param pluginConfigChildren configuration elements
     * @param pluginBuilder the bean definition parser
     * @return the mongodb key attribute mappings
     */
    protected List<MongoDbKeyAttributeMapper> parseAttributeMappings(String pluginId,
                                                                     Map<QName, List<Element>> pluginConfigChildren,
                                                                     BeanDefinitionBuilder pluginBuilder) {

        List<MongoDbKeyAttributeMapper> keyAttributeMaps = new ArrayList<MongoDbKeyAttributeMapper>();
        MongoDbKeyAttributeMapper keyAttributeMap;
        String keyName;
        String attributeName;
        if (pluginConfigChildren.containsKey(KEY_ELEMENT_NAME)) {
            for (Element e : pluginConfigChildren.get(KEY_ELEMENT_NAME)) {
                keyName = DatatypeHelper.safeTrimOrNullString(e.getAttributeNS(null, "keyName"));
                attributeName = DatatypeHelper.safeTrimOrNullString(e.getAttributeNS(null, "attributeID"));
                keyAttributeMap = new MongoDbKeyAttributeMapper(keyName, attributeName);
                keyAttributeMaps.add(keyAttributeMap);
            }
            log.debug("Data connector {} key descriptors: {}", pluginId, keyAttributeMaps);
        }
        return keyAttributeMaps;
    }

    /**
     * Parse mongodb connection entries
     *
     * @param pluginId the id of this connector
     * @param pluginConfigChildren configuration elements
     * @param pluginBuilder the bean definition parser
     * @return the server addresses extracted from mongohost elements
     */
    protected List<ServerAddress> parseMongoHostNames(String pluginId,
                                                      Map<QName, List<Element>> pluginConfigChildren,
                                                      BeanDefinitionBuilder pluginBuilder) {
        List<ServerAddress> hosts = new ArrayList<ServerAddress>();
        String host;
        int port;

        if (pluginConfigChildren.containsKey(HOST_ELEMENT_NAME)) {
            for (Element e : pluginConfigChildren.get(HOST_ELEMENT_NAME)) {
                host = DatatypeHelper.safeTrimOrNullString(e.getAttributeNS(null, "hostName"));
                try {
                    if (e.hasAttributeNS(null, "port")) {
                        port = Integer.parseInt(e.getAttributeNS(null, "port"));
                        hosts.add(new ServerAddress(host, port));
                    } else {
                        hosts.add(new ServerAddress(host));
                    }
                } catch (UnknownHostException err) {
                    log.error("Data connector {} unknown host {}", pluginId, err);
                }
            }
        }
        return hosts;
    }
}
