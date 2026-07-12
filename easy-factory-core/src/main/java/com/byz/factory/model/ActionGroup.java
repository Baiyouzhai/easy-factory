package com.byz.factory.model;

import com.byz.factory.data.Dict;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 动作组 — 将多子动作组合为一个复合动作。
 * <p>
 * 子动作按重要性（Optional/Require）分级，存于有序映射中。
 * 继承 {@link Action} 获得完整动作能力（code/name/importance/order/script/requireResources）。
 *
 * @author 苏政
 */
public class ActionGroup extends Action {

    /** 子动作映射：动作 → 重要性 */
    protected LinkedHashMap<Action, Dict.Importance> actions;

    public ActionGroup(String code, String name, Dict.Importance importance, long order,
                       LinkedHashMap<Action, Dict.Importance> actions) {
        super(code, name, importance, order);
        this.actions = actions;
    }

    public ActionGroup(String code, String name, LinkedHashMap<Action, Dict.Importance> actions) {
        this(code, name, Dict.Importance.Optional, System.currentTimeMillis(), actions);
    }

    public ActionGroup(LinkedHashMap<Action, Dict.Importance> actions) {
        this("ActionGroup", "动作组", Dict.Importance.Optional, System.currentTimeMillis(), actions);
    }

    // ---- 子动作管理 ----

    public Map<Action, Dict.Importance> getActions() {
        if (null == actions) actions = new LinkedHashMap<>();
        return actions;
    }

    public void addAction(Action action, Dict.Importance importance) {
        if (null == actions) actions = new LinkedHashMap<>();
        actions.put(action, importance);
    }

    /**
     * 执行所有必要子动作，累积输出
     */
    @Override
    public IResourcePack execute(IProcess process, IResourceModel... resources) {
        if (null == actions || actions.isEmpty()) {
            return process.getResourcePack();
        }
        IResourcePack output = process.getResourcePack();
        for (Map.Entry<Action, Dict.Importance> entry : actions.entrySet()) {
            if (entry.getValue() == Dict.Importance.Optional) {
                // 可选动作：跳过失败
                try {
                    output = entry.getKey().execute(process, resources);
                } catch (Exception e) {
                    // 忽略可选动作的异常
                }
            } else {
                output = entry.getKey().execute(process, resources);
            }
        }
        return output;
    }

}
