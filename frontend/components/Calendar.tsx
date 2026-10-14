"use client";

import { buildCalendarGrid, monthLabel } from "@/lib/ideal";

interface Props {
  month: string;
  days: number[];
  streakDays: number;
  monthCount: number;
  onPrev: () => void;
  onNext: () => void;
}

const WEEKDAYS = ["一", "二", "三", "四", "五", "六", "日"];

export default function Calendar({ month, days, streakDays, monthCount, onPrev, onNext }: Props) {
  const cells = buildCalendarGrid(month, days);

  return (
    <div>
      <div className="cal">
        <div className="cal-head">
          <span className="nav" onClick={onPrev}>‹</span>
          <span>{monthLabel(month)}</span>
          <span className="nav" onClick={onNext}>›</span>
        </div>
        <div className="g7">
          {WEEKDAYS.map((w) => (
            <span key={w} className="wd">{w}</span>
          ))}
          {cells.map((c, i) => {
            const cls = c.off ? "d off" : c.done ? "d done" : c.today ? "d today" : "d";
            return <span key={i} className={cls}>{c.day}</span>;
          })}
        </div>
        <div className="legend-cal">
          <span><i />已打卡</span>
          <span>今天 · 记得打卡</span>
        </div>
      </div>

      <div className="tiles" style={{ gridTemplateColumns: "repeat(2,1fr)" }}>
        <div className="tile"><div className="v">{streakDays}</div><div className="l">连续打卡（天）</div></div>
        <div className="tile"><div className="v">{monthCount}</div><div className="l">本月打卡（天）</div></div>
      </div>

      <div className="note">
        {streakDays > 0 ? `已连续打卡 ${streakDays} 天。` : "从今天开始，迈出第一步。"}
      </div>
    </div>
  );
}
