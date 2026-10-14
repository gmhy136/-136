"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { listIdeals } from "@/lib/api";
import { progressPct, visibilityLabel } from "@/lib/ideal";
import type { IdealCard } from "@/lib/types";

export default function IdealListPage() {
  const router = useRouter();
  const { data, isPending, isError } = useQuery({
    queryKey: ["ideals"],
    queryFn: listIdeals,
  });

  const ideals = data ?? [];
  const active = ideals.filter((i) => i.status === "ACTIVE");
  const done = ideals.filter((i) => i.status === "DONE");

  const renderIdeal = (i: IdealCard) => {
    const pct = progressPct(i.completedTaskCount, i.taskCount);
    return (
      <div
        key={i.id}
        className="ideal"
        style={{ cursor: "pointer" }}
        onClick={() => router.push(`/ideal/${i.id}`)}
      >
        <div className="t">{i.title}</div>
        <div className="d">{i.description || "没有描述，行动本身就是说明。"}</div>
        <div className="bar">
          <i style={{ width: `${pct}%` }} />
        </div>
        <div className="meta">
          <span>
            {i.completedTaskCount}/{i.taskCount} 步 · {visibilityLabel(i.visibility)}
          </span>
          <span>{pct}%</span>
        </div>
      </div>
    );
  };

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">理想</div>
          <div className="s">把想做的事，拆成一件件能打卡的小事。</div>
        </div>

        {isPending && (
          <div className="empty">
            <div className="t">加载中…</div>
          </div>
        )}

        {isError && (
          <div className="empty">
            <div className="t">加载失败</div>
            <div className="s">请确认后端已启动</div>
          </div>
        )}

        {!isPending && !isError && ideals.length === 0 && (
          <div className="empty">
            <div className="t">还没有理想</div>
            <div className="s">种下一个，从今天开始浇灌。</div>
          </div>
        )}

        {active.length > 0 && (
          <>
            <div className="sect-t">进行中</div>
            {active.map(renderIdeal)}
          </>
        )}

        {done.length > 0 && (
          <>
            <div className="sect-t">已完成</div>
            {done.map(renderIdeal)}
          </>
        )}

        <Link href="/ideal/new" className="btn btn-p btn-block" style={{ marginTop: 16 }}>
          种下一个新理想
        </Link>
      </div>
    </div>
  );
}
