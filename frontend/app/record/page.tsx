"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useInfiniteQuery } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import { assetUrl, listRecords } from "@/lib/api";
import { aiTypeLabel, formatTime, isAiType, styleOf } from "@/lib/record";
import type { PageVO, RecordCard } from "@/lib/types";

const PAGE_SIZE = 10;

export default function RecordListPage() {
  const router = useRouter();
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isPending, isError } =
    useInfiniteQuery({
      queryKey: ["records"],
      queryFn: ({ pageParam }) => listRecords(pageParam as number, PAGE_SIZE),
      initialPageParam: 1,
      getNextPageParam: (lastPage: PageVO<RecordCard>) =>
        lastPage.hasMore ? lastPage.page + 1 : undefined,
    });

  const records = data?.pages.flatMap((p) => p.records) ?? [];
  const total = data?.pages[0]?.total ?? 0;

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">全部记录</div>
          <div className="s">{total} 张卡片 · 按时间倒序</div>
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

        {!isPending && !isError && records.length === 0 && (
          <div className="empty">
            <div className="t">还没有记录</div>
            <div className="s">写下一张卡片，留住今天。</div>
          </div>
        )}

        {records.map((r) => {
          const st = styleOf(r.styleKey);
          return (
            <div
              key={r.id}
              className={`rec ${st.cls}`}
              style={{ cursor: "pointer" }}
              onClick={() => router.push(`/record/${r.id}/edit`)}
            >
              <div className="top">
                <div className="mood">
                  {r.moodIcon && <i className={`mdot ${r.moodIcon}`} />}
                  {r.moodName ?? "记录"}
                </div>
                <span className="time">{formatTime(r.createdAt)}</span>
              </div>
              <p>{r.content}</p>
              {r.imageUrls.length > 0 && (
                <div className="imgs">
                  {r.imageUrls.map((u) => (
                    <img
                      key={u}
                      src={assetUrl(u)}
                      alt=""
                      style={{ width: 44, height: 44, objectFit: "cover", borderRadius: 4 }}
                    />
                  ))}
                </div>
              )}
              <div className="foot">
                <span className={isAiType(r.aiType) ? "tag ai" : "tag"}>
                  {aiTypeLabel(r.aiType)}
                </span>
                <span className="time">{st.name}</span>
              </div>
            </div>
          );
        })}

        {hasNextPage && (
          <div className="row-list" style={{ marginTop: 6 }}>
            <div
              className="li"
              style={{ cursor: "pointer" }}
              onClick={() => fetchNextPage()}
            >
              <div className="txt">
                <div className="t">{isFetchingNextPage ? "加载中…" : "更早的记录"}</div>
                <div className="s">点击加载更多</div>
              </div>
              <span className="tag ghost">加载更多</span>
            </div>
          </div>
        )}

        <Link href="/record/new" className="btn btn-p btn-block" style={{ marginTop: 10 }}>
          写一张新卡
        </Link>
      </div>
    </div>
  );
}
