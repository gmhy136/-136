"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import TopNav from "@/components/TopNav";
import { acceptInvite } from "@/lib/friend";

export default function InviteView({ token }: { token: string }) {
  const [state, setState] = useState<"loading" | "ok" | "error">("loading");
  const [message, setMessage] = useState("");

  useEffect(() => {
    let cancelled = false;
    acceptInvite(token)
      .then(() => {
        if (!cancelled) {
          setState("ok");
          setMessage("已发送好友申请，等待对方通过。");
        }
      })
      .catch((e) => {
        if (!cancelled) {
          setState("error");
          setMessage(e instanceof Error ? e.message : "邀请链接无效");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [token]);

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">好友邀请</div>
          <div className="s">通过链接加入好友。</div>
        </div>
        <div className="empty">
          <div className="t">
            {state === "loading" ? "正在处理邀请…" : state === "ok" ? "已发送申请" : "处理失败"}
          </div>
          <div className="s">{message || (state === "loading" ? "请稍候" : "")}</div>
        </div>
        <Link href="/friends" className="btn btn-p btn-block" style={{ marginTop: 12 }}>
          去好友页
        </Link>
      </div>
    </div>
  );
}
