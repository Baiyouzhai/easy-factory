package com.byz.factory.script;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScriptMetadata 脚本元数据解析测试")
class ScriptMetadataTest {

    private static final String VALID_SCRIPT = """
        /**
         * @id          test.pass.v1
         * @name        通过测试
         * @version     1.0.0
         * @module      test
         * @description 验证通过的测试脚本
         * @author      测试员
         * @since       2026-07-18
         */
        function execute(context) { return {}; }
        """;

    @Test
    @DisplayName("parse — 完整元数据解析")
    void parse_validScript() {
        ScriptMetadata meta = ScriptMetadata.parse(VALID_SCRIPT);
        assertEquals("test.pass.v1", meta.getId());
        assertEquals("通过测试", meta.getName());
        assertEquals("1.0.0", meta.getVersion());
        assertEquals("test", meta.getModule());
        assertEquals("验证通过的测试脚本", meta.getDescription());
        assertEquals("测试员", meta.getAuthor());
        assertEquals("2026-07-18", meta.getSince());
        assertEquals("DRAFT", meta.getStatus());
        assertEquals(30_000, meta.getTimeoutMs());
    }

    @Test
    @DisplayName("parse — 缺少必填 @id 抛异常")
    void parse_missingId_throws() {
        String src = """
            /**
             * @name Test
             * @version 1.0.0
             * @module test
             */
            function execute(ctx) {}
            """;
        assertThrows(IllegalArgumentException.class, () -> ScriptMetadata.parse(src));
    }

    @Test
    @DisplayName("parse — 缺少必填 @name 抛异常")
    void parse_missingName_throws() {
        String src = """
            /**
             * @id test.v1
             * @version 1.0.0
             * @module test
             */
            function execute(ctx) {}
            """;
        assertThrows(IllegalArgumentException.class, () -> ScriptMetadata.parse(src));
    }

    @Test
    @DisplayName("parse — 缺少必填 @version 抛异常")
    void parse_missingVersion_throws() {
        String src = """
            /**
             * @id test.v1
             * @name Test
             * @module test
             */
            function execute(ctx) {}
            """;
        assertThrows(IllegalArgumentException.class, () -> ScriptMetadata.parse(src));
    }

    @Test
    @DisplayName("parse — 缺少必填 @module 抛异常")
    void parse_missingModule_throws() {
        String src = """
            /**
             * @id test.v1
             * @name Test
             * @version 1.0.0
             */
            function execute(ctx) {}
            """;
        assertThrows(IllegalArgumentException.class, () -> ScriptMetadata.parse(src));
    }

    @Test
    @DisplayName("setStatus — 状态更新")
    void setStatus_updatesCorrectly() {
        ScriptMetadata meta = ScriptMetadata.parse(VALID_SCRIPT);
        meta.setStatus("ACTIVE");
        assertEquals("ACTIVE", meta.getStatus());
    }

    @Test
    @DisplayName("setTimeoutMs — 超时时间更新")
    void setTimeoutMs_updatesCorrectly() {
        ScriptMetadata meta = ScriptMetadata.parse(VALID_SCRIPT);
        meta.setTimeoutMs(10_000);
        assertEquals(10_000, meta.getTimeoutMs());
    }

    @Test
    @DisplayName("toString — 包含关键字段")
    void toString_containsKeyFields() {
        ScriptMetadata meta = ScriptMetadata.parse(VALID_SCRIPT);
        String str = meta.toString();
        assertTrue(str.contains("test.pass.v1"));
        assertTrue(str.contains("1.0.0"));
        assertTrue(str.contains("test"));
    }

}
