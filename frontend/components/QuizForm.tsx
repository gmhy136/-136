"use client";

import type { FormEvent } from "react";
import { useState } from "react";
import type { CreateQuizInput, QuizDetail, QuizOption, QuizQuestion } from "@/lib/types";

interface EditableOption {
  key: string;
  label: string;
}

interface EditableQuestion {
  key: string;
  type: "CHOICE" | "JUDGE";
  content: string;
  options: EditableOption[];
  correctIndex: number | null;
  judgeAnswer: boolean | null;
}

interface Props {
  initial?: QuizDetail;
  submitLabel: string;
  submitting: boolean;
  error: string | null;
  onSubmit: (input: CreateQuizInput) => void;
}

let seq = 0;
const nextKey = () => `k${++seq}`;

const cardStyle = {
  background: "var(--card)",
  border: "1px solid var(--line-strong)",
  borderRadius: "var(--r-lg)",
  padding: "14px",
  marginBottom: "12px",
} as const;

function newOption(): EditableOption {
  return { key: nextKey(), label: "" };
}

function emptyQuestion(): EditableQuestion {
  return {
    key: nextKey(),
    type: "CHOICE",
    content: "",
    options: [newOption(), newOption()],
    correctIndex: null,
    judgeAnswer: null,
  };
}

function questionToEditable(q: QuizQuestion): EditableQuestion {
  if (q.type === "CHOICE") {
    const options = (q.options ?? []).map((o: QuizOption) => ({ key: nextKey(), label: o.label }));
    const correctIndex = (q.options ?? []).findIndex((o) => o.isCorrect);
    return {
      key: nextKey(),
      type: "CHOICE",
      content: q.content,
      options,
      correctIndex: correctIndex >= 0 ? correctIndex : null,
      judgeAnswer: null,
    };
  }
  return {
    key: nextKey(),
    type: "JUDGE",
    content: q.content,
    options: [],
    correctIndex: null,
    judgeAnswer: q.judgeAnswer,
  };
}

