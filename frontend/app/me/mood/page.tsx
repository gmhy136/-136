"use client";

import { useQuery } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { getMoodReport } from "@/lib/api";

const WEEKDAYS = ["一", "二", "三", "四", "五", "六", "日"];

/** 由 "2026-10" 生成月历格子：前导空位为 null，其余为 1..当月天数。 */
function buildGrid(month: string): (number | null)[] {
  const [y, m] = month.split("-").map(Number);
  const leading = (new Date(y, m - 1, 1).getDay() + 6) % 7; // 周一开头
  const total = new Date(y, m, 0).getDate();
  const cells: (number | null)[] = [];
  for (let i = 0; i < leading; i++) cells.push(null);
  for (let d = 1; d <= total; d++) cells.push(d);
  return cells;
}

export default function MoodReportPage() {
  const { data, isPending, isError } = useQuery({
    queryKey: ["mood-report"],
    queryFn: () => getMoodReport(),
  });

  if (isPending) {
    return (
      <div className="app">
        <TopNav />
        <div className="content">
          <div className="empty">
            <div className="t">加载中…</div>
          </div>
        </div>
      </div>
    );
  }

  if (isError || !data) {
    return (
      <div className="app">
        <TopNav />
        <div className="content">
          <div className="empty">
            <div className="t">加载失败</div>
            <div className="s">请确认后端已启动</div>
          </div>
        </div>
      </div>
    );
  }

  const [y, m] = data.month.split("-").map(Number);
  const cells = buildGrid(data.month);

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">{m} 月的心情天气</div>
          <div className="s">把每一天的心情，画成一张天气图。</div>
        </div>

        <div className="cal" style={{ padding: 14 }}>
          <div className="cal-head">
            <span>{m} 月 · {y}</span>
            <span className="tag ai">月报</span>
          </div>
          <div className="wx-tiles">
            {WEEKDAYS.map((w) => (
              <span key={w} className="wx wd">{w}</span>
            ))}
            {cells.map((d, i) =>
              d == null ? (
                <span key={`b${i}`} className="wx blank" />
              ) : (
                <span key={d} className="wx">
                  {data.days[d] && <i className={`mdot ${data.days[d]}`} />}
                </span>
              )
            )}
          </div>
        </div>

        <div className="quote">{data.quote}</div>

        <div className="tiles" style={{ gridTemplateColumns: "repeat(3,1fr)" }}>
          <div className="tile"><div className="v">{data.sunnyCount}</div><div className="l">晴天</div></div>
          <div className="tile"><div className="v">{data.cloudyCount}</div><div className="l">多云</div></div>
          <div className="tile"><div className="v">{data.rainyCount}</div><div className="l">雨天</div></div>
        </div>
      </div>
    </div>
  );
}
