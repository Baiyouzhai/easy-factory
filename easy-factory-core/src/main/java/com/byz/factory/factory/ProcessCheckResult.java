package com.byz.factory.factory;

/**
 * 单个工序的可制造性检查结果。
 *
 * @param processCode 工序编码
 * @param passed      是否通过
 * @param reason      原因说明
 * @author 苏政
 */
public record ProcessCheckResult(String processCode, boolean passed, String reason) {

    public static ProcessCheckResult pass(String processCode) {
        return new ProcessCheckResult(processCode, true, "OK");
    }

    public static ProcessCheckResult fail(String processCode, String reason) {
        return new ProcessCheckResult(processCode, false, reason);
    }

}
