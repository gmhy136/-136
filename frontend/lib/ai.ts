import { fetchEventSource } from "@microsoft/fetch-event-source";
import { api, ApiError, refreshTokens } from "./api";
import { getAccessToken } from "./auth";
import type { StyleRecommendation } from "./types";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080/api/v1";

export interface ExpandHandlers {
  onStart?: (remaining: number) => void;
  onDelta?: (text: string) => void;
  onDone?: () => void;
  onError?: (message: string) => void;
}

interface SseMessage {
  type: string;
  content?: string;
  remaining?: number;
  message?: string;
}

/**
 * AI 扩写（SSE 流式，POST + Bearer）。
 * 服务端协议：start(delta 前一次) / delta* / done / error。
 * 超限、未登录、网络错误抛 ApiError；生成中断走 onError 回调。
 */
export async function streamExpand(
  content: string,
  moodId: number | null,
  handlers: ExpandHandlers,
  signal: AbortSignal
): Promise<void> {
  const run = (token: string | null) =>
    fetchEventSource(`${API_BASE}/ai/expand`, {
      method: "POST",
      headers: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ content, moodId }),
      signal,
      async onopen(response) {
        if (!response.ok) {
          const body = (await response.json().catch(() => null)) as {
            code?: number;
            message?: string;
          } | null;
          throw new ApiError(body?.code ?? response.status, body?.message ?? "请求失败");
        }
      },
      onmessage(ev) {
        if (!ev.data) return;
        const msg = JSON.parse(ev.data) as SseMessage;
        if (msg.type === "start") handlers.onStart?.(msg.remaining ?? 0);
        else if (msg.type === "delta") handlers.onDelta?.(msg.content ?? "");
        else if (msg.type === "done") handlers.onDone?.();
        else if (msg.type === "error") handlers.onError?.(msg.message ?? "生成中断，可重试");
      },
      onerror(err) {
        throw err; // 不做自动重连，手动「再写一次」重试
      },
    });

  try {
    await run(getAccessToken());
  } catch (e) {
    if (e instanceof ApiError && e.code === 401 && (await refreshTokens())) {
      await run(getAccessToken());
      return;
    }
    throw e;
  }
}

/** AI 风格推荐（非流式 JSON）。 */
export function recommendStyle(
  content: string,
  moodId: number | null
): Promise<StyleRecommendation> {
  return api<StyleRecommendation>("/ai/style", {
    method: "POST",
    body: JSON.stringify({ content, moodId }),
  });
}
