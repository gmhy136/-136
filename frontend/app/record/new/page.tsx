"use client";

import { useRouter } from "next/navigation";
import { useMutation } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import RecordForm from "@/components/RecordForm";
import { createRecord } from "@/lib/api";

export default function NewRecordPage() {
  const router = useRouter();
  const mutation = useMutation({
    mutationFn: createRecord,
    onSuccess: () => router.push("/record"),
  });

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">写一张新卡</div>
          <div className="s">不用写得多好，写下来就好。</div>
        </div>
        <RecordForm
          submitLabel="保存卡片"
          submitting={mutation.isPending}
          error={mutation.error instanceof Error ? mutation.error.message : null}
          onSubmit={(input) => mutation.mutate(input)}
        />
      </div>
    </div>
  );
}
