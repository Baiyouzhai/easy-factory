package com.byz.factory.resource;

import com.byz.factory.exception.ResourceException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源包
 *
 * @author 苏政
 */
public interface IResourcePack {

    /**
     * 资源清单
     *
     * @return 资源清单
     */
    default IResourceItem[] getResources() {
        return IResourceItem.EMPTY_ARRAY;
    }

    /**
     * 设置资源清单
     *
     * @param resources 资源清单
     * @return this
     */
    default IResourcePack setResources(Collection<IResourceItem> resources) {
        return this;
    }

    /**
     * 设置资源清单
     *
     * @param resources 资源清单
     * @return this
     */
    default IResourcePack setResources(IResourceItem[] resources) {
        List<IResourceItem> list = null == resources ? new ArrayList<>() : Arrays.stream(resources).filter(Objects::nonNull).collect(Collectors.toList());
        return setResources(list);
    }

    /**
     * 是否空
     *
     * @param resourceCheck 是否检查资源
     * @return 是否空
     */
    default boolean isEmpty(boolean resourceCheck) {
        IResourceItem[] resources = getResources();
        if (null == resources) return true;
        if (0 == resources.length) return true;
        return resourceCheck && Arrays.stream(resources).filter(Objects::nonNull).allMatch(IResourceItem::isEmpty);
    }

    /**
     * 是否空, {@link #isEmpty(boolean)} - isEmpty(false)
     *
     * @return 是否空
     */
    default boolean isEmpty() {
        return isEmpty(false);
    }

    /**
     * 获取必要的资源
     *
     * @return 必要的资源
     */
    default IResourceItem[] requireResources() {
        IResourceItem[] resources = getResources();
        if (null == resources) return IResourceItem.EMPTY_ARRAY;
        return Arrays.stream(resources).filter(Objects::nonNull).filter(IResourceItem::require).toArray(IResourceItem[]::new);
    }

    /**
     * 合并资源
     *
     * @param resources 资源
     * @return this
     * @throws ResourceException 资源已存在, 重复添加资源
     */
    default IResourcePack merge(Collection<IResourceItem> resources) {
        if (null == resources) return this;
        if (resources.isEmpty()) return this;
        IResourceItem[] array = getResources();
        if (null == array) return setResources(resources);
        List<IResourceItem> list = Arrays.stream(array).filter(Objects::nonNull).collect(Collectors.toList());
        for (IResourceItem resource : resources) {
            if (null == resource) continue;
            if (list.contains(resource)) {
                throw new ResourceException("资源已存在, 重复添加资源: " + resource.toJsonString());
            }
        }
        list.addAll(resources);
        setResources(list);
        return this;
    }

    /**
     * 合并资源
     *
     * @param resources 资源
     * @return this
     * @throws ResourceException 资源已存在, 重复添加资源
     */
    default IResourcePack merge(IResourceItem... resources) {
        if (null == resources) return this;
        if (0 == resources.length) return this;
        List<IResourceItem> list = Arrays.asList(resources);
        return merge(list);
    }

    /**
     * 合并资源
     *
     * @param resourcePack 资源包
     * @return this
     * @throws ResourceException 资源已存在, 重复添加资源
     */
    default IResourcePack merge(IResourcePack resourcePack) {
        if (null == resourcePack) return this;
        IResourceItem[] resources = resourcePack.getResources();
        return merge(resources);
    }

    /**
     * 压缩资源
     *
     * @return this
     */
    default IResourcePack compress() {
        IResourceItem[] array = getResources();
        if (null == array) return this;
        if (0 == array.length) return this;
        List<IResourceItem> newList = new ArrayList<>();
        Map<String, IResourceItem> groupList = new LinkedHashMap<>();
        for (IResourceItem resource : array) {
            if (null == resource) continue;
            String key = resource.getGroup().name() + '-' + resource.getName();
            if (groupList.containsKey(key)) {
                groupList.get(key).put(resource.getNumber());
            } else {
                IResourceItem copy = resource.copy();
                newList.add(copy);
                groupList.put(key, copy);
            }
        }
        if (array.length == newList.size()) return this;
        return setResources(newList);
    }

    /**
     * 压缩资源
     *
     * @return this
     */
    default IResourcePack compress(Collection<IResourceItem> resources) {
        return merge(resources).compress();
    }

    /**
     * 压缩资源
     *
     * @param resources 资源
     * @return this
     */
    default IResourcePack compress(IResourceItem... resources) {
        return merge(resources).compress();
    }

    /**
     * 压缩资源
     *
     * @param resourcePack 资源
     * @return this
     */
    default IResourcePack compress(IResourcePack resourcePack) {
        if (null == resourcePack) return compress();
        IResourceItem[] resources = resourcePack.getResources();
        return compress(resources);
    }

    /**
     * 复制资源包
     *
     * @return 资源包
     */
    default IResourcePack copy() {
        return new ResourcePack();
    }

}
