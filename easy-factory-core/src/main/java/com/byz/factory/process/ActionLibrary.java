package com.byz.factory.process;

import com.byz.factory.shared.Dict.Execute;
import com.byz.factory.shared.Dict.Importance;

import java.util.*;

/**
 * 动作库 — 共享的动作定义注册表。
 * <p>
 * 各模块从此选取动作，不重复定义。动作库中的动作是"定义"（code/name/executeType/Importance），
 * 不绑定具体设备或参数——那些是 EngineeringBOM 的标注。
 *
 * @author 苏政
 */
public class ActionLibrary {

    private final Map<String, Action> actions = new LinkedHashMap<>();

    /** 注册动作 */
    public ActionLibrary register(String code, String name, Execute executeType, Importance importance) {
        Action a = new Action(code, name, importance, System.currentTimeMillis());
        a.setExecuteType(executeType);
        actions.put(code, a);
        return this;
    }

    /** 注册动作（默认 Importance.Require） */
    public ActionLibrary register(String code, String name, Execute executeType) {
        return register(code, name, executeType, Importance.Require);
    }

    /** 获取动作定义 */
    public Optional<Action> get(String code) {
        return Optional.ofNullable(actions.get(code));
    }

    /** 获取全部动作 */
    public Collection<Action> getAll() {
        return Collections.unmodifiableCollection(actions.values());
    }

    /** 按前缀查找 */
    public List<Action> findByPrefix(String prefix) {
        return actions.values().stream()
            .filter(a -> a.getCode().startsWith(prefix))
            .toList();
    }

    public int size() { return actions.size(); }
    public boolean contains(String code) { return actions.containsKey(code); }
}
