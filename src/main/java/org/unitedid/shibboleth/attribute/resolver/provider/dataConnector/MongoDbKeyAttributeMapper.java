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
 */

package org.unitedid.shibboleth.attribute.resolver.provider.dataConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * A mapping class between mongo document keys and shibboleth attributes
 *
 * @author Stefan Wold <stefan.wold@unitedid.org>
 */
public class MongoDbKeyAttributeMapper {

    /** The mongo document key */
    private String mongoKey;

    /** The shibboleth attribute */
    private String attributeName;

    private List<MongoDbKeyAttributeMapper> childKeyAttributeMaps = new ArrayList<MongoDbKeyAttributeMapper>();

    /**
     * Constructor
     *
     * @param key the mongo document key
     * @param attribute the shibboleth attribute
     */
    public MongoDbKeyAttributeMapper(String key, String attribute) {
        mongoKey = key;
        attributeName = attribute;
    }

    /**
     * Gets the key name
     *
     * @return the key name
     */
    public String getMongoKey() {
        return mongoKey;
    }

    /**
     * Gets the attribute name
     *
     * @return the attribute name
     */
    public String getAttributeName() {
        return attributeName;
    }

    public List<MongoDbKeyAttributeMapper> getChildKeyAttributeMaps() {
        return childKeyAttributeMaps;
    }

    public void setChildKeyAttributeMaps(List<MongoDbKeyAttributeMapper> childKeyAttributeMaps) {
        this.childKeyAttributeMaps = childKeyAttributeMaps;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "MongoDbKeyAttributeMapper{mongoKey=" + mongoKey + ", attributeId=" + attributeName + ", children=" + childKeyAttributeMaps.size() + "}";
    }

}
