/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.sql.v2017_03_01_preview;

import com.microsoft.azure.arm.collection.SupportsCreating;
import rx.Completable;
import rx.Observable;
import com.microsoft.azure.management.sql.v2017_03_01_preview.implementation.SensitivityLabelsInner;
import com.microsoft.azure.arm.model.HasInner;
import com.microsoft.azure.management.sql.v2017_03_01_preview.DatabasisServerSensitivityLabel;

/**
 * Type representing SensitivityLabels.
 */
public interface SensitivityLabels extends SupportsCreating<SensitivityLabel.DefinitionStages.Blank>, HasInner<SensitivityLabelsInner> {
    /**
     * Enables sensitivity recommendations on a given column (recommendations are enabled by default on all columns).
     *
     * @param resourceGroupName The name of the resource group that contains the resource. You can obtain this value from the Azure Resource Manager API or the portal.
     * @param serverName The name of the server.
     * @param databaseName The name of the database.
     * @param schemaName The name of the schema.
     * @param tableName The name of the table.
     * @param columnName The name of the column.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Completable enableRecommendationAsync(String resourceGroupName, String serverName, String databaseName, String schemaName, String tableName, String columnName);

    /**
     * Disables sensitivity recommendations on a given column.
     *
     * @param resourceGroupName The name of the resource group that contains the resource. You can obtain this value from the Azure Resource Manager API or the portal.
     * @param serverName The name of the server.
     * @param databaseName The name of the database.
     * @param schemaName The name of the schema.
     * @param tableName The name of the table.
     * @param columnName The name of the column.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Completable disableRecommendationAsync(String resourceGroupName, String serverName, String databaseName, String schemaName, String tableName, String columnName);

    /**
     * Gets the sensitivity label of a given column.
     *
     * @param resourceGroupName The name of the resource group that contains the resource. You can obtain this value from the Azure Resource Manager API or the portal.
     * @param serverName The name of the server.
     * @param databaseName The name of the database.
     * @param schemaName The name of the schema.
     * @param tableName The name of the table.
     * @param columnName The name of the column.
     * @param sensitivityLabelSource The source of the sensitivity label. Possible values include: 'current', 'recommended'
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Observable<SensitivityLabel> getAsync(String resourceGroupName, String serverName, String databaseName, String schemaName, String tableName, String columnName, SensitivityLabelSource sensitivityLabelSource);

    /**
     * Deletes the sensitivity label of a given column.
     *
     * @param resourceGroupName The name of the resource group that contains the resource. You can obtain this value from the Azure Resource Manager API or the portal.
     * @param serverName The name of the server.
     * @param databaseName The name of the database.
     * @param schemaName The name of the schema.
     * @param tableName The name of the table.
     * @param columnName The name of the column.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Completable deleteAsync(String resourceGroupName, String serverName, String databaseName, String schemaName, String tableName, String columnName);

    /**
     * Gets the sensitivity labels of a given database.
     *
     * @param resourceGroupName The name of the resource group that contains the resource. You can obtain this value from the Azure Resource Manager API or the portal.
     * @param serverName The name of the server.
     * @param databaseName The name of the database.
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the observable for the request
     */
    Observable<DatabasisServerSensitivityLabel> listCurrentByDatabaseAsync(final String resourceGroupName, final String serverName, final String databaseName);

}
