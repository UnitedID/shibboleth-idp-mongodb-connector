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
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.TemplateEngine;
import edu.internet2.middleware.shibboleth.common.config.attribute.resolver.dataConnector.BaseDataConnectorFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedid.shibboleth.attribute.resolver.provider.dataConnector.MongoDbDataConnector;
import org.unitedid.shibboleth.attribute.resolver.provider.dataConnector.MongoDbKeyAttributeMapper;

import java.util.List;
import java.util.Map;

/**
 * Spring factory for creating {@link MongoDbDataConnector} beans.
 */
public class MongoDbDataConnectorFactoryBean extends BaseDataConnectorFactoryBean {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(MongoDbDataConnectorFactoryBean.class);

    /** Template engine used to construct queries. */
    private TemplateEngine templateEngine;

    /** Mapping object between mongo document keys and shibboleth attributes */
    private List<MongoDbKeyAttributeMapper> keyAttributeMap;

    /** A list of mongo database servers */
    private List<ServerAddress> mongoHost;

    /** Mongo database */
    private String mongoDbName;

    /** Collection */
    private String mongoCollection;

    /** Username */
    private String mongoUser;

    /** Password */
    private String mongoPassword;

    /** Template that create the query to use */
    private String queryTemplate;

    /** {@inheritDoc} */
    protected Object createInstance() throws Exception {
        log.debug("MongoDbDataConnectorFactoryBean createInstance");
        MongoDbDataConnector connector = new MongoDbDataConnector(getMongoHost(), getMongoDbName(), getMongoCollection());
        populateDataConnector(connector);
        connector.setQueryTemplate(getQueryTemplate());
        connector.setTemplateEngine(getTemplateEngine());

        if (getKeyAttributeMap() != null) {
            Map<String, MongoDbKeyAttributeMapper> keyAttributeMap = connector.getKeyAttributeMap();
            for (MongoDbKeyAttributeMapper attributeMapper : getKeyAttributeMap()) {
                keyAttributeMap.put(attributeMapper.getKeyName(), attributeMapper);
            }
        }
        connector.initialize();

        return connector;
    }

    /** {@inheritDoc} */
    public Class getObjectType() {
        return MongoDbDataConnector.class;
    }

    /**
     * Gets the engine used to build the query
     *
     * @return engine used to build the query
     */

    public TemplateEngine getTemplateEngine() {
        return templateEngine;
    }

    /**
      * Sets the engine used to build the query
      *
      * @param engine used to build the query
      */
     public void setTemplateEngine(TemplateEngine engine) {
        templateEngine = engine;
    }

    /**
     * Gets a list of key and attribute mappings
     *
     * @return list of key and attribute mappings
     */
    public List<MongoDbKeyAttributeMapper> getKeyAttributeMap() {
        return keyAttributeMap;
    }

    /**
     * Sets a list of key and attribute mappings
     *
     * @param list of key and attribute mappings
     */
    public void setKeyAttributeMap(List<MongoDbKeyAttributeMapper> list) {
        keyAttributeMap = list;
    }

    public List<ServerAddress> getMongoHost() {
        return mongoHost;
    }

    public void setMongoHost(List<ServerAddress> host) {
        mongoHost = host;
    }

    public String getMongoDbName() {
        return mongoDbName;
    }

    public void setMongoDbName(String dbName) {
        mongoDbName = dbName;
    }

    public String getMongoUser() {
        return mongoUser;
    }

    public void setMongoUser(String user) {
        mongoUser = user;
    }

    public String getMongoPassword() {
        return mongoPassword;
    }

    public void setMongoPassword(String password) {
        mongoPassword = password;
    }

    public String getMongoCollection() {
        return mongoCollection;
    }

    public void setMongoCollection(String collection) {
        mongoCollection = collection;
    }

    public String getQueryTemplate() {
        return queryTemplate;
    }

    public void setQueryTemplate(String query) {
        queryTemplate = query;
    }
}
