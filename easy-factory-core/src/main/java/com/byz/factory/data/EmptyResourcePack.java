package com.byz.factory.data;

import com.byz.factory.core.IResource;
import com.byz.factory.core.IResourcePack;
import com.byz.factory.core.ResourcePack;

public final class EmptyResourcePack implements IResourcePack {

    public static final EmptyResourcePack Instance = new EmptyResourcePack();

    private final IResource[] resources = ResourcePack.Empty;

    @Override
    public IResource[] getResources() {
        return resources;
    }

    @Override
    public EmptyResourcePack copy() {
        return Instance;
    }

}
