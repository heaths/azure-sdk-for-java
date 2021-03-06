/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 *
 */

package com.microsoft.azure.management.recoveryservices.backup.v2016_06_01.implementation;

import com.microsoft.azure.arm.model.implementation.WrapperImpl;
import com.microsoft.azure.management.recoveryservices.backup.v2016_06_01.BackupEngines;
import rx.Observable;
import rx.functions.Func1;
import com.microsoft.azure.Page;
import com.microsoft.azure.management.recoveryservices.backup.v2016_06_01.BackupEngineBaseResource;

class BackupEnginesImpl extends WrapperImpl<BackupEnginesInner> implements BackupEngines {
    private final RecoveryServicesManager manager;

    BackupEnginesImpl(RecoveryServicesManager manager) {
        super(manager.inner().backupEngines());
        this.manager = manager;
    }

    public RecoveryServicesManager manager() {
        return this.manager;
    }

    private BackupEngineBaseResourceImpl wrapModel(BackupEngineBaseResourceInner inner) {
        return  new BackupEngineBaseResourceImpl(inner, manager());
    }

    @Override
    public Observable<BackupEngineBaseResource> getAsync(final String vaultName, final String resourceGroupName) {
        BackupEnginesInner client = this.inner();
        return client.getAsync(vaultName, resourceGroupName)
        .flatMapIterable(new Func1<Page<BackupEngineBaseResourceInner>, Iterable<BackupEngineBaseResourceInner>>() {
            @Override
            public Iterable<BackupEngineBaseResourceInner> call(Page<BackupEngineBaseResourceInner> page) {
                return page.items();
            }
        })
        .map(new Func1<BackupEngineBaseResourceInner, BackupEngineBaseResource>() {
            @Override
            public BackupEngineBaseResource call(BackupEngineBaseResourceInner inner) {
                return wrapModel(inner);
            }
        });
    }

}
