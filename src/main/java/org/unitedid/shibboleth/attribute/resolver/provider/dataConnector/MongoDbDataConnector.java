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

package org.unitedid.shibboleth.attribute.resolver.provider.dataConnector;

import com.mongodb.*;
import com.mongodb.util.JSON;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.AttributeResolutionException;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.ShibbolethResolutionContext;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.BaseDataConnector;
import edu.internet2.middleware.shibboleth.common.attribute.resolver.provider.dataConnector.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.*;

/**
 * <code>MongoDbDataConnector</code> provides a plugin to fetch attributes from a mongo database.
 */
public class MongoDbDataConnector extends BaseDataConnector implements ApplicationListener {

    /** Class logger. */
    private final Logger log = LoggerFactory.getLogger(MongoDbDataConnector.class);

    /** Indicates if this connector has been initialized. */
    private boolean initialized;

    /** A list of mongo database servers. */
    private List<ServerAddress> mongoHost;

    /** Mongo database. */
    private String mongoDbName;

    /** Mongo collection */
    private String mongoCollection;

    /** Username */
    private String mongoUser;

    /** Password */
    private String mongoPassword;

    /** Whether the query results should be cached */
    private boolean cacheResults;

    /** Data cache */
    private Map<String, Map<String, Map<String, BaseAttribute>>> dataCache;

    /** Mongo connection object. */
    private Mongo mongoCon;

    /** Mapping object between mongo document keys and shibboleth attributes */
    private Map<String, MongoDbKeyAttributeMapper> keyAttributeMap;

    /** DB ojbect. */
    private DB db;

    /** Mongo collection object. */
    private DBCollection collection;

    /** Template name that produces the query to use. */
    private String queryTemplateName;

    /** Template that produces the query to use. */
    private String queryTemplate;

    /** Template engine that create a real query from the query template. */
    private TemplateEngine queryCreator;

    /**
     * Constructor
     *
     * @param hosts the list of hosts to connect to
     * @param dbName the mongo database name to use
     * @param collection the mongo collection name to use
     */
    public MongoDbDataConnector(List<ServerAddress> hosts, String dbName, String collection) {
        super();
        mongoHost = hosts;
        mongoDbName = dbName;
        mongoCollection = collection;
        keyAttributeMap = new HashMap<String, MongoDbKeyAttributeMapper>();
    }

    /**
     * Initializes the connector
     */
    public void initialize() {
        initialized = true;
        registerTemplate();
        initializeMongoDbConnection();
        initializeCache();
    }

    /**
     * Gets the engine used to build the query
     *
     * @return engine used to build the query
     */
    public TemplateEngine getTemplateEngine() {
        return queryCreator;
    }

    /**
     * Sets the engine used to build the query
     *
     * @param engine used to build the query
     */
    public void setTemplateEngine(TemplateEngine engine) {
        queryCreator = engine;
        registerTemplate();
        resetCache();
    }

    /**
     * Gets the mongo database username
     *
     * @return mongo database username
     */
    public String getMongoUser() {
        return mongoUser;
    }

    /**
     * Sets the mongo database username
     *
     * @param user the mongo database username
     */
    public void setMongoUser(String user) {
        mongoUser = user;
    }

    /**
     * Gets the mongo database password
     *
     * @return mongo database password
     */
    public String getMongoPassword() {
        return mongoPassword;
    }

    /**
     * Sets the mongo database password
     *
     * @param password the mongo database password
     */
    public void setMongoPassword(String password) {
        mongoPassword = password;
    }

    /**
     * Gets whether to cache search results
     *
     * @return cacheResults whether to cache query result
     */
    public boolean isCacheResults() {
        return cacheResults;
    }

    /**
     * Sets whether to cache search results
     *
     * @param cache whether to cache search results
     */
    public void setCacheResults(boolean cache) {
        cacheResults = cache;
    }

