"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { deleteQuiz, listQuizzes, publishQuiz } from "@/lib/quiz";
import { formatTime } from "@/lib/record";

export default function QuizListPage() {
  const router = useRouter();
  const qc = useQueryClient();
  const { data: quizzes = [], isPending, isError } = useQuery({
    queryKey: ["quizzes"],
    queryFn: listQuizzes,
  });

  const publish = useMutation({
    mutationFn: publishQuiz,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["quizzes"] });
      qc.invalidateQueries({ queryKey: ["feed"] });
    },
  });
  const del = useMutation({
    mutationFn: deleteQuiz,
    onSuccess: () => qc.invalidateQueries({ queryKey: ["quizzes"] }),
  });

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">我的题目</div>
          <div className="s">出题、发布，分享给朋友。</div>
        </div>

        <div style={{ display: "flex", gap: 8, marginBottom: 16 }}>
          <Link href="/quiz/new" className="btn btn-p">
            出题
          </Link>
          <Link href="/feed" className="btn btn-g">
            去朋友圈答题
          </Link>
        </div>

        {isPending && (
          <div className="empty">
            <div className="t">加载中…</div>
          </div>
        )}
        {isError && (
          <div className="empty">
            <div className="t">加载失败</div>
            <div className="s">请确认后端已启动</div>
          </div>
        )}
        {!isPending && !isError && quizzes.length === 0 && (
          <div className="empty">
            <div className="t">还没有题目</div>
            <div className="s">出一套选择题或判断题吧。</div>
          </div>
        )}

        {quizzes.map((q) => (
          <div
            key={q.id}
            className="rec"
            style={{ cursor: "pointer" }}
            onClick={() => router.push(`/quiz/${q.id}`)}
          >
            <div className="top">
              <span className="mood">
                <span className={`tag ${q.status === "PUBLISHED" ? "on" : "ghost"}`}>
                  {q.status === "PUBLISHED" ? "已发布" : "草稿"}
                </span>
              </span>
              <span className="time">{formatTime(q.publishedAt ?? q.createdAt)}</span>
            </div>
            <p style={{ fontFamily: "var(--font-serif)", fontWeight: 600, marginBottom: 4 }}>
              {q.title}
            </p>
            {q.description && (
              <p style={{ color: "var(--ink-soft)", fontSize: 12.5, marginBottom: 8 }}>
                {q.description}
              </p>
            )}
            <div className="foot">
              <span className="tag">{q.questionCount} 题</span>
              <span style={{ display: "flex", gap: 6 }}>
                {q.status === "DRAFT" && (
                  <button
                    className="btn btn-s sm"
                    onClick={(e) => {
                      e.stopPropagation();
                      publish.mutate(q.id);
                    }}
                    disabled={publish.isPending}
                  >
                    发布
                  </button>
                )}
                <button
                  className="btn btn-g sm"
                  onClick={(e) => {
                    e.stopPropagation();
                    if (confirm("确定删除？")) del.mutate(q.id);
                  }}
                  disabled={del.isPending}
                >
                  删除
                </button>
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
