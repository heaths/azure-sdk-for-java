/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.automation.v2018_06_30;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Definition of the runbook parameter type.
 */
public class RunbookParameter {
    /**
     * Gets or sets the type of the parameter.
     */
    @JsonProperty(value = "type")
    private String type;

    /**
     * Gets or sets a Boolean value to indicate whether the parameter is
     * mandatory or not.
     */
    @JsonProperty(value = "isMandatory")
    private Boolean isMandatory;

    /**
     * Get or sets the position of the parameter.
     */
    @JsonProperty(value = "position")
    private Integer position;

    /**
     * Gets or sets the default value of parameter.
     */
    @JsonProperty(value = "defaultValue")
    private String defaultValue;

    /**
     * Get gets or sets the type of the parameter.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set gets or sets the type of the parameter.
     *
     * @param type the type value to set
     * @return the RunbookParameter object itself.
     */
    public RunbookParameter withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get gets or sets a Boolean value to indicate whether the parameter is mandatory or not.
     *
     * @return the isMandatory value
     */
    public Boolean isMandatory() {
        return this.isMandatory;
    }

    /**
     * Set gets or sets a Boolean value to indicate whether the parameter is mandatory or not.
     *
     * @param isMandatory the isMandatory value to set
     * @return the RunbookParameter object itself.
     */
    public RunbookParameter withIsMandatory(Boolean isMandatory) {
        this.isMandatory = isMandatory;
        return this;
    }

    /**
     * Get get or sets the position of the parameter.
     *
     * @return the position value
     */
    public Integer position() {
        return this.position;
    }

    /**
     * Set get or sets the position of the parameter.
     *
     * @param position the position value to set
     * @return the RunbookParameter object itself.
     */
    public RunbookParameter withPosition(Integer position) {
        this.position = position;
        return this;
    }

    /**
     * Get gets or sets the default value of parameter.
     *
     * @return the defaultValue value
     */
    public String defaultValue() {
        return this.defaultValue;
    }

    /**
     * Set gets or sets the default value of parameter.
     *
     * @param defaultValue the defaultValue value to set
     * @return the RunbookParameter object itself.
     */
    public RunbookParameter withDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

}
