/** 日期 → YYYY-MM-DD。 */
function toDateStr(d: Date): string {
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${d.getFullYear()}-${m}-${day}`;
}

/** 开启日期最早为明天（用于日期选择器 min）。 */
export function minOpenDate(): string {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return toDateStr(d);
}

export interface Countdown {
  days: number;
  hours: number;
  minutes: number;
}

/** 距离开启日 00:00 的倒计时（now 由调用方传入，避免在渲染期读当前时间）。 */
export function countdown(openDate: string, now: number): Countdown {
  const target = new Date(`${openDate}T00:00:00`).getTime();
  const diff = Math.max(0, target - now);
  return {
    days: Math.floor(diff / 86_400_000),
    hours: Math.floor((diff % 86_400_000) / 3_600_000),
    minutes: Math.floor((diff % 3_600_000) / 60_000),
  };
}

/** "2026-10-01" → "2026 年 10 月 1 日"。 */
export function formatDateCN(iso: string): string {
  const dt = new Date(`${iso}T00:00:00`);
  return `${dt.getFullYear()} 年 ${dt.getMonth() + 1} 月 ${dt.getDate()} 日`;
}
