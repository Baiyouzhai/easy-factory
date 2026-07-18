package com.byz.factory.resource;

import com.byz.factory.exception.ResourceException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源包(BOM)实现
 *
 * @author 苏政
 */
public class ResourcePack implements IResourcePack {

    public static final IResourceItem[] Empty = ResourceItem.EMPTY_ARRAY;
    public static final IResourcePack EmptyPack = com.byz.factory.shared.EmptyResourcePack.Instance;

    protected List<IResourceItem> resources;

    public ResourcePack(Collection<IResourceItem> resources) {
        setResources(resources);
    }

    public ResourcePack() {
        this.resources = new ArrayList<>();
    }

    @Override
    public IResourceItem[] getResources() {
        if (null == resources) resources = new ArrayList<>();
        return resources.toArray(IResourceItem[]::new);
    }

    /**
     * 设置资源清单
     *
     * @param resources 资源清单(为List子类时, 内存共享)
     * @return this
     */
    @Override
    @SuppressWarnings("unchecked")
    public ResourcePack setResources(Collection<IResourceItem> resources) {
        if (resources instanceof List) {
            this.resources = (List<IResourceItem>) resources;
            return this;
        }
        this.resources = null == resources ? new ArrayList<>() : resources.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return this;
    }

    @Override
    public ResourcePack setResources(IResourceItem[] resources) {
        this.resources = null == resources ? new ArrayList<>() : Arrays.stream(resources).filter(Objects::nonNull).collect(Collectors.toList());
        return this;
    }

    @Override
    public boolean isEmpty(boolean resourceCheck) {
        if (null == resources) return true;
        if (resources.isEmpty()) return true;
        return resourceCheck && resources.stream().allMatch(IResourceItem::isEmpty);
    }

    @Override
    public ResourcePack merge(Collection<IResourceItem> resources) {
        if (null == resources) return this;
        if (resources.isEmpty()) return this;
        if (null == this.resources) return setResources(resources);
        for (IResourceItem resource : resources) {
            if (null == resource) continue;
            if (this.resources.contains(resource)) {
                throw new ResourceException("资源已存在, 重复添加资源: " + resource.toJsonString());
            }
        }
        this.resources.addAll(resources);
        return this;
    }

    @Override
    public ResourcePack compress() {
        if (null == this.resources) return this;
        if (this.resources.isEmpty()) return this;
        List<IResourceItem> newList = new ArrayList<>();
        Map<String, IResourceItem> groupList = new LinkedHashMap<>();
        for (IResourceItem resource : this.resources) {
            String key = resource.getGroup().name() + '-' + resource.getName();
            if (groupList.containsKey(key)) {
                groupList.get(key).put(resource.getNumber());
            } else {
                IResourceItem copy = resource.copy();
                newList.add(copy);
                groupList.put(key, copy);
            }
        }
        if (this.resources.size() != newList.size()) this.resources = newList;
        return this;
    }

    @Override
    public ResourcePack copy() {
        ResourcePack resourcePack = new ResourcePack();
        IResourceItem[] array = getResources();
        List<IResourceItem> resources = null == array ? new ArrayList<>() : Arrays.stream(array).filter(Objects::nonNull).map(IResourceItem::copy).toList();
        resourcePack.setResources(resources);
        return resourcePack;
    }

}
