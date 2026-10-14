"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { assetUrl, getAiRemaining, getMe, updateMe, uploadImage } from "@/lib/api";
import { useAuthStore } from "@/store/auth";

const CARD_STYLE: React.CSSProperties = {
  background: "var(--card)",
  border: "1.5px solid var(--line-strong)",
  borderRadius: 4,
  padding: "4px 14px",
  boxShadow: "var(--shadow)",
};

export default function SettingsPage() {
  const router = useRouter();
  const qc = useQueryClient();
  const setAuth = useAuthStore((s) => s.setAuth);
  const logout = useAuthStore((s) => s.logout);

  const fileRef = useRef<HTMLInputElement>(null);
  const [nickname, setNickname] = useState("");
  const [bio, setBio] = useState("");
  const [saved, setSaved] = useState(false);
  const [uploadErr, setUploadErr] = useState("");

  const { data: user } = useQuery({ queryKey: ["me"], queryFn: getMe });
  const { data: aiRemaining } = useQuery({
    queryKey: ["ai-remaining"],
    queryFn: getAiRemaining,
  });

  useEffect(() => {
    if (user) {
      setNickname(user.nickname ?? "");
      setBio(user.bio ?? "");
    }
  }, [user]);

  const save = useMutation({
    mutationFn: () => updateMe({ nickname, bio }),
    onSuccess: (u) => {
      setAuth(u);
      qc.invalidateQueries({ queryKey: ["overview"] });
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    },
  });

  const handleAvatar = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = "";
    if (!file) return;
    setUploadErr("");
    try {
      const { url } = await uploadImage(file);
      const u = await updateMe({ avatarUrl: url });
      setAuth(u);
      qc.invalidateQueries({ queryKey: ["me"] });
      qc.invalidateQueries({ queryKey: ["overview"] });
    } catch (err) {
      setUploadErr(err instanceof Error ? err.message : "上传失败");
    }
  };

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  const avatarChar = user?.nickname?.slice(0, 1) || "我";

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">基础设置</div>
          <div className="s">这里只保留必要的东西。</div>
        </div>

        <div className="sect-t">个人资料</div>
        <div style={CARD_STYLE}>
          <div className="set-row">
            <div className="txt">
              <div className="t">头像</div>
              <div className="s">云记录支持上传头像</div>
            </div>
            <div
              className="avatar"
              style={{ marginLeft: 0, width: 30, height: 30, fontSize: 14, cursor: "pointer" }}
              onClick={() => fileRef.current?.click()}
            >
              {user?.avatarUrl ? (
                <img
                  src={assetUrl(user.avatarUrl)}
                  alt="头像"
                  style={{ width: "100%", height: "100%", objectFit: "cover", borderRadius: 3 }}
                />
              ) : (
                avatarChar
              )}
            </div>
          </div>
          <div className="set-row">
            <div className="txt">
              <div className="t">昵称</div>
            </div>
            <input
              className="inp"
              style={{ width: 110 }}
              value={nickname}
              onChange={(e) => setNickname(e.target.value)}
            />
          </div>
          <div className="set-row">
            <div className="txt">
              <div className="t">简介</div>
            </div>
            <input
              className="inp"
              style={{ width: 130 }}
              value={bio}
              onChange={(e) => setBio(e.target.value)}
            />
          </div>
          <input
            ref={fileRef}
            type="file"
            accept="image/jpeg,image/png,image/webp"
            hidden
            onChange={handleAvatar}
          />
        </div>

        <div style={{ marginTop: 10, display: "flex", alignItems: "center", gap: 8, flexWrap: "wrap" }}>
          <button className="btn btn-s sm" disabled={save.isPending} onClick={() => save.mutate()}>
            {save.isPending ? "保存中…" : saved ? "已保存" : "保存资料"}
          </button>
          {uploadErr && <span className="err" style={{ marginTop: 0 }}>{uploadErr}</span>}
          {save.error && <span className="err" style={{ marginTop: 0 }}>{save.error.message}</span>}
        </div>

        <div className="sect-t">AI 助手</div>
        <div style={CARD_STYLE}>
          <div className="set-row">
            <div className="txt">
              <div className="t">今日剩余 AI 次数</div>
              <div className="s">每天每种能力 20 次，明天自动恢复</div>
            </div>
            <span className="tag on">
              {aiRemaining ? `${aiRemaining.remaining} / ${aiRemaining.limit}` : "20 / 20"}
            </span>
          </div>
          <div className="set-row" style={{ opacity: 0.55 }}>
            <div className="txt">
              <div className="t">每日一句</div>
              <div className="s">二期开放，敬请期待</div>
            </div>
            <div className="switch"></div>
          </div>
        </div>

        <div className="sect-t">通知</div>
        <div style={CARD_STYLE}>
          <div className="set-row">
            <div className="txt">
              <div className="t">时间胶囊到期提醒</div>
              <div className="s">开启日当天站内提醒</div>
            </div>
            <div className="switch on"></div>
          </div>
          <div className="set-row" style={{ opacity: 0.55 }}>
            <div className="txt">
              <div className="t">邮件提醒</div>
              <div className="s">二期开放，敬请期待</div>
            </div>
            <div className="switch"></div>
          </div>
        </div>

        <button className="btn btn-g btn-block" style={{ marginTop: 16 }} onClick={handleLogout}>
          退出登录
        </button>
      </div>
    </div>
  );
}