export default function QuizForm({ initial, submitLabel, submitting, error, onSubmit }: Props) {
  const [title, setTitle] = useState(initial?.title ?? "");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [questions, setQuestions] = useState<EditableQuestion[]>(
    initial ? initial.questions.map(questionToEditable) : [emptyQuestion()]
  );
  const [clientError, setClientError] = useState("");

  const patchQuestion = (key: string, patch: Partial<EditableQuestion>) =>
    setQuestions((qs) => qs.map((q) => (q.key === key ? { ...q, ...patch } : q)));

  const addOption = (key: string) =>
    setQuestions((qs) =>
      qs.map((q) => (q.key === key ? { ...q, options: [...q.options, newOption()] } : q))
    );

  const removeOption = (key: string, optKey: string) =>
    setQuestions((qs) =>
      qs.map((q) =>
        q.key === key
          ? { ...q, options: q.options.filter((o) => o.key !== optKey) }
          : q
      )
    );

  const setOptionLabel = (key: string, optKey: string, label: string) =>
    setQuestions((qs) =>
      qs.map((q) =>
        q.key === key
          ? { ...q, options: q.options.map((o) => (o.key === optKey ? { ...o, label } : o)) }
          : q
      )
    );

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      setClientError("标题不能为空");
      return;
    }
    if (questions.length === 0) {
      setClientError("至少需要一道题");
      return;
    }
    for (const q of questions) {
      if (!q.content.trim()) {
        setClientError("题目内容不能为空");
        return;
      }
      if (q.type === "CHOICE") {
        if (q.options.filter((o) => o.label.trim()).length < 2) {
          setClientError("选择题至少需要两个选项");
          return;
        }
        if (q.options.some((o) => !o.label.trim())) {
          setClientError("选项内容不能为空");
          return;
        }
        if (q.correctIndex == null) {
          setClientError("请标记一个正确选项");
          return;
        }
      } else if (q.judgeAnswer == null) {
        setClientError("判断题需给出答案");
        return;
      }
    }
    setClientError("");

    const input: CreateQuizInput = {
      title: title.trim(),
      description: description.trim() || null,
      questions: questions.map((q) =>
        q.type === "CHOICE"
          ? {
              type: "CHOICE",
              content: q.content.trim(),
              options: q.options.map((o, i) => ({
                label: o.label.trim(),
                isCorrect: i === q.correctIndex,
              })),
            }
          : {
              type: "JUDGE",
              content: q.content.trim(),
              judgeAnswer: q.judgeAnswer,
            }
      ),
    };
    onSubmit(input);
  };

  return (
    <form onSubmit={handleSubmit}>
      <label className="f">标题</label>
      <input
        className="inp"
        placeholder="给问卷起个名字"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
      />

      <label className="f">简介（可选）</label>
      <input
        className="inp"
        placeholder="一句话说明"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
      />

      <label className="f">题目</label>
      {questions.map((q, qi) => (
        <div key={q.key} style={cardStyle}>
          <div style={{ display: "flex", alignItems: "center", gap: 6, marginBottom: 10 }}>
            <span
              className={`tag${q.type === "CHOICE" ? " on" : ""}`}
              style={{ cursor: "pointer" }}
              onClick={() =>
                patchQuestion(q.key, {
                  type: "CHOICE",
                  options: q.options.length >= 2 ? q.options : [newOption(), newOption()],
                  correctIndex: null,
                  judgeAnswer: null,
                })
              }
            >
              选择题
            </span>
            <span
              className={`tag${q.type === "JUDGE" ? " on" : ""}`}
              style={{ cursor: "pointer" }}
              onClick={() =>
                patchQuestion(q.key, { type: "JUDGE", judgeAnswer: q.judgeAnswer ?? true })
              }
            >
              判断题
            </span>
            <span style={{ marginLeft: "auto", fontSize: 10, color: "var(--ink-faint)" }}>
              {qi + 1}
            </span>
            {questions.length > 1 && (
              <button
                type="button"
                className="btn btn-g sm"
                onClick={() =>
                  setQuestions((qs) => qs.filter((x) => x.key !== q.key))
                }
              >
                删除
              </button>
            )}
          </div>

          <textarea
            className="inp"
            placeholder="题目内容"
            value={q.content}
            onChange={(e) => patchQuestion(q.key, { content: e.target.value })}
          />

          {q.type === "CHOICE" ? (
            <div style={{ marginTop: 10, display: "flex", flexDirection: "column", gap: 7 }}>
              {q.options.map((o, oi) => (
                <div key={o.key} style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <input
                    type="radio"
                    name={q.key}
                    checked={q.correctIndex === oi}
                    onChange={() => patchQuestion(q.key, { correctIndex: oi })}
                    title="标记为正确选项"
                  />
                  <input
                    className="inp"
                    placeholder={`选项 ${String.fromCharCode(65 + oi)}`}
                    value={o.label}
                    onChange={(e) => setOptionLabel(q.key, o.key, e.target.value)}
                  />
                  {q.options.length > 2 && (
                    <button
                      type="button"
                      className="btn btn-g sm"
                      onClick={() => removeOption(q.key, o.key)}
                    >
                      删
                    </button>
                  )}
                </div>
              ))}
              <div>
                <button type="button" className="btn btn-s sm" onClick={() => addOption(q.key)}>
                  + 加选项
                </button>
                <div className="hint">勾选圆圈即标记正确答案。</div>
              </div>
            </div>
          ) : (
            <div style={{ display: "flex", gap: 8, marginTop: 10 }}>
              <span
                className={`tag${q.judgeAnswer === true ? " on" : ""}`}
                style={{ cursor: "pointer" }}
                onClick={() => patchQuestion(q.key, { judgeAnswer: true })}
              >
                对
              </span>
              <span
                className={`tag${q.judgeAnswer === false ? " on" : ""}`}
                style={{ cursor: "pointer" }}
                onClick={() => patchQuestion(q.key, { judgeAnswer: false })}
              >
                错
              </span>
            </div>
          )}
        </div>
      ))}

      <button type="button" className="btn btn-g btn-block" onClick={() => setQuestions((qs) => [...qs, emptyQuestion()])}>
        + 加一道题
      </button>

      {(clientError || error) && (
        <div className="err">{clientError || error}</div>
      )}

      <button type="submit" className="btn btn-p btn-block" style={{ marginTop: 16 }} disabled={submitting}>
        {submitting ? "保存中…" : submitLabel}
      </button>
    </form>
  );
}