    /**
     * Gets the template used to create a query
     *
     * @return template used to createa a query
     */
    public String getQueryTemplate() {
        return queryTemplate;
    }

    /**
     * Sets the template used to create a query
     *
     * @param template used to create a query
     */
    public void setQueryTemplate(String template) {
        queryTemplate = template;
        resetCache();
    }

    /**
     * Gets a list of key and attribute mappings
     *
     * @return list of key and attribute mappings
     */
    public Map<String, MongoDbKeyAttributeMapper> getKeyAttributeMap() {
        return keyAttributeMap;
    }

    /**
     * Sets a map of key and attribute mappings
     *
     * @param map of key and attribute mappings
     */
    public void setKeyAttributeMap(Map<String, MongoDbKeyAttributeMapper> map) {
        keyAttributeMap = map;
    }

    /**
     * Creates the mongo database connection
     */
    protected void initializeMongoDbConnection() {
        if (initialized) {
            log.debug("MongoDB connector initializing!");
            mongoCon = new Mongo(mongoHost);

            // Ok to read from slave if more than one server is configured
            if (mongoHost.size() > 1) {
                mongoCon.slaveOk();
            }
            db = mongoCon.getDB(mongoDbName);
            if (mongoUser != null) {
                boolean dbAuth = db.authenticate(mongoUser, mongoPassword.toCharArray());
                if (!dbAuth) {
                    log.error("MongoDB data connector {} authentication failed for database {}, username or password!", getId(), mongoDbName);
                } else {
                    log.debug("MongoDB data connector {} authentication successfull!", getId());
                }
            }
        }
    }

    /**
     * Initialize search results cache
     */
    protected void initializeCache() {
        if (cacheResults && initialized) {
            dataCache = new HashMap<String, Map<String, Map<String, BaseAttribute>>>();
        }
    }

    /**
     * Reset search results cache
     */
    protected void resetCache() {
        if (cacheResults && initialized) {
            dataCache.clear();
        }
    }

    /**
     * Registers the query template with template engine.
     */
    protected void registerTemplate() {
        if (initialized) {
            queryTemplateName = "shibboleth.resolver.dc." + getId();
            queryCreator.registerTemplate(queryTemplateName, queryTemplate);
        }
    }

    /** {@inheritDoc} */
    public Map<String, BaseAttribute> resolve(ShibbolethResolutionContext resolutionContext) throws AttributeResolutionException {
        String query = queryCreator.createStatement(queryTemplateName, resolutionContext, getDependencyIds(), null);
        log.debug("Search Query: {}", query);

        Map<String, BaseAttribute> resolvedAttributes = new HashMap<String, BaseAttribute>();

        if (cacheResults) {
            log.debug("Data connector {} checking cache for attributes.", getId());
            resolvedAttributes = getAttributeCache(resolutionContext, query);
        }

        if (resolvedAttributes == null) {
            resolvedAttributes = retrieveAttributesFromDatabase(query);
            if (cacheResults && resolvedAttributes != null) {
                setAttributeCache(resolutionContext, query, resolvedAttributes);
            }
        }

        return resolvedAttributes;
    }

    /**
     * Retrieves cached attributes for current resolution context
     *
     * @param resolutionContext <code>ResolutionContext</code>
     * @param query the query used to get the search result
     * @return cached attributes
     */
    protected Map<String,BaseAttribute> getAttributeCache(ShibbolethResolutionContext resolutionContext, String query) {
        if (cacheResults) {
            String principal = resolutionContext.getAttributeRequestContext().getPrincipalName();
            Map<String, Map<String, BaseAttribute>> cache = dataCache.get(principal);
            if (cache != null) {
                Map<String, BaseAttribute> attributes = cache.get(query);
                log.debug("Data connector {} got cached attributes for principal: {}", getId(), principal);
                return attributes;
            }
        }

        return null;
    }

