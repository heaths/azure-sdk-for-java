/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.datafactory.v2018_06_01;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A copy activity Salesforce source.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = SalesforceSource.class)
@JsonTypeName("SalesforceSource")
public class SalesforceSource extends TabularSource {
    /**
     * Database query. Type: string (or Expression with resultType string).
     */
    @JsonProperty(value = "query")
    private Object query;

    /**
     * The read behavior for the operation. Default is Query. Possible values
     * include: 'Query', 'QueryAll'.
     */
    @JsonProperty(value = "readBehavior")
    private SalesforceSourceReadBehavior readBehavior;

    /**
     * Get database query. Type: string (or Expression with resultType string).
     *
     * @return the query value
     */
    public Object query() {
        return this.query;
    }

    /**
     * Set database query. Type: string (or Expression with resultType string).
     *
     * @param query the query value to set
     * @return the SalesforceSource object itself.
     */
    public SalesforceSource withQuery(Object query) {
        this.query = query;
        return this;
    }

    /**
     * Get the read behavior for the operation. Default is Query. Possible values include: 'Query', 'QueryAll'.
     *
     * @return the readBehavior value
     */
    public SalesforceSourceReadBehavior readBehavior() {
        return this.readBehavior;
    }

    /**
     * Set the read behavior for the operation. Default is Query. Possible values include: 'Query', 'QueryAll'.
     *
     * @param readBehavior the readBehavior value to set
     * @return the SalesforceSource object itself.
     */
    public SalesforceSource withReadBehavior(SalesforceSourceReadBehavior readBehavior) {
        this.readBehavior = readBehavior;
        return this;
    }

}
