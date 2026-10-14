"use client";

import { useRouter } from "next/navigation";
import { useMutation } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import IdealForm from "@/components/IdealForm";
import { createIdeal } from "@/lib/api";

export default function NewIdealPage() {
  const router = useRouter();
  const mutation = useMutation({
    mutationFn: createIdeal,
    onSuccess: (data) => router.push(`/ideal/${data.id}`),
  });

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">种下一个理想</div>
          <div className="s">它不必很大，愿意开始就很好。</div>
        </div>
        <IdealForm
          submitLabel="种下理想"
          submitting={mutation.isPending}
          error={mutation.error instanceof Error ? mutation.error.message : null}
          onSubmit={(input) => mutation.mutate(input)}
        />
      </div>
    </div>
  );
}
