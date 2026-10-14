/** 理想卡进度（百分比，0~100）。 */
export function progressPct(completed: number, total: number): number {
  if (total <= 0) return 0;
  return Math.round((completed / total) * 100);
}

export function visibilityLabel(v: string | null | undefined): string {
  return v === "PUBLIC" ? "公开" : "私密";
}

export interface CalCell {
  day: number;
  off: boolean;
  done: boolean;
  today: boolean;
}

/** 生成周一起始的日历网格（含前后月补齐）。 */
export function buildCalendarGrid(month: string, checkedDays: number[]): CalCell[] {
  const [y, m] = month.split("-").map(Number);
  const daysInMonth = new Date(y, m, 0).getDate();
  const prevDays = new Date(y, m - 1, 0).getDate();
  // 周一=0 的偏移量
  const leadOffset = (new Date(y, m - 1, 1).getDay() + 6) % 7;

  const now = new Date();
  const isCurrentMonth = now.getFullYear() === y && now.getMonth() + 1 === m;
  const checked = new Set(checkedDays);

  const cells: CalCell[] = [];
  for (let i = leadOffset; i > 0; i--) {
    cells.push({ day: prevDays - i + 1, off: true, done: false, today: false });
  }
  for (let d = 1; d <= daysInMonth; d++) {
    cells.push({
      day: d,
      off: false,
      done: checked.has(d),
      today: isCurrentMonth && now.getDate() === d,
    });
  }
  const trailing = (7 - (cells.length % 7)) % 7;
  for (let d = 1; d <= trailing; d++) {
    cells.push({ day: d, off: true, done: false, today: false });
  }
  return cells;
}

/** 月份偏移：delta=±1 切月。 */
export function shiftMonth(month: string, delta: number): string {
  const [y, m] = month.split("-").map(Number);
  const d = new Date(y, m - 1 + delta, 1);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`;
}

export function monthLabel(month: string): string {
  const [y, m] = month.split("-").map(Number);
  return `${y} 年 ${m} 月`;
}
