package com.cloudrecord.ai;

import reactor.core.publisher.Flux;

/**
 * AI 扩写结果：流式增量文本 + 今日剩余次数。
 */
public record ExpandResult(Flux<String> deltas, int remaining) {
}
