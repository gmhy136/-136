"use client";

import type { FormEvent } from "react";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { createCapsule } from "@/lib/api";
import { minOpenDate } from "@/lib/capsule";

export default function NewCapsulePage() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [openDate, setOpenDate] = useState("");
  const [emptyError, setEmptyError] = useState(false);
  const [min, setMin] = useState("");

  useEffect(() => {
    setMin(minOpenDate());
  }, []);

  const mutation = useMutation({
    mutationFn: createCapsule,
    onSuccess: (data) => router.push(`/capsule/${data.id}`),
  });

  const valid = content.trim().length > 0 && openDate.trim().length > 0;

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!content.trim() || !openDate.trim()) {
      setEmptyError(true);
      return;
    }
    setEmptyError(false);
    mutation.mutate({
      title: title.trim() || null,
      content: content.trim(),
      openDate: openDate.trim(),
    });
  };

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">写一封信给未来的自己</div>
          <div className="s">想对那时的自己，说点什么？</div>
        </div>

        <form onSubmit={handleSubmit}>
          <label className="f">标题（可选）</label>
          <input
            className="inp"
            placeholder="比如：给 2027 年的自己"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />

          <label className="f">信的内容</label>
          <textarea
            className="inp"
            placeholder={"亲爱的自己：\n\n不知道现在的你过得好不好……"}
            value={content}
            onChange={(e) => {
              setContent(e.target.value);
              if (emptyError) setEmptyError(false);
            }}
          />

          <label className="f">开启日期</label>
          <input
            className="inp"
            type="date"
            min={min}
            value={openDate}
            onChange={(e) => {
              setOpenDate(e.target.value);
              if (emptyError) setEmptyError(false);
            }}
          />
          <div className="hint">开启日期最早为明天，到期后我们会提醒你。</div>

          <div className="notice" style={{ marginTop: 14 }}>
            <span className="seal">密</span>
            <span>这封信会被好好保管，在开启日前，谁也看不到（包括你自己）。</span>
          </div>

          {(emptyError || mutation.error) && (
            <div className="err" style={{ marginTop: 10 }}>
              {emptyError
                ? "请填写信的内容和开启日期"
                : mutation.error instanceof Error
                  ? mutation.error.message
                  : "保存失败"}
            </div>
          )}

          <button
            type="submit"
            className="btn btn-p btn-block"
            style={{ marginTop: 16 }}
            disabled={!valid || mutation.isPending}
          >
            {mutation.isPending ? "封存中…" : "封存这封信"}
          </button>
        </form>
      </div>
    </div>
  );
}
