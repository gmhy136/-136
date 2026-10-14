"use client";

import type { FormEvent, ReactNode } from "react";
import { useEffect, useRef, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { listMoods } from "@/lib/api";
import { recommendStyle, streamExpand } from "@/lib/ai";
import { STYLE_LIST, styleOf } from "@/lib/record";
import type { RecordInput, StyleRecommendation } from "@/lib/types";
import ImageUploader from "./ImageUploader";

interface Props {
  initial?: {
    content: string;
    moodId: number | null;
    imageUrls: string[];
    styleKey?: string | null;
    aiType?: string | null;
  };
  submitLabel: string;
  submitting: boolean;
  error: string | null;
  onSubmit: (input: RecordInput) => void;
  extraActions?: ReactNode;
}

export default function RecordForm({
  initial,
  submitLabel,
  submitting,
  error,
  onSubmit,
  extraActions,
}: Props) {
  const { data: moods = [] } = useQuery({ queryKey: ["moods"], queryFn: listMoods });

  const [content, setContent] = useState(initial?.content ?? "");
  // undefined = 尚未选择（新建时默认选中第一个心情）；null = 明确无心情（编辑时）。
  const [moodId, setMoodId] = useState<number | null | undefined>(
    initial ? initial.moodId : undefined
  );
  const [images, setImages] = useState<string[]>(initial?.imageUrls ?? []);
  const [styleKey, setStyleKey] = useState<string | null>(initial?.styleKey ?? null);
  const [aiType, setAiType] = useState<string | null>(initial?.aiType ?? null);
  const [emptyError, setEmptyError] = useState(false);

  // AI 扩写
  const [expanding, setExpanding] = useState(false);
  const [expandText, setExpandText] = useState("");
  const [expandRemaining, setExpandRemaining] = useState<number | null>(null);
  const [expandError, setExpandError] = useState("");
  const expandAbortRef = useRef<AbortController | null>(null);

  // AI 风格推荐
  const [stylePanelOpen, setStylePanelOpen] = useState(false);
  const [styleLoading, setStyleLoading] = useState(false);
  const [styleRec, setStyleRec] = useState<StyleRecommendation | null>(null);
  const [styleError, setStyleError] = useState("");
  const [pickedStyle, setPickedStyle] = useState<string | null>(null);

  const selectedMoodId = moodId === undefined ? (moods[0]?.id ?? null) : moodId;
  const selectedMood = moods.find((m) => m.id === selectedMoodId) ?? null;

  useEffect(() => () => expandAbortRef.current?.abort(), []);

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!content.trim()) {
      setEmptyError(true);
      return;
    }
    setEmptyError(false);
    onSubmit({
      content: content.trim(),
      moodId: selectedMoodId,
      styleKey,
      aiType,
      imageUrls: images,
    });
  };

  // ---- AI 扩写 ----
  const handleExpand = async () => {
    if (!content.trim()) {
      setEmptyError(true);
      return;
    }
    setEmptyError(false);
    setStylePanelOpen(false);
    setExpandError("");
    setExpandText("");
    setExpanding(true);
    const ctrl = new AbortController();
    expandAbortRef.current = ctrl;
    try {
      await streamExpand(
        content.trim(),
        selectedMoodId,
        {
          onStart: (r) => setExpandRemaining(r),
          onDelta: (t) => setExpandText((prev) => prev + t),
          onDone: () => setExpanding(false),
          onError: (m) => {
            setExpandError(m);
            setExpanding(false);
          },
        },
        ctrl.signal
      );
    } catch (err) {
      setExpandError(err instanceof Error ? err.message : "AI 暂时不可用，请稍后再试");
      setExpanding(false);
    }
  };

  const closeExpand = () => {
    expandAbortRef.current?.abort();
    setExpanding(false);
    setExpandText("");
    setExpandError("");
  };

  const adoptExpand = () => {
    if (expandText.trim()) {
      setContent(expandText.trim());
      setAiType("AI扩写");
    }
    closeExpand();
  };

  // ---- AI 风格推荐 ----
  const handleStyleRecommend = async () => {
    if (!content.trim()) {
      setEmptyError(true);
      return;
    }
    setEmptyError(false);
    closeExpand();
    setStylePanelOpen(true);
    setStyleError("");
    setStyleRec(null);
    setPickedStyle(styleKey);
    setStyleLoading(true);
    try {
      const rec = await recommendStyle(content.trim(), selectedMoodId);
      setStyleRec(rec);
      setPickedStyle(rec.styleKey);
    } catch (err) {
      setStyleError(err instanceof Error ? err.message : "AI 暂时不可用，可手动选择");
    } finally {
      setStyleLoading(false);
    }
  };

  const applyStyle = () => {
    if (pickedStyle) setStyleKey(pickedStyle);
    setStylePanelOpen(false);
  };

  const expandPanelOpen = expanding || expandText !== "" || expandError !== "";

  return (
    <form onSubmit={handleSubmit}>
      <label className="f">今天的心情</label>
      <div className="moodpick">
        {moods.map((m) => (
          <div
            key={m.id}
            className={m.id === selectedMoodId ? "m on" : "m"}
            onClick={() => setMoodId(m.id)}
          >
            <i className={`mdot ${m.weatherIcon}`} />
            {m.name}
          </div>
        ))}
      </div>

      <label className="f">正文</label>
      <textarea
        className="inp"
        placeholder="写点什么，今天过得怎么样？"
        value={content}
        onChange={(e) => {
          setContent(e.target.value);
          if (emptyError) setEmptyError(false);
        }}
      />
      <div className="hint">也可以让 AI 帮你写。</div>

      <div className="row-list" style={{ marginTop: 12 }}>
        <div className="li" style={{ cursor: "pointer" }} onClick={handleExpand}>
          <div className="txt">
            <div className="t">AI 扩写</div>
            <div className="s">让这句话更自然</div>
          </div>
          <span className="tag ai">
            {expandRemaining != null ? `剩余 ${expandRemaining} 次` : "可用 20 次"}
          </span>
        </div>
        <div className="li" style={{ cursor: "pointer" }} onClick={handleStyleRecommend}>
          <div className="txt">
            <div className="t">AI 风格推荐</div>
            <div className="s">{styleKey ? `已选 · ${styleOf(styleKey).name}` : "按心情挑一个卡片风格"}</div>
          </div>
          <span className="tag">智能</span>
        </div>
      </div>

      {expandPanelOpen && (
        <div className="ai-panel">
          <div className="ai-head">
            <span className="ai-tag">AI</span>
            <span className="t">{expanding ? "正在润色" : "润色结果"}</span>
            {expandRemaining != null && <span className="cost">剩余 {expandRemaining} 次</span>}
          </div>
          <div className="ai-body">
            {expandError ? (
              <span style={{ color: "var(--ink-soft)" }}>{expandError}</span>
            ) : (
              <>
                {expandText}
                {expanding && <span className="cursor" />}
              </>
            )}
          </div>
          <div className="ai-actions">
            <button
              type="button"
              className="btn btn-p sm"
              onClick={adoptExpand}
              disabled={!expandText.trim()}
            >
              采纳
            </button>
            <button type="button" className="btn btn-g sm" onClick={closeExpand}>
              丢弃
            </button>
            <button
              type="button"
              className="btn btn-s sm"
              onClick={handleExpand}
              disabled={expanding}
            >
              再写一次
            </button>
          </div>
        </div>
      )}

      {stylePanelOpen && (
        <>
          <div className="ai-panel" style={{ marginTop: 12 }}>
            <div className="ai-head">
              <span className="ai-tag">AI</span>
              <span className="t">推荐</span>
              {selectedMood && <span className="cost">心情：{selectedMood.name}</span>}
            </div>
            {styleLoading ? (
              <div className="ai-body">正在为你挑选一个风格…</div>
            ) : styleRec ? (
              <div className="ai-body" style={{ display: "flex", alignItems: "center", gap: 10 }}>
                <span
                  style={{
                    width: 14,
                    height: 34,
                    borderRadius: 2,
                    background: styleOf(styleRec.styleKey).swatch,
                    flex: "none",
                    display: "inline-block",
                  }}
                />
                <span>
                  {styleOf(styleRec.styleKey).name} —— {styleRec.reason}
                </span>
              </div>
            ) : styleError ? (
              <div className="ai-body">{styleError}</div>
            ) : null}
          </div>

          <label className="f">全部风格</label>
          <div className="styles-grid">
            {STYLE_LIST.map((s) => (
              <div
                key={s.key}
                className={`st${pickedStyle === s.key ? " sel" : ""}`}
                onClick={() => setPickedStyle(s.key)}
              >
                <span className={`bar ${s.bar}`} />
                <div className="t">{s.name}</div>
              </div>
            ))}
          </div>

          {pickedStyle && (
            <div className={`rec ${styleOf(pickedStyle).cls}`} style={{ marginTop: 16 }}>
              <div className="top">
                <span className="mood">
                  {selectedMood?.weatherIcon && <i className={`mdot ${selectedMood.weatherIcon}`} />}
                  {selectedMood?.name ?? "此刻"}
                </span>
                <span className="time">预览</span>
              </div>
              <p>{content.trim() || "写点什么，这里会是预览。"}</p>
              <div className="foot">
                <span className="tag ai">{styleOf(pickedStyle).name}</span>
                <span className="time">效果预览</span>
              </div>
            </div>
          )}

          <button
            type="button"
            className="btn btn-p btn-block"
            style={{ marginTop: 16 }}
            onClick={applyStyle}
            disabled={!pickedStyle}
          >
            应用这个风格
          </button>
        </>
      )}

      <label className="f">配图（最多 3 张）</label>
      <ImageUploader images={images} onChange={setImages} />

      {(emptyError || error) && (
        <div className="err">{emptyError ? "内容不能为空" : error}</div>
      )}

      <div style={{ display: "flex", gap: 8, marginTop: 16 }}>
        <button
          type="submit"
          className={extraActions ? "btn btn-p" : "btn btn-p btn-block"}
          style={extraActions ? { flex: 1 } : undefined}
          disabled={submitting}
        >
          {submitting ? "保存中…" : submitLabel}
        </button>
        {extraActions}
      </div>
    </form>
  );
}
