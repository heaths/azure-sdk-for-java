/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.cosmosdb.v2019_08_01_preview;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The CompositePath model.
 */
public class CompositePath {
    /**
     * The path for which the indexing behavior applies to. Index paths
     * typically start with root and end with wildcard (/path/*).
     */
    @JsonProperty(value = "path")
    private String path;

    /**
     * Sort order for composite paths. Possible values include: 'Ascending',
     * 'Descending'.
     */
    @JsonProperty(value = "order")
    private CompositePathSortOrder order;

    /**
     * Get the path for which the indexing behavior applies to. Index paths typically start with root and end with wildcard (/path/*).
     *
     * @return the path value
     */
    public String path() {
        return this.path;
    }

    /**
     * Set the path for which the indexing behavior applies to. Index paths typically start with root and end with wildcard (/path/*).
     *
     * @param path the path value to set
     * @return the CompositePath object itself.
     */
    public CompositePath withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Get sort order for composite paths. Possible values include: 'Ascending', 'Descending'.
     *
     * @return the order value
     */
    public CompositePathSortOrder order() {
        return this.order;
    }

    /**
     * Set sort order for composite paths. Possible values include: 'Ascending', 'Descending'.
     *
     * @param order the order value to set
     * @return the CompositePath object itself.
     */
    public CompositePath withOrder(CompositePathSortOrder order) {
        this.order = order;
        return this;
    }

}
