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

import java.io.Serializable;
import java.util.Date;

/**
 * Data object representing a persistent identifier entry in the database.
 */
public class PersistentIdEntry implements Serializable {

    /** ID of the entity that issued that identifier. */
    private String localEntityId;

    /** ID of the entity to which the identifier was issued. */
    private String peerEntityId;

    /** Local component portion of the persistent ID entry. */
    private String localId;

    /** The persistent identifier. */
    private String persistentId;

    /** Name of the principal represented by the identifier. */
    private String principalName;

    /** Date and time when the persistent identifier was created. */
    private Date creationTime;

    /** Date and time when the persistent identifier was deactivated. */
    private Date deactivationTime;

    /** Constructor */
    public PersistentIdEntry() {
    }

    /**
     * Gets the ID of the entity that issued that identifier.
     *
     * @return ID of the entity that issued that identifier.
     */
    public String getLocalEntityId() {
        return localEntityId;
    }

    /**
     * Sets the ID of the entity that issued that identifier.
     *
     * @param entityId ID of the entity that issued that identifier.
     */
    public void setLocalEntityId(String entityId) {
        localEntityId = entityId;
    }

    /**
     * Gets the ID of the entity to which the identifier was issued.
     *
     * @return ID of the entity to which the identifier was issued.
     */
    public String getPeerEntityId() {
        return peerEntityId;
    }

    /**
     * Sets the ID of the entity to which the identifier was issued.
     *
     * @param entityId ID of the entity to which the identifier was issued.
     */
    public void setPeerEntityId(String entityId) {
        peerEntityId = entityId;
    }

    /**
     * Gets the local component portion of the persistent ID entry.
     *
     * @return local component portion of the persistent ID entry.
     */
    public String getLocalId() {
        return localId;
    }

    /**
     * Sets the local component portion of the persistent ID entry.
     *
     * @param id local component portion of the persistent ID entry.
     */
    public void setLocalId(String id) {
        localId = id;
    }

    /**
     * Gets the persistent identifier.
     *
     * @return persistent identifier.
     */
    public String getPersistentId() {
        return persistentId;
    }

    /**
     * Sets the persistent identifier.
     *
     * @param id persistent identifier.
     */
    public void setPersistentId(String id) {
        persistentId = id;
    }

    /**
     * Gets the name of the principal represented by the identifier.
     *
     * @return name of the principal represented by the identifier.
     */
    public String getPrincipalName() {
        return principalName;
    }

    /**
     * Sets the name of the principal represented by the identifier.
     *
     * @param principal name of the principal represented by the identifier.
     */
    public void setPrincipalName(String principal) {
        principalName = principal;
    }

    /**
     * Gets the date and time when the persistent identifier was created.
     *
     * @return date and time when the persistent identifier was created.
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * Sets the date and time when the persistent identifier was created.
     *
     * @param date date and time when the persistent identifier was created.
     */
    public void setCreationTime(Date date) {
        creationTime = date;
    }

    /**
     * Gets the date and time when the persistent identifier was deactivated.
     *
     * @return date and time when the persistent identifier was deactivated.
     */
    public Date getDeactivationTime() {
        return deactivationTime;
    }

    /**
     * Sets the date and time when the persistent identifier was deactivated.
     *
     * @param date date and time when the persistent identifier was deactivated.
     */
    public void setDeactivationTime(Date date) {
        deactivationTime = date;
    }
}
