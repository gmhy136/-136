"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { assetUrl, getOverview } from "@/lib/api";
import { progressPct, visibilityLabel } from "@/lib/ideal";
import { aiTypeLabel, formatTime, isAiType, styleOf } from "@/lib/record";
import type { IdealCard, RecordCard } from "@/lib/types";

type Tab = "cards" | "ideals" | "stats";

const TABS: { key: Tab; label: string }[] = [
  { key: "cards", label: "我的卡片" },
  { key: "ideals", label: "我的理想" },
  { key: "stats", label: "统计" },
];

/** "2026-10-13T..." → 加入于 2026 年 10 月。 */
function joinLine(createdAt: string, recordDays: number): string {
  const d = new Date(createdAt);
  return `加入于 ${d.getFullYear()} 年 ${d.getMonth() + 1} 月 · 记录 ${recordDays} 天`;
}

export default function MePage() {
  const router = useRouter();
  const [tab, setTab] = useState<Tab>("cards");
  const { data, isPending, isError } = useQuery({
    queryKey: ["overview"],
    queryFn: getOverview,
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

  const { user } = data;
  const activeIdeals = data.ideals.filter((i) => i.status === "ACTIVE");
  const doneIdeals = data.ideals.filter((i) => i.status === "DONE");

  const renderRecord = (r: RecordCard) => {
    const st = styleOf(r.styleKey);
    return (
      <div
        key={r.id}
        className={`rec ${st.cls}`}
        style={{ cursor: "pointer" }}
        onClick={() => router.push(`/record/${r.id}/edit`)}
      >
        <div className="top">
          <div className="mood">
            {r.moodIcon && <i className={`mdot ${r.moodIcon}`} />}
            {r.moodName ?? "记录"}
          </div>
          <span className="time">{formatTime(r.createdAt)}</span>
        </div>
        <p>{r.content}</p>
        <div className="foot">
          <span className={isAiType(r.aiType) ? "tag ai" : "tag"}>{aiTypeLabel(r.aiType)}</span>
          <span className="time">{st.name}</span>
        </div>
      </div>
    );
  };

  const renderIdeal = (i: IdealCard) => {
    const pct = progressPct(i.completedTaskCount, i.taskCount);
    return (
      <div
        key={i.id}
        className="ideal"
        style={i.status === "DONE" ? { opacity: 0.75, cursor: "pointer" } : { cursor: "pointer" }}
        onClick={() => router.push(`/ideal/${i.id}`)}
      >
        <div className="t">{i.title}</div>
        <div className="bar">
          <i style={{ width: `${pct}%` }} />
        </div>
        <div className="meta">
          <span>
            {i.status === "DONE" ? "已完成" : `${i.completedTaskCount} / ${i.taskCount} 步`}
          </span>
          <span>{visibilityLabel(i.visibility)}</span>
        </div>
      </div>
    );
  };

  const stats: { v: number; l: string }[] = [
    { v: data.recordCount, l: "张记录卡" },
    { v: data.recordDays, l: "记录天数" },
    { v: data.streakDays, l: "连续打卡（天）" },
    { v: data.monthCheckInCount, l: "本月打卡（天）" },
    { v: data.idealCount, l: "个理想" },
    { v: data.idealDoneCount, l: "已完成理想" },
    { v: data.capsuleCount, l: "封胶囊" },
    { v: data.capsuleOpenedCount, l: "已开启胶囊" },
  ];

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="profile">
          <div className="ava">
            {user.avatarUrl ? (
              <img
                src={assetUrl(user.avatarUrl)}
                alt="头像"
                style={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: 3 }}
              />
            ) : (
              user.nickname?.slice(0, 1) || "我"
            )}
          </div>
          <div className="nick">{user.nickname}</div>
          <div className="bio">{user.bio ? `「${user.bio}」` : "写下你的签名。"}</div>
          <div className="join">{joinLine(user.createdAt, data.recordDays)}</div>
        </div>

        <div className="tiles">
          <div className="tile">
            <div className="v">{data.recordCount}</div>
            <div className="l">张记录卡</div>
          </div>
          <div className="tile">
            <div className="v">{data.idealCount}</div>
            <div className="l">个理想</div>
          </div>
          <div className="tile">
            <div className="v">{data.capsuleCount}</div>
            <div className="l">封胶囊</div>
          </div>
        </div>

        <div className="tabs">
          {TABS.map((t) => (
            <a
              key={t.key}
              className={tab === t.key ? "on" : undefined}
              style={{ cursor: "pointer" }}
              onClick={() => setTab(t.key)}
            >
              {t.label}
            </a>
          ))}
        </div>

        {tab === "cards" && (
          <>
            <div className="sect-t">最近写下</div>
            {data.recentRecords.length === 0 ? (
              <div className="empty">
                <div className="t">还没有记录</div>
                <div className="s">写下一张卡片，留住今天。</div>
              </div>
            ) : (
              data.recentRecords.map(renderRecord)
            )}
          </>
        )}

        {tab === "ideals" && (
          <>
            {activeIdeals.length > 0 && (
              <>
                <div className="sect-t">进行中</div>
                {activeIdeals.map(renderIdeal)}
              </>
            )}
            {doneIdeals.length > 0 && (
              <>
                <div className="sect-t">已完成</div>
                {doneIdeals.map(renderIdeal)}
              </>
            )}
            {data.ideals.length === 0 && (
              <div className="empty">
                <div className="t">还没有理想</div>
                <div className="s">种下一个，从今天开始浇灌。</div>
              </div>
            )}
          </>
        )}

        {tab === "stats" && (
          <div className="tiles">
            {stats.map((s) => (
              <div key={s.l} className="tile">
                <div className="v">{s.v}</div>
                <div className="l">{s.l}</div>
              </div>
            ))}
          </div>
        )}

        <Link href="/settings" className="btn btn-g btn-block" style={{ marginTop: 18 }}>
          基础设置
        </Link>
      </div>
    </div>
  );
}
