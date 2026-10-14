package com.cloudrecord.ai;

/**
 * 单次 AI 调用的采样参数（与方案设计文档 §7.1 建议参数一致）。
 */
public record AiParams(double temperature, int maxTokens) {

    /** 扩写/润色：温度 0.7、max_tokens 1024。 */
    public static AiParams expand() {
        return new AiParams(0.7, 1024);
    }

    /** 风格推荐：温度 0.2、max_tokens 512（更确定，利于 JSON 解析）。 */
    public static AiParams style() {
        return new AiParams(0.2, 512);
    }

    /** 理想拆解：温度 0.2、max_tokens 512。 */
    public static AiParams split() {
        return new AiParams(0.2, 512);
    }

    /** 今日一句：温度 0.9、max_tokens 128（更有灵气，短句足够）。 */
    public static AiParams quote() {
        return new AiParams(0.9, 128);
    }
}
