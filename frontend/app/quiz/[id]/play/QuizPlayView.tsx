"use client";

import Link from "next/link";
import { useState } from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { getMyAnswer, playQuiz, submitAnswer } from "@/lib/quiz";
import type { AnswerResult, PlayQuiz, SubmitAnswerInput } from "@/lib/types";

export default function QuizPlayView({ id }: { id: number }) {
  const { data: quiz, isPending, isError } = useQuery({
    queryKey: ["play", id],
    queryFn: () => playQuiz(id),
  });
  const myAnswer = useQuery({
    queryKey: ["my-answer", id],
    queryFn: () => getMyAnswer(id),
    retry: false,
  });

  const [submitted, setSubmitted] = useState<AnswerResult | null>(null);
  const [anonymous, setAnonymous] = useState(false);
  const [answers, setAnswers] = useState<Record<number, { optionId?: number; chosenBool?: boolean }>>({});
  const [clientError, setClientError] = useState("");

  const submitMu = useMutation({
    mutationFn: (input: SubmitAnswerInput) => submitAnswer(id, input),
    onSuccess: (r) => setSubmitted(r),
  });

  const result = submitted ?? (myAnswer.data ?? null);

  const handleSubmit = () => {
    if (!quiz) return;
    for (const q of quiz.questions) {
      const a = answers[q.id];
      const missing = q.type === "CHOICE" ? a?.optionId == null : a?.chosenBool == null;
      if (missing) {
        setClientError("请完成所有题目");
        return;
      }
    }
    setClientError("");
    const input: SubmitAnswerInput = {
      anonymous,
      answers: quiz.questions.map((q) => {
        const a = answers[q.id];
        return q.type === "CHOICE"
          ? { questionId: q.id, optionId: a.optionId }
          : { questionId: q.id, chosenBool: a.chosenBool };
      }),
    };
    submitMu.mutate(input);
  };

  if (isPending) {
    return (
      <div className="app">
        <TopNav />
        <div className="content">
          <div className="empty">
            <div className="t">加载中…</div>
          </div>
        </div>
      </div>
    );
  }
  if (isError || !quiz) {
    return (
      <div className="app">
        <TopNav />
        <div className="content">
          <div className="empty">
            <div className="t">加载失败</div>
            <div className="s">题目不存在或仅好友可答</div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">{quiz.title}</div>
          <div className="s">{quiz.description || `共 ${quiz.questionCount} 题`}</div>
        </div>

        {result ? (
          <ResultView result={result} quiz={quiz} />
        ) : (
          <>
            {quiz.questions.map((q, i) => (
              <div key={q.id} className="rec">
                <div className="top">
                  <span className="mood">
                    <span className="tag">{q.type === "CHOICE" ? "选择题" : "判断题"}</span>
                  </span>
                  <span className="time">第 {i + 1} 题</span>
                </div>
                <p style={{ fontFamily: "var(--font-serif)", fontWeight: 600 }}>{q.content}</p>
                {q.type === "CHOICE" ? (
                  <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                    {q.options.map((o) => {
                      const selected = answers[q.id]?.optionId === o.id;
                      return (
                        <div
                          key={o.id}
                          className="row-list"
                          style={{ cursor: "pointer" }}
                          onClick={() => setAnswers((prev) => ({ ...prev, [q.id]: { optionId: o.id } }))}
                        >
                          <div
                            className="li"
                            style={{
                              background: selected ? "var(--cyan-tint)" : undefined,
                              borderColor: selected ? "var(--cyan-line)" : undefined,
                              marginBottom: 0,
                            }}
                          >
                            <div className="txt">
                              <div className="t">{o.label}</div>
                            </div>
                            <span className="check">{selected ? "✓" : ""}</span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <div style={{ display: "flex", gap: 8 }}>
                    {[true, false].map((b) => {
                      const selected = answers[q.id]?.chosenBool === b;
                      return (
                        <span
                          key={String(b)}
                          className={`tag${selected ? " on" : ""}`}
                          style={{ cursor: "pointer", fontSize: 12, padding: "6px 16px" }}
                          onClick={() => setAnswers((prev) => ({ ...prev, [q.id]: { chosenBool: b } }))}
                        >
                          {b ? "对" : "错"}
                        </span>
                      );
                    })}
                  </div>
                )}
              </div>
            ))}

            <div className="set-row" style={{ padding: "10px 0" }}>
              <div className="txt">
                <div className="t">匿名作答</div>
                <div className="s">你的昵称不会展示给他人</div>
              </div>
              <div className={`switch${anonymous ? " on" : ""}`} onClick={() => setAnonymous((v) => !v)} />
            </div>

            {clientError && <div className="err">{clientError}</div>}
            {submitMu.error instanceof Error && <div className="err">{submitMu.error.message}</div>}

            <button
              className="btn btn-p btn-block"
              style={{ marginTop: 14 }}
              onClick={handleSubmit}
              disabled={submitMu.isPending}
            >
              {submitMu.isPending ? "提交中…" : "提交答案"}
            </button>
          </>
        )}

        <Link href="/feed" className="btn btn-g btn-block" style={{ marginTop: 12 }}>
          返回朋友圈
        </Link>
      </div>
    </div>
  );
}

function ResultView({ result, quiz }: { result: AnswerResult; quiz: PlayQuiz }) {
  const questionById = new Map(quiz.questions.map((q) => [q.id, q]));
  return (
    <>
      <div className="env opened" style={{ textAlign: "center" }}>
        <div className="t">
          得分 {result.score} / {result.total}
        </div>
        <div className="s">{result.score === result.total ? "全对，厉害！" : "再接再厉"}</div>
      </div>
      {result.items.map((it) => {
        const q = questionById.get(it.questionId);
        if (!q) return null;
        const correctText =
          q.type === "CHOICE"
            ? (q.options.find((o) => o.id === it.correctOptionId)?.label ?? "")
            : it.correctBool
              ? "对"
              : "错";
        return (
          <div key={it.questionId} className="rec">
            <div className="top">
              <span className={`tag ${it.isCorrect ? "on" : "ghost"}`}>
                {it.isCorrect ? "答对" : "答错"}
              </span>
            </div>
            <p style={{ fontFamily: "var(--font-serif)", fontWeight: 600 }}>{q.content}</p>
            <div className="hint">正确答案：{correctText}</div>
          </div>
        );
      })}
    </>
  );
}
