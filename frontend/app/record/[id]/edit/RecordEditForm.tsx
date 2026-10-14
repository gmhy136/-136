"use client";

import { useRouter } from "next/navigation";
import { useMutation, useQuery } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import RecordForm from "@/components/RecordForm";
import { deleteRecord, getRecord, updateRecord } from "@/lib/api";
import type { RecordInput } from "@/lib/types";

export default function RecordEditForm({ id }: { id: number }) {
  const router = useRouter();

  const { data: record, isPending, isError } = useQuery({
    queryKey: ["record", id],
    queryFn: () => getRecord(id),
  });

  const update = useMutation({
    mutationFn: (input: RecordInput) => updateRecord(id, input),
    onSuccess: () => router.push("/record"),
  });

  const del = useMutation({
    mutationFn: () => deleteRecord(id),
    onSuccess: () => router.push("/record"),
  });

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">编辑卡片</div>
          <div className="s">把它改得更贴近此刻。</div>
        </div>

        {isPending && (
          <div className="empty">
            <div className="t">加载中…</div>
          </div>
        )}

        {isError && (
          <div className="empty">
            <div className="t">记录不存在或已删除</div>
            <div className="s">它可能已经被移走了。</div>
          </div>
        )}

        {record && (
          <RecordForm
            initial={{
              content: record.content,
              moodId: record.moodId,
              imageUrls: record.imageUrls,
              styleKey: record.styleKey,
              aiType: record.aiType,
            }}
            submitLabel="保存修改"
            submitting={update.isPending}
            error={update.error instanceof Error ? update.error.message : null}
            onSubmit={(input) => update.mutate(input)}
            extraActions={
              <button
                type="button"
                className="btn btn-g"
                disabled={del.isPending}
                onClick={() => {
                  if (window.confirm("确定删除这张卡片吗？")) del.mutate();
                }}
              >
                {del.isPending ? "删除中…" : "删除"}
              </button>
            }
          />
        )}
      </div>
    </div>
  );
}
