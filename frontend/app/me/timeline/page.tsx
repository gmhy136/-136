"use client";

import { useQuery } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { getTimeline } from "@/lib/api";
import type { TimelineEvent } from "@/lib/types";

/** "2026-10-05" → "10月 5 日"；进行中的理想显示为 "10月 · 现在"。 */
function fmtDate(e: TimelineEvent): string {
  const [, m, d] = e.date.split("-").map(Number);
  if (e.type === "IDEAL_ACTIVE") return `${m}月 · 现在`;
  return `${m}月 ${d}日`;
}

export default function TimelinePage() {
  const { data, isPending, isError } = useQuery({
    queryKey: ["timeline"],
    queryFn: getTimeline,
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

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">我的成长故事</div>
          <div className="s">记录卡和理想卡，串成一条慢慢向上的线。</div>
        </div>

        {data.length === 0 ? (
          <div className="empty">
            <div className="t">还没有故事</div>
            <div className="s">写下第一张卡，开始你的时间线。</div>
          </div>
        ) : (
          <div className="tl">
            {data.map((e, i) => (
              <div key={i} className="tl-item">
                <div className="dot" />
                <div className="date">{fmtDate(e)}</div>
                <div className="card">
                  <b>{e.label}</b> · {e.detail}
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="note">回头看，来路清晰可见。</div>
      </div>
    </div>
  );
}
