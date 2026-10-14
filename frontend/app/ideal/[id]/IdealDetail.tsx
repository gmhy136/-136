"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import Calendar from "@/components/Calendar";
import {
  addIdealTask,
  checkInIdeal,
  deleteIdeal,
  getIdeal,
  getIdealCalendar,
  splitIdeal,
  toggleIdealTask,
} from "@/lib/api";
import { progressPct, shiftMonth, visibilityLabel } from "@/lib/ideal";

export default function IdealDetail({ id }: { id: number }) {
  const router = useRouter();
  const qc = useQueryClient();

  const [month, setMonth] = useState(() => {
    const n = new Date();
    return `${n.getFullYear()}-${String(n.getMonth() + 1).padStart(2, "0")}`;
  });
  const [newTask, setNewTask] = useState("");
  const [aiTasks, setAiTasks] = useState<string[]>([]);
  const [adopting, setAdopting] = useState(false);
  const [adoptErr, setAdoptErr] = useState<string | null>(null);

  const { data: detail, isPending, isError } = useQuery({
    queryKey: ["ideal", id],
    queryFn: () => getIdeal(id),
  });

  const { data: cal } = useQuery({
    queryKey: ["ideal-calendar", id, month],
    queryFn: () => getIdealCalendar(id, month),
  });

  const toggle = useMutation({
    mutationFn: (taskId: number) => toggleIdealTask(id, taskId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["ideal", id] }),
  });

  const add = useMutation({
    mutationFn: (content: string) => addIdealTask(id, content),
    onSuccess: () => {
      setNewTask("");
      qc.invalidateQueries({ queryKey: ["ideal", id] });
    },
  });

  const checkin = useMutation({
    mutationFn: () => checkInIdeal(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["ideal", id] });
      qc.invalidateQueries({ queryKey: ["ideal-calendar", id, month] });
    },
  });

  const del = useMutation({
    mutationFn: () => deleteIdeal(id),
    onSuccess: () => router.push("/ideal"),
  });

  const split = useMutation({
    mutationFn: () => splitIdeal(detail!.title, detail!.description),
    onSuccess: (d) => setAiTasks(d.tasks),
  });

  const handleAdopt = async () => {
    setAdopting(true);
    setAdoptErr(null);
    try {
      for (const t of aiTasks) {
        await addIdealTask(id, t);
      }
      setAiTasks([]);
      qc.invalidateQueries({ queryKey: ["ideal", id] });
    } catch (e) {
      setAdoptErr(e instanceof Error ? e.message : "采纳失败");
    } finally {
      setAdopting(false);
    }
  };

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        {isPending && (
          <div className="empty">
            <div className="t">加载中…</div>
          </div>
        )}

        {isError && (
          <div className="empty">
            <div className="t">理想不存在或已删除</div>
            <div className="s">它可能已经被移走了。</div>
          </div>
        )}

        {detail && (
          <>
            <div className="page-head">
              <div className="t">{detail.title}</div>
              <div className="s">
                {visibilityLabel(detail.visibility)} · 连续 {detail.streakDays} 天
                {detail.status === "DONE" && " · 已完成"}
              </div>
            </div>

            <div className="ideal">
              <div className="t">
                {detail.status === "DONE" ? "这个理想完成了" : "拆解进度"}
              </div>
              <div className="d">{detail.description || "没有描述，行动本身就是说明。"}</div>
              <div className="bar">
                <i
                  style={{
                    width: `${progressPct(detail.completedTaskCount, detail.taskCount)}%`,
                  }}
                />
              </div>
              <div className="meta">
                <span>
                  {detail.completedTaskCount}/{detail.taskCount} 步
                </span>
                <span>{progressPct(detail.completedTaskCount, detail.taskCount)}%</span>
              </div>
            </div>

            <div className="sect-t">拆解任务</div>
            <div className="row-list">
              {detail.tasks.length === 0 && (
                <div className="empty" style={{ padding: "22px 14px" }}>
                  <div className="t">还没有任务</div>
                  <div className="s">手动添加，或让 AI 帮你拆解。</div>
                </div>
              )}
              {detail.tasks.map((t, i) => (
                <div
                  key={t.id}
                  className="li"
                  style={{ cursor: "pointer" }}
                  onClick={() => toggle.mutate(t.id)}
                >
                  <span className="num">{String(i + 1).padStart(2, "0")}</span>
                  <div className="txt">
                    <div
                      className="t"
                      style={
                        t.completed
                          ? { textDecoration: "line-through", color: "var(--ink-faint)" }
                          : undefined
                      }
                    >
                      {t.content}
                    </div>
                  </div>
                  <span
                    className="check"
                    style={!t.completed ? { color: "var(--ink-faint)" } : undefined}
                  >
                    {t.completed ? "✓" : "·"}
                  </span>
                </div>
              ))}
            </div>

            <div className="code-row" style={{ marginTop: 10 }}>
              <input
                className="inp"
                placeholder="添加一个小步骤"
                value={newTask}
                onChange={(e) => setNewTask(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter" && newTask.trim()) add.mutate(newTask.trim());
                }}
              />
              <button
                className="btn btn-s sm"
                disabled={!newTask.trim() || add.isPending}
                onClick={() => add.mutate(newTask.trim())}
              >
                添加
              </button>
            </div>

            <button
              className="btn btn-p btn-block"
              style={{ marginTop: 16 }}
              disabled={detail.todayChecked || checkin.isPending}
              onClick={() => checkin.mutate()}
            >
              {checkin.isPending
                ? "打卡中…"
                : detail.todayChecked
                  ? "今日已打卡"
                  : "今日打卡"}
            </button>
            {checkin.error && (
              <div className="err" style={{ marginTop: 8 }}>
                {checkin.error.message}
              </div>
            )}

            <div className="sect-t">AI 拆解</div>
            <div className="ai-panel">
              <div className="ai-head">
                <span className="ai-tag">AI</span>
                <span className="t">帮我拆解成小步骤</span>
                <span className="cost">
                  {split.data ? `剩余 ${split.data.remaining} 次` : "温和进行，不催促"}
                </span>
              </div>

              {aiTasks.length === 0 ? (
                <div className="ai-body dim">
                  {split.isPending
                    ? "拆解中，稍等片刻…"
                    : "基于标题与描述，把它拆成一件件能打卡的小事。"}
                </div>
              ) : (
                <div className="row-list" style={{ marginTop: 10 }}>
                  {aiTasks.map((t, i) => (
                    <div key={i} className="li" style={{ marginBottom: 6 }}>
                      <span className="num">{String(i + 1).padStart(2, "0")}</span>
                      <div className="txt">
                        <div className="t">{t}</div>
                      </div>
                      <span
                        className="check"
                        style={{ cursor: "pointer", color: "var(--ink-faint)" }}
                        onClick={() => setAiTasks(aiTasks.filter((_, j) => j !== i))}
                      >
                        ✕
                      </span>
                    </div>
                  ))}
                </div>
              )}

              <div className="ai-actions">
                {aiTasks.length === 0 ? (
                  <button
                    className="btn btn-s sm"
                    disabled={split.isPending}
                    onClick={() => split.mutate()}
                  >
                    {split.isPending ? "拆解中…" : "开始拆解"}
                  </button>
                ) : (
                  <>
                    <button
                      className="btn btn-p sm"
                      disabled={adopting}
                      onClick={handleAdopt}
                    >
                      {adopting ? "采纳中…" : "全部采纳"}
                    </button>
                    <button className="btn btn-g sm" onClick={() => setAiTasks([])}>
                      放弃
                    </button>
                  </>
                )}
              </div>
              {split.error && (
                <div className="err" style={{ marginTop: 8 }}>
                  {split.error.message}
                </div>
              )}
              {adoptErr && (
                <div className="err" style={{ marginTop: 8 }}>
                  {adoptErr}
                </div>
              )}
            </div>

            <div className="sect-t">打卡日历</div>
            {cal && (
              <Calendar
                month={cal.month}
                days={cal.days}
                streakDays={cal.streakDays}
                monthCount={cal.monthCount}
                onPrev={() => setMonth(shiftMonth(month, -1))}
                onNext={() => setMonth(shiftMonth(month, 1))}
                onCheckIn={() => {
                  if (!detail.todayChecked && !checkin.isPending) checkin.mutate();
                }}
              />
            )}

            <button
              className="btn btn-g btn-block"
              style={{ marginTop: 16 }}
              disabled={del.isPending}
              onClick={() => {
                if (window.confirm("确定删除这个理想吗？任务与打卡会一并删除。")) del.mutate();
              }}
            >
              {del.isPending ? "删除中…" : "删除这个理想"}
            </button>
          </>
        )}
      </div>
    </div>
  );
}
