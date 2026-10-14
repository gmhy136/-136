"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { register, sendCode } from "@/lib/api";
import { setTokens } from "@/lib/auth";
import { useAuthStore } from "@/store/auth";

export default function RegisterPage() {
  const router = useRouter();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [email, setEmail] = useState("");
  const [code, setCode] = useState("");
  const [nickname, setNickname] = useState("");
  const [password, setPassword] = useState("");
  const [notice, setNotice] = useState("");

  const send = useMutation({
    mutationFn: () => sendCode(email),
    onSuccess: () => setNotice("验证码已发送（开发环境请查看后端日志）"),
  });

  const reg = useMutation({
    mutationFn: () => register({ email, code, nickname, password }),
    onSuccess: (data) => {
      setTokens(data.accessToken, data.refreshToken, data.user);
      setAuth(data.user);
      router.push("/");
    },
  });

  return (
    <div className="app auth">
      <div className="brand">开始记录</div>
      <div className="slogan">记下此刻，也种下将来</div>
      <form
        className="box"
        onSubmit={(e) => {
          e.preventDefault();
          setNotice("");
          reg.mutate();
        }}
      >
        <h3>创建账号</h3>
        <div className="sub">一个邮箱，就能拥有自己的一方天地。</div>
        <label className="f">邮箱</label>
        <input
          className="inp"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@example.com"
        />
        <label className="f">验证码</label>
        <div className="code-row">
          <input
            className="inp"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            placeholder="6 位验证码"
          />
          <button
            type="button"
            className="btn btn-s"
            style={{ flex: "none" }}
            disabled={send.isPending}
            onClick={() => {
              setNotice("");
              send.mutate();
            }}
          >
            {send.isPending ? "发送中" : "获取"}
          </button>
        </div>
        <label className="f">昵称</label>
        <input
          className="inp"
          value={nickname}
          onChange={(e) => setNickname(e.target.value)}
          placeholder="怎么称呼你？"
        />
        <label className="f">密码</label>
        <input
          className="inp"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="至少 6 位"
        />
        {notice && (
          <div className="hint" style={{ marginTop: 12 }}>
            {notice}
          </div>
        )}
        {reg.isError && (
          <div className="err">
            {reg.error instanceof Error ? reg.error.message : "注册失败"}
          </div>
        )}
        <button
          type="submit"
          className="btn btn-p btn-block"
          style={{ marginTop: 16 }}
          disabled={reg.isPending}
        >
          {reg.isPending ? "注册中…" : "注册，开始记录"}
        </button>
        <div className="foot">
          已有账号？<Link href="/login">直接登录</Link>
        </div>
      </form>
    </div>
  );
}
