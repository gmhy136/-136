"use client";

import type { FormEvent } from "react";
import { useState } from "react";
import type { IdealInput } from "@/lib/types";

interface Props {
  submitLabel: string;
  submitting: boolean;
  error: string | null;
  onSubmit: (input: IdealInput) => void;
}

export default function IdealForm({ submitLabel, submitting, error, onSubmit }: Props) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [targetDate, setTargetDate] = useState("");
  const [emptyError, setEmptyError] = useState(false);

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      setEmptyError(true);
      return;
    }
    setEmptyError(false);
    onSubmit({
      title: title.trim(),
      description: description.trim() || null,
      visibility: "PRIVATE",
      targetDate: targetDate.trim() || null,
    });
  };

  return (
    <form onSubmit={handleSubmit}>
      <label className="f">理想标题</label>
      <input
        className="inp"
        placeholder="比如：学会弹一首完整的歌"
        value={title}
        onChange={(e) => {
          setTitle(e.target.value);
          if (emptyError) setEmptyError(false);
        }}
      />

      <label className="f">描述（可选）</label>
      <textarea
        className="inp"
        style={{ minHeight: 64 }}
        placeholder="它为什么重要？想对未来的自己说点什么？"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
      />

      <label className="f">可见性</label>
      <div className="row-list">
        <div className="li">
          <div className="txt">
            <div className="t">私密</div>
            <div className="s">只给自己看</div>
          </div>
          <div className="switch on"></div>
        </div>
        <div className="li" style={{ opacity: 0.6 }}>
          <div className="txt">
            <div className="t">公开</div>
            <div className="s">展示在个人主页（二期开放）</div>
          </div>
          <div className="switch"></div>
        </div>
      </div>

      <label className="f">目标日期（可选）</label>
      <input
        className="inp"
        placeholder="2026-12-31"
        value={targetDate}
        onChange={(e) => setTargetDate(e.target.value)}
      />

      {(emptyError || error) && (
        <div className="err">{emptyError ? "理想标题不能为空" : error}</div>
      )}

      <button
        type="submit"
        className="btn btn-p btn-block"
        style={{ marginTop: 16 }}
        disabled={submitting}
      >
        {submitting ? "创建中…" : submitLabel}
      </button>
      <div className="hint" style={{ textAlign: "center", marginTop: 8 }}>
        创建后可以让 AI 帮你拆解成小步骤
      </div>
    </form>
  );
}
