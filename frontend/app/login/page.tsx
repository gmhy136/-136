"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { login } from "@/lib/api";
import { setTokens } from "@/lib/auth";
import { useAuthStore } from "@/store/auth";

export default function LoginPage() {
  const router = useRouter();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const mutation = useMutation({
    mutationFn: () => login(email, password),
    onSuccess: (data) => {
      setTokens(data.accessToken, data.refreshToken, data.user);
      setAuth(data.user);
      router.push("/");
    },
  });

  return (
    <div className="app auth">
      <div className="brand">云记录</div>
      <div className="slogan">写给自己的私人手记</div>
      <form
        className="box"
        onSubmit={(e) => {
          e.preventDefault();
          mutation.mutate();
        }}
      >
        <h3>欢迎回来</h3>
        <div className="sub">慢慢写，不必着急。</div>
        <label className="f">邮箱</label>
        <input
          className="inp"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@example.com"
        />
        <label className="f">密码</label>
        <input
          className="inp"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="你的密码"
        />
        {mutation.isError && (
          <div className="err">
            {mutation.error instanceof Error ? mutation.error.message : "登录失败"}
          </div>
        )}
        <button
          type="submit"
          className="btn btn-p btn-block"
          style={{ marginTop: 16 }}
          disabled={mutation.isPending}
        >
          {mutation.isPending ? "登录中…" : "登录"}
        </button>
        <div className="foot">
          还没有账号？<Link href="/register">去注册</Link>
        </div>
      </form>
      <div className="note">不必着急，慢慢写。</div>
    </div>
  );
}
