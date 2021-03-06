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
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.management.datafactory.v2018_06_01.implementation.DatasetInner;

/**
 * A Rest service dataset.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = RestResourceDataset.class)
@JsonTypeName("RestResource")
@JsonFlatten
public class RestResourceDataset extends DatasetInner {
    /**
     * The relative URL to the resource that the RESTful API provides. Type:
     * string (or Expression with resultType string).
     */
    @JsonProperty(value = "typeProperties.relativeUrl")
    private Object relativeUrl;

    /**
     * The HTTP method used to call the RESTful API. The default is GET. Type:
     * string (or Expression with resultType string).
     */
    @JsonProperty(value = "typeProperties.requestMethod")
    private Object requestMethod;

    /**
     * The HTTP request body to the RESTful API if requestMethod is POST. Type:
     * string (or Expression with resultType string).
     */
    @JsonProperty(value = "typeProperties.requestBody")
    private Object requestBody;

    /**
     * The additional HTTP headers in the request to the RESTful API. Type:
     * string (or Expression with resultType string).
     */
    @JsonProperty(value = "typeProperties.additionalHeaders")
    private Object additionalHeaders;

    /**
     * The pagination rules to compose next page requests. Type: string (or
     * Expression with resultType string).
     */
    @JsonProperty(value = "typeProperties.paginationRules")
    private Object paginationRules;

    /**
     * Get the relative URL to the resource that the RESTful API provides. Type: string (or Expression with resultType string).
     *
     * @return the relativeUrl value
     */
    public Object relativeUrl() {
        return this.relativeUrl;
    }

    /**
     * Set the relative URL to the resource that the RESTful API provides. Type: string (or Expression with resultType string).
     *
     * @param relativeUrl the relativeUrl value to set
     * @return the RestResourceDataset object itself.
     */
    public RestResourceDataset withRelativeUrl(Object relativeUrl) {
        this.relativeUrl = relativeUrl;
        return this;
    }

    /**
     * Get the HTTP method used to call the RESTful API. The default is GET. Type: string (or Expression with resultType string).
     *
     * @return the requestMethod value
     */
    public Object requestMethod() {
        return this.requestMethod;
    }

    /**
     * Set the HTTP method used to call the RESTful API. The default is GET. Type: string (or Expression with resultType string).
     *
     * @param requestMethod the requestMethod value to set
     * @return the RestResourceDataset object itself.
     */
    public RestResourceDataset withRequestMethod(Object requestMethod) {
        this.requestMethod = requestMethod;
        return this;
    }

    /**
     * Get the HTTP request body to the RESTful API if requestMethod is POST. Type: string (or Expression with resultType string).
     *
     * @return the requestBody value
     */
    public Object requestBody() {
        return this.requestBody;
    }

    /**
     * Set the HTTP request body to the RESTful API if requestMethod is POST. Type: string (or Expression with resultType string).
     *
     * @param requestBody the requestBody value to set
     * @return the RestResourceDataset object itself.
     */
    public RestResourceDataset withRequestBody(Object requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    /**
     * Get the additional HTTP headers in the request to the RESTful API. Type: string (or Expression with resultType string).
     *
     * @return the additionalHeaders value
     */
    public Object additionalHeaders() {
        return this.additionalHeaders;
    }

    /**
     * Set the additional HTTP headers in the request to the RESTful API. Type: string (or Expression with resultType string).
     *
     * @param additionalHeaders the additionalHeaders value to set
     * @return the RestResourceDataset object itself.
     */
    public RestResourceDataset withAdditionalHeaders(Object additionalHeaders) {
        this.additionalHeaders = additionalHeaders;
        return this;
    }

    /**
     * Get the pagination rules to compose next page requests. Type: string (or Expression with resultType string).
     *
     * @return the paginationRules value
     */
    public Object paginationRules() {
        return this.paginationRules;
    }

    /**
     * Set the pagination rules to compose next page requests. Type: string (or Expression with resultType string).
     *
     * @param paginationRules the paginationRules value to set
     * @return the RestResourceDataset object itself.
     */
    public RestResourceDataset withPaginationRules(Object paginationRules) {
        this.paginationRules = paginationRules;
        return this;
    }

}
