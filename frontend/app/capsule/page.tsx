"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { listCapsules } from "@/lib/api";
import type { CapsuleCard } from "@/lib/types";

function lockText(c: CapsuleCard): string {
  if (c.state === "LOCKED") return "密封";
  if (c.state === "READY") return "可开启";
  return "已开启";
}

function subText(c: CapsuleCard): string {
  if (c.state === "LOCKED") return `${c.openDate} 开启 · 还剩 ${c.daysLeft} 天`;
  if (c.state === "READY") return `${c.openDate} 已到开启日`;
  return `开启于 ${c.openDate}`;
}

export default function CapsuleListPage() {
  const router = useRouter();
  const { data, isPending, isError } = useQuery({
    queryKey: ["capsules"],
    queryFn: listCapsules,
  });

  const capsules = data ?? [];
  const waiting = capsules.filter((c) => c.state === "LOCKED");
  const opened = capsules.filter((c) => c.state !== "LOCKED");

  const renderCapsule = (c: CapsuleCard) => (
    <div
      key={c.id}
      className={c.state === "OPENED" ? "env opened" : "env"}
      style={{ cursor: "pointer" }}
      onClick={() => router.push(`/capsule/${c.id}`)}
    >
      <div className="flap"></div>
      <div className="lock">{lockText(c)}</div>
      <div className="t">{c.title ?? "给未来的自己"}</div>
      <div className="s">{subText(c)}</div>
    </div>
  );

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">时间胶囊</div>
          <div className="s">写给未来的自己，一句一句，都在时间里等你。</div>
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

        {!isPending && !isError && capsules.length === 0 && (
          <div className="empty">
            <div className="t">还没有胶囊</div>
            <div className="s">写一封信，寄给未来的自己。</div>
          </div>
        )}

        {waiting.length > 0 && (
          <>
            <div className="sect-t">等待开启 · {waiting.length}</div>
            {waiting.map(renderCapsule)}
          </>
        )}

        {opened.length > 0 && (
          <>
            <div className="sect-t">已经开启 · {opened.length}</div>
            {opened.map(renderCapsule)}
          </>
        )}

        <Link href="/capsule/new" className="btn btn-p btn-block" style={{ marginTop: 16 }}>
          写一封信给未来
        </Link>
      </div>
    </div>
  );
}
