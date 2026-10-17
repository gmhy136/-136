"use client";

import { Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useMutation, useQuery } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import QuizForm from "@/components/QuizForm";
import { createQuiz, getQuiz, updateQuiz } from "@/lib/quiz";
import type { CreateQuizInput } from "@/lib/types";

export default function NewQuizPage() {
  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <Suspense
          fallback={
            <div className="empty">
              <div className="t">加载中…</div>
            </div>
          }
        >
          <QuizEditor />
        </Suspense>
      </div>
    </div>
  );
}

function QuizEditor() {
  const router = useRouter();
  const params = useSearchParams();
  const idParam = params.get("id");
  const editId = idParam ? Number(idParam) : null;

  const { data: initial } = useQuery({
    queryKey: ["quiz", editId],
    queryFn: () => getQuiz(editId!),
    enabled: editId != null,
  });

  const createMu = useMutation({
    mutationFn: createQuiz,
    onSuccess: (d) => router.push(`/quiz/${d.id}`),
  });
  const updateMu = useMutation({
    mutationFn: (input: CreateQuizInput) => updateQuiz(editId!, input),
    onSuccess: (d) => router.push(`/quiz/${d.id}`),
  });

  const submitting = createMu.isPending || updateMu.isPending;
  const error =
    createMu.error instanceof Error
      ? createMu.error.message
      : updateMu.error instanceof Error
        ? updateMu.error.message
        : null;

  const onSubmit = (input: CreateQuizInput) => {
    if (editId != null) updateMu.mutate(input);
    else createMu.mutate(input);
  };

  return (
    <>
      <div className="page-head">
        <div className="t">{editId != null ? "编辑题目" : "出一套题"}</div>
        <div className="s">选择题、判断题都可以。</div>
      </div>
      {editId != null && !initial ? (
        <div className="empty">
          <div className="t">加载中…</div>
        </div>
      ) : (
        <QuizForm
          initial={initial}
          submitLabel="保存"
          submitting={submitting}
          error={error}
          onSubmit={onSubmit}
        />
      )}
    </>
  );
}
