export interface StyleMeta {
  /** 卡片样式类（st-warm 等）。 */
  cls: string;
  /** 中文名。 */
  name: string;
  /** 风格网格色条类（bar warm 等）。 */
  bar: string;
  /** 推荐面板色块用 CSS 变量。 */
  swatch: string;
}

/** 卡片风格映射：styleKey（长 key，见方案设计文档 §14.3）→ 类名/中文名/色条。 */
export const STYLES: Record<string, StyleMeta> = {
  "warm-sunny": { cls: "st-warm", name: "暖阳橙", bar: "warm", swatch: "var(--warm)" },
  "quiet-blue": { cls: "st-quiet", name: "静谧蓝", bar: "quiet", swatch: "var(--quiet)" },
  "mint-fresh": { cls: "st-mint", name: "薄荷绿", bar: "mint", swatch: "var(--mint)" },
  "sakura-pink": { cls: "st-sakura", name: "樱花粉", bar: "sakura", swatch: "var(--sakura)" },
  "night-purple": { cls: "st-night", name: "夜空紫", bar: "night", swatch: "var(--night)" },
  "paper-white": { cls: "st-paper", name: "素白", bar: "paper", swatch: "var(--paper-style)" },
};

/** 有序风格列表（渲染 6 风格网格用，顺序与原型一致）。 */
export const STYLE_LIST = Object.entries(STYLES).map(([key, meta]) => ({ key, ...meta }));

export const DEFAULT_STYLE_KEY = "paper-white";

export const DEFAULT_STYLE = STYLES[DEFAULT_STYLE_KEY];

export function styleOf(key: string | null | undefined): StyleMeta {
  return STYLES[key ?? ""] ?? DEFAULT_STYLE;
}

/** 时间显示：今天 HH:mm / 昨天 / M月d日。 */
export function formatTime(iso: string): string {
  const d = new Date(iso);
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const day = new Date(d.getFullYear(), d.getMonth(), d.getDate());
  const diffDays = Math.round((today.getTime() - day.getTime()) / 86400000);
  if (diffDays === 0) {
    const hh = String(d.getHours()).padStart(2, "0");
    const mm = String(d.getMinutes()).padStart(2, "0");
    return `今天 ${hh}:${mm}`;
  }
  if (diffDays === 1) return "昨天";
  return `${d.getMonth() + 1}月${d.getDate()}日`;
}

export function isAiType(aiType: string | null | undefined): boolean {
  return aiType === "AI润色" || aiType === "AI扩写";
}

export function aiTypeLabel(aiType: string | null | undefined): string {
  if (aiType === "AI润色") return "AI 润色";
  if (aiType === "AI扩写") return "AI 扩写";
  return aiType || "原创";
}
