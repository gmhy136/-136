"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQuery } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { deleteCapsule, getCapsule } from "@/lib/api";
import { countdown, formatDateCN } from "@/lib/capsule";

export default function CapsuleDetail({ id }: { id: number }) {
  const router = useRouter();
  const [now, setNow] = useState<number | null>(null);

  useEffect(() => {
    setNow(Date.now());
    const t = setInterval(() => setNow(Date.now()), 30_000);
    return () => clearInterval(t);
  }, []);

  const { data: capsule, isPending, isError } = useQuery({
    queryKey: ["capsule", id],
    queryFn: () => getCapsule(id),
  });

  const del = useMutation({
    mutationFn: () => deleteCapsule(id),
    onSuccess: () => router.push("/capsule"),
  });

  const locked = !!capsule && capsule.content == null;

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        {isPending && (
          <div className="empty">
            <div className="t">加载中…</div>
          </div>
        )}

        {isError && (
          <div className="empty">
            <div className="t">胶囊不存在或已删除</div>
            <div className="s">它可能已经被移走了。</div>
          </div>
        )}

        {capsule && locked && (
          <div style={{ textAlign: "center", paddingTop: 20 }}>
            <div className="env" style={{ maxWidth: 240, margin: "0 auto" }}>
              <div className="flap"></div>
              <div className="lock">密封</div>
              <div className="t">{capsule.title ?? "给未来的自己"}</div>
            </div>

            <div className="page-head" style={{ marginTop: 24 }}>
              <div className="t">这封信还在时间里旅行</div>
            </div>
            <div style={{ color: "var(--ink-soft)", fontSize: 12 }}>
              将于 <b style={{ color: "var(--blue-deep)" }}>{formatDateCN(capsule.openDate)}</b> 开启
            </div>

            {now != null && <CountdownTiles openDate={capsule.openDate} now={now} />}

            <div className="note" style={{ textAlign: "center", borderLeft: "none", paddingLeft: 0, marginTop: 24 }}>
              留给未来的自己，先不打开。
            </div>

            <button
              className="btn btn-g btn-block"
              style={{ marginTop: 16 }}
              disabled={del.isPending}
              onClick={() => {
                if (window.confirm("确定删除这封信吗？")) del.mutate();
              }}
            >
              {del.isPending ? "删除中…" : "删除这封信"}
            </button>
          </div>
        )}

        {capsule && !locked && (
          <>
            <div className="page-head" style={{ textAlign: "center" }}>
              <div className="t">你的信到了</div>
              <div className="s">来自 {formatDateCN(capsule.createdAt)} 的你</div>
            </div>

            <div
              style={{
                background: "var(--card)",
                border: "1.5px solid var(--line-strong)",
                borderLeft: "3px solid var(--warm)",
                borderRadius: 4,
                padding: "18px 16px",
                fontFamily: "'Ma Shan Zheng','Noto Serif SC'",
                fontSize: "14.5px",
                lineHeight: 1.9,
                color: "var(--ink)",
                boxShadow: "var(--shadow)",
                whiteSpace: "pre-wrap",
              }}
            >
              {capsule.content}
            </div>

            <div className="note">你始终比想象中更笃定。</div>

            <button
              className="btn btn-p btn-block"
              style={{ marginTop: 16 }}
              onClick={() => router.push("/capsule/new")}
            >
              再写一封给未来
            </button>

            <button
              className="btn btn-g btn-block"
              style={{ marginTop: 10 }}
              disabled={del.isPending}
              onClick={() => {
                if (window.confirm("确定删除这封信吗？")) del.mutate();
              }}
            >
              {del.isPending ? "删除中…" : "删除这封信"}
            </button>
          </>
        )}
      </div>
    </div>
  );
}

function CountdownTiles({ openDate, now }: { openDate: string; now: number }) {
  const cd = countdown(openDate, now);
  return (
    <div
      className="tiles"
      style={{ gridTemplateColumns: "repeat(3,1fr)", maxWidth: 240, margin: "20px auto 0" }}
    >
      <div className="tile">
        <div className="v">{cd.days}</div>
        <div className="l">天</div>
      </div>
      <div className="tile">
        <div className="v">{cd.hours}</div>
        <div className="l">时</div>
      </div>
      <div className="tile">
        <div className="v">{cd.minutes}</div>
        <div className="l">分</div>
      </div>
    </div>
  );
}
