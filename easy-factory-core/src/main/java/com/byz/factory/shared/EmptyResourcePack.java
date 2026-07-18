package com.byz.factory.shared;

import com.byz.factory.resource.IResourceItem;
import com.byz.factory.resource.IResourcePack;
import com.byz.factory.resource.ResourceItem;

public final class EmptyResourcePack implements IResourcePack {

    public static final EmptyResourcePack Instance = new EmptyResourcePack();

    private final IResourceItem[] resources = ResourceItem.EMPTY_ARRAY;

    @Override
    public IResourceItem[] getResources() {
        return resources;
    }

    @Override
    public EmptyResourcePack copy() {
        return Instance;
    }

}
