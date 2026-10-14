package com.cloudrecord.ai;

import java.util.List;

/**
 * AI 理想拆解结果（失败降级为空任务列表）。
 */
public record SplitIdealResult(List<String> tasks, int remaining) {
}
