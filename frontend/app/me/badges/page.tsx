"use client";

import { useQuery } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { getBadges, getOverview } from "@/lib/api";
import type { Badge, Overview } from "@/lib/types";

/** 下一个待点亮徽章的引导文案。 */
function nextBadgeHint(b: Badge, ov: Overview): string {
  switch (b.key) {
    case "first-record":
      return "写下第一张卡片，点亮「初次记录」。";
    case "ten-cards":
      return `再写 ${Math.max(0, 10 - ov.recordCount)} 张卡片，点亮「十张卡片」。`;
    case "streak-7":
      return `再坚持 ${Math.max(0, 7 - ov.streakDays)} 天，点亮「连续 7 天」。`;
    case "ideal-done":
      return "完成第一个理想，点亮「理想达成」。";
    case "time-traveler":
      return "开启一封胶囊，点亮「时空旅人」。";
    case "full-moon":
      return `再记录 ${Math.max(0, 30 - ov.recordDays)} 天，点亮「满月记录」。`;
    default:
      return "继续记录，点亮更多徽章。";
  }
}

export default function BadgesPage() {
  const badgesQuery = useQuery({ queryKey: ["badges"], queryFn: getBadges });
  const overviewQuery = useQuery({ queryKey: ["overview"], queryFn: getOverview });

  if (badgesQuery.isPending || overviewQuery.isPending) {
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

  if (badgesQuery.isError || !badgesQuery.data || overviewQuery.isError || !overviewQuery.data) {
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

  const badges = badgesQuery.data;
  const lit = badges.filter((b) => b.unlocked).length;
  const locked = badges.find((b) => !b.unlocked);
  const hint = locked ? nextBadgeHint(locked, overviewQuery.data) : "全部点亮，你真的很棒。";

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">我的徽章</div>
          <div className="s">每一枚徽章，都是一次小小的坚持。</div>
        </div>

        <div className="sect-t">已点亮 · {lit}</div>
        <div className="badges">
          {badges.map((b) => (
            <div key={b.key} className={b.unlocked ? "badge" : "badge lock"}>
              <div className="seal">{b.seal}</div>
              <div className="t">{b.name}</div>
              <div className="s">{b.desc}</div>
            </div>
          ))}
        </div>

        <div className="notice" style={{ marginTop: 16 }}>
          <span className="seal">待</span>
          <span>{hint}</span>
        </div>
      </div>
    </div>
  );
}
