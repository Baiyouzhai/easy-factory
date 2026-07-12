package com.byz.factory.data;

import com.byz.factory.model.IResourceModel;
import com.byz.factory.model.IResourcePack;
import com.byz.factory.model.ResourcePack;

public final class EmptyResourcePack implements IResourcePack {

    public static final EmptyResourcePack Instance = new EmptyResourcePack();

    private final IResourceModel[] resources = ResourcePack.Empty;

    @Override
    public IResourceModel[] getResources() {
        return resources;
    }

    @Override
    public EmptyResourcePack copy() {
        return Instance;
    }

}
