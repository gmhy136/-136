"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { listFeed } from "@/lib/quiz";
import { formatTime } from "@/lib/record";

function avatar(nickname: string) {
  return (
    <span
      style={{
        width: 26,
        height: 26,
        flex: "none",
        borderRadius: 8,
        background: "var(--cyan-tint)",
        border: "1px solid var(--cyan-line)",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        fontFamily: "var(--font-serif)",
        fontSize: 13,
        color: "var(--ink)",
      }}
    >
      {nickname.slice(0, 1) || "?"}
    </span>
  );
}

export default function FeedPage() {
  const router = useRouter();
  const { data: items = [], isPending, isError } = useQuery({
    queryKey: ["feed"],
    queryFn: listFeed,
  });

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">问答</div>
          <div className="s">朋友出的题，等你来答。</div>
        </div>

        <div style={{ display: "flex", gap: 8, marginBottom: 16, flexWrap: "wrap" }}>
          <Link href="/quiz/new" className="btn btn-p">
            出题
          </Link>
          <Link href="/quiz" className="btn btn-g">
            我的题目
          </Link>
          <Link href="/friends" className="btn btn-g">
            朋友
          </Link>
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
        {!isPending && !isError && items.length === 0 && (
          <div className="empty">
            <div className="t">朋友圈还很安静</div>
            <div className="s">出一道题，或者加个好友吧。</div>
          </div>
        )}

        {items.map((q) => (
          <div
            key={q.id}
            className="rec"
            style={{ cursor: "pointer" }}
            onClick={() => router.push(`/quiz/${q.id}/play`)}
          >
            <div className="top">
              <span className="mood">
                {avatar(q.ownerNickname)}
                {q.ownerNickname}
              </span>
              <span className="time">{formatTime(q.publishedAt)}</span>
            </div>
            <p style={{ fontFamily: "var(--font-serif)", fontWeight: 600, marginBottom: 4 }}>
              {q.title}
            </p>
            {q.description && (
              <p style={{ color: "var(--ink-soft)", fontSize: 12.5, marginBottom: 8 }}>
                {q.description}
              </p>
            )}
            <div className="foot">
              <span className="tag">{q.questionCount} 题</span>
              <span className={q.answered ? "tag on" : "tag ghost"}>
                {q.answered ? "已答" : "去答题"}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
