package com.byz.factory.core.resource;

import com.byz.factory.data.EmptyResourcePack;
import com.byz.factory.exception.ResourceException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源包(BOM)
 *
 * @author 苏政
 */
public class ResourcePack implements IResourcePack {

    public static final ResourceModel[] Empty = ResourceModel.Empty;
    public static final IResourcePack EmptyPack = EmptyResourcePack.Instance;

    protected List<IResourceModel> resources;

    public ResourcePack(Collection<IResourceModel> resources) {
        setResources(resources);
    }

    public ResourcePack() {
        this.resources = new ArrayList<>();
    }

    @Override
    public IResourceModel[] getResources() {
        if (null == resources) resources = new ArrayList<>();
        return resources.toArray(IResourceModel[]::new);
    }

    /**
     * 设置资源清单
     *
     * @param resources 资源清单(为List子类时, 内存共享)
     * @return this
     */
    @Override
    public ResourcePack setResources(Collection<IResourceModel> resources) {
        if (resources instanceof List) {
            this.resources = (List<IResourceModel>) resources;
            return this;
        }
        this.resources = null == resources ? new ArrayList<>() : resources.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return this;
    }

    @Override
    public ResourcePack setResources(IResourceModel[] resources) {
        this.resources = null == resources ? new ArrayList<>() : Arrays.stream(resources).filter(Objects::nonNull).collect(Collectors.toList());
        return this;
    }

    @Override
    public boolean isEmpty(boolean resourceCheck) {
        if (null == resources) return true;
        if (resources.isEmpty()) return true;
        return resourceCheck && resources.stream().allMatch(IResourceModel::isEmpty);
    }

    @Override
    public ResourcePack merge(Collection<IResourceModel> resources) {
        if (null == resources) return this;
        if (resources.isEmpty()) return this;
        if (null == this.resources) return setResources(resources);
        for (IResourceModel resource : resources) {
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
        List<IResourceModel> newList = new ArrayList<>();
        Map<String, IResourceModel> groupList = new HashMap<>();
        for (IResourceModel resource : this.resources) {
            String key = resource.getGroup().name() + '-' + resource.getName();
            if (groupList.containsKey(key)) {
                groupList.get(key).put(resource.getNumber());
            } else {
                IResourceModel copy = resource.copy();
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
        IResourceModel[] array = getResources();
        List<IResourceModel> resources = null == array ? new ArrayList<>() : Arrays.stream(array).filter(Objects::nonNull).map(IResourceModel::copy).toList();
        resourcePack.setResources(resources);
        return resourcePack;
    }

}
