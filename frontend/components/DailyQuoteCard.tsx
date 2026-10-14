"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "@/store/auth";
import { getDailyQuote } from "@/lib/api";

export default function DailyQuoteCard() {
  const user = useAuthStore((s) => s.user);
  const [greeting, setGreeting] = useState("");

  useEffect(() => {
    const h = new Date().getHours();
    setGreeting(h < 5 ? "夜深了" : h < 12 ? "早上好" : h < 18 ? "下午好" : "晚上好");
  }, []);

  const { data } = useQuery({
    queryKey: ["daily-quote"],
    queryFn: getDailyQuote,
    enabled: !!user,
  });

  const nick = user?.nickname || "";
  const avatarChar = nick.slice(0, 1) || "我";
  const title = user ? (greeting ? `${greeting}，${nick}` : nick) : "欢迎来到云记录";
  const bio = user ? "今天也记得对自己好一点。" : "登录后，AI 会为你写下一句今日的话。";

  return (
    <div className="content" style={{ textAlign: "center", paddingTop: 34 }}>
      <div className="profile">
        <div className="ava">{avatarChar}</div>
        <div className="nick">{title}</div>
        <div className="bio">{bio}</div>
      </div>

      <div
        style={{
          marginTop: 18,
          background: "var(--card)",
          border: "1.5px solid var(--line-strong)",
          borderRadius: 4,
          padding: "26px 18px",
          boxShadow: "var(--shadow)",
        }}
      >
        <span className="ai-tag" style={{ fontSize: 11 }}>AI</span>
        <div
          style={{
            fontFamily: "var(--font-hand)",
            fontSize: 19,
            color: "var(--blue-deep)",
            marginTop: 10,
            lineHeight: 1.8,
          }}
        >
          {data ? `「${data.content}」` : "……"}
        </div>
        <div className="hint" style={{ marginTop: 12 }}>根据你最近的心情生成 · 每天都不一样</div>
      </div>

      <div className="row-list" style={{ marginTop: 20, textAlign: "left" }}>
        <Link href="/record" className="li" style={{ textDecoration: "none" }}>
          <div className="txt">
            <div className="t">{user ? "今天还没写卡片" : "开始你的记录"}</div>
            <div className="s">{user ? "把这句，也记下来吧" : "登录后，写下第一张卡"}</div>
          </div>
          <span className="tag">{user ? "去写" : "去登录"}</span>
        </Link>
      </div>
    </div>
  );
}
