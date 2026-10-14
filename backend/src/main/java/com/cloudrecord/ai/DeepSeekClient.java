package com.cloudrecord.ai;

import reactor.core.publisher.Flux;

/**
 * DeepSeek 调用薄封装，隔离 Spring AI，便于单测 mock 与后续换模型。
 */
public interface DeepSeekClient {

    /** 流式对话，返回逐段增量文本。 */
    Flux<String> stream(String system, String user, AiParams params);

    /** 非流式对话，一次性返回完整文本。 */
    String call(String system, String user, AiParams params);
}
