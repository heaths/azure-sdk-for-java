/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.apimanagement.v2019_01_01.implementation;

import com.microsoft.azure.management.apimanagement.v2019_01_01.UserGroupContract;
import com.microsoft.azure.arm.model.implementation.WrapperImpl;
import rx.Observable;
import com.microsoft.azure.management.apimanagement.v2019_01_01.GroupType;

class UserGroupContractImpl extends WrapperImpl<GroupContractInner> implements UserGroupContract {
    private final ApiManagementManager manager;

    UserGroupContractImpl(GroupContractInner inner,  ApiManagementManager manager) {
        super(inner);
        this.manager = manager;
    }

    @Override
    public ApiManagementManager manager() {
        return this.manager;
    }



    @Override
    public Boolean builtIn() {
        return this.inner().builtIn();
    }

    @Override
    public String description() {
        return this.inner().description();
    }

    @Override
    public String displayName() {
        return this.inner().displayName();
    }

    @Override
    public String externalId() {
        return this.inner().externalId();
    }

    @Override
    public GroupType groupContractType() {
        return this.inner().groupContractType();
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String type() {
        return this.inner().type();
    }

}
