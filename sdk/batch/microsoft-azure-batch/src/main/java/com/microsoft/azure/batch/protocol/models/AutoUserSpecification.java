/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Specifies the parameters for the auto user that runs a Task on the Batch
 * service.
 */
public class AutoUserSpecification {
    /**
     * The scope for the auto user.
     * The default value is Task. Possible values include: 'task', 'pool'.
     */
    @JsonProperty(value = "scope")
    private AutoUserScope scope;

    /**
     * The elevation level of the auto user.
     * The default value is nonAdmin. Possible values include: 'nonAdmin',
     * 'admin'.
     */
    @JsonProperty(value = "elevationLevel")
    private ElevationLevel elevationLevel;

    /**
     * Get the default value is Task. Possible values include: 'task', 'pool'.
     *
     * @return the scope value
     */
    public AutoUserScope scope() {
        return this.scope;
    }

    /**
     * Set the default value is Task. Possible values include: 'task', 'pool'.
     *
     * @param scope the scope value to set
     * @return the AutoUserSpecification object itself.
     */
    public AutoUserSpecification withScope(AutoUserScope scope) {
        this.scope = scope;
        return this;
    }

    /**
     * Get the default value is nonAdmin. Possible values include: 'nonAdmin', 'admin'.
     *
     * @return the elevationLevel value
     */
    public ElevationLevel elevationLevel() {
        return this.elevationLevel;
    }

    /**
     * Set the default value is nonAdmin. Possible values include: 'nonAdmin', 'admin'.
     *
     * @param elevationLevel the elevationLevel value to set
     * @return the AutoUserSpecification object itself.
     */
    public AutoUserSpecification withElevationLevel(ElevationLevel elevationLevel) {
        this.elevationLevel = elevationLevel;
        return this;
    }

}
