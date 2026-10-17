"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { deleteQuiz, getQuiz, publishQuiz } from "@/lib/quiz";

export default function QuizDetailView({ id }: { id: number }) {
  const router = useRouter();
  const qc = useQueryClient();

  const { data: quiz, isPending, isError } = useQuery({
    queryKey: ["quiz", id],
    queryFn: () => getQuiz(id),
  });

  const publish = useMutation({
    mutationFn: () => publishQuiz(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["quiz", id] });
      qc.invalidateQueries({ queryKey: ["quizzes"] });
      qc.invalidateQueries({ queryKey: ["feed"] });
    },
  });
  const del = useMutation({
    mutationFn: () => deleteQuiz(id),
    onSuccess: () => router.push("/quiz"),
  });

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
            <div className="s">题目不存在或无权查看</div>
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
          <div className="s">{quiz.description || `共 ${quiz.questions.length} 题`}</div>
        </div>

        <div style={{ display: "flex", gap: 8, marginBottom: 16, flexWrap: "wrap" }}>
          {quiz.status === "DRAFT" ? (
            <>
              <button className="btn btn-p" onClick={() => publish.mutate()} disabled={publish.isPending}>
                {publish.isPending ? "发布中…" : "发布"}
              </button>
              <Link href={`/quiz/new?id=${quiz.id}`} className="btn btn-g">
                编辑
              </Link>
            </>
          ) : (
            <Link href="/feed" className="btn btn-g">
              去朋友圈
            </Link>
          )}
          <button
            className="btn btn-g"
            onClick={() => {
              if (confirm("确定删除？")) del.mutate();
            }}
            disabled={del.isPending}
          >
            删除
          </button>
        </div>

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
              <div style={{ display: "flex", flexDirection: "column", gap: 6 }}>
                {q.options.map((o) => (
                  <div key={o.id} style={{ fontSize: 12.5, display: "flex", gap: 8, alignItems: "center" }}>
                    <span
                      style={{
                        color: o.isCorrect ? "var(--mint)" : "var(--ink-faint)",
                        fontWeight: o.isCorrect ? 700 : 400,
                      }}
                    >
                      {o.isCorrect ? "✓" : "·"}
                    </span>
                    <span style={{ color: o.isCorrect ? "var(--ink)" : "var(--ink-soft)" }}>
                      {o.label}
                    </span>
                  </div>
                ))}
              </div>
            ) : (
              <div style={{ fontSize: 12.5 }}>
                答案：<b>{q.judgeAnswer ? "对" : "错"}</b>
              </div>
            )}
          </div>
        ))}

        <Link href="/quiz" className="btn btn-g btn-block" style={{ marginTop: 10 }}>
          返回我的题目
        </Link>
      </div>
    </div>
  );
}