    /**
     * Stores the attributes in the cache
     *
     * @param resolutionContext <code>ResolutionContext</code>
     * @param query the query used to get the search result
     * @param resolvedAttributes <code>Map</code> of attribute ids to attribute
     */
    protected void setAttributeCache(ShibbolethResolutionContext resolutionContext, String query, Map<String,BaseAttribute> resolvedAttributes) {
        String principal = resolutionContext.getAttributeRequestContext().getPrincipalName();
        Map<String, Map<String, BaseAttribute>> cache = dataCache.get(principal);
        if (cache == null) {
            cache = new HashMap<String, Map<String, BaseAttribute>>();
            dataCache.put(principal, cache);
        }
        cache.put(query, resolvedAttributes);
    }

    /** {@inheritDoc} */
    public void validate() throws AttributeResolutionException {
        log.debug("Validating data connector {} configuration.", getId());
        try {
            Mongo connection = new Mongo(mongoHost);
            if (mongoHost.size() > 1) {
                log.debug("Data connector {} slave OK", getId());
                connection.slaveOk();
            }
            if (connection == null) {
                log.error("Unable to create connections using {} data connector ", getId());
                throw new AttributeResolutionException("Unable to create connections using " + getId() + " data connector.");
            }
        } catch (MongoException e) {
            log.error("Unable to validate {} data connector", getId(), e);
            throw new AttributeResolutionException("Unable to validate " + getId() + " data connector: ", e);
        }
    }

    /** {@inheritDoc} */
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Retrieve attributes from the database based on the query
     *
     * @param q the query to run
     * @return a list of attributes
     * @throws AttributeResolutionException
     */
    protected Map<String, BaseAttribute> retrieveAttributesFromDatabase(String q) throws AttributeResolutionException {
        Map <String, BaseAttribute> resolvedAttributes = new HashMap<String, BaseAttribute>();
        try {
            log.debug("Data connector {} retrieving attributes from: {}", getId(), db.getMongo().getAddress());
            DBCollection collection = db.getCollection(mongoCollection);
            DBObject query = (DBObject) JSON.parse(q);
            resolvedAttributes = processCollectionResult(collection.findOne(query));

        } catch (MongoException e) {
            log.error("Data connector {} exception", getId(), e);
            throw new AttributeResolutionException("MongoDB data connector " + getId() + " unable to execute query", e);
        }

        return resolvedAttributes;
    }

    /**
     * Process the result from the query and return a list of resolved attributes
     *
     * @param result the result from the query
     * @return a list of resolved attributes
     * @throws AttributeResolutionException
     */
    protected Map<String, BaseAttribute> processCollectionResult(DBObject result) throws AttributeResolutionException {
        Map<String, BaseAttribute> attributes = new HashMap<String, BaseAttribute>();
        try {
            MongoDbKeyAttributeMapper keyAttributeMapper;
            BaseAttribute attribute;

            if (result != null) {
                for (String keyName : result.keySet()) {
                    log.debug("Processing mongodb key: {}", keyName);
                    keyAttributeMapper = keyAttributeMap.get(keyName);
                    if (keyAttributeMapper == null || keyAttributeMapper.getAttributeName() == null) {
                        attribute = attributes.get(keyName);
                        if (attribute == null) {
                            attribute = new BasicAttribute(keyName);
                        }
                    } else {
                        attribute = attributes.get(keyAttributeMapper.getAttributeName());
                        if (attribute == null) {
                            attribute = new BasicAttribute(keyAttributeMapper.getAttributeName());
                        }
                    }
                    attribute.getValues().add(result.get(keyName));
                    attributes.put(attribute.getId(), attribute);
                }
            }
        } catch (MongoException e) {
            log.error("Problem processing result {}:", getId(), e);
            throw new AttributeResolutionException("Problem processing result " + getId() + ":", e);
        }
        log.debug("MongoDb data connector {} attribute result: {}", getId(), attributes.keySet());
        return attributes;
    }
}
