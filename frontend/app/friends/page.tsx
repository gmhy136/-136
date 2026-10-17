"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import TopNav from "@/components/TopNav";
import {
  acceptFriendRequest,
  createInvite,
  listFriendRequests,
  listFriends,
  rejectFriendRequest,
  removeFriend,
  searchUserByEmail,
  sendFriendRequest,
} from "@/lib/friend";
import type { Invite, User } from "@/lib/types";

function avatar(nickname: string) {
  return (
    <span
      style={{
        width: 28,
        height: 28,
        flex: "none",
        borderRadius: 8,
        background: "var(--cyan-tint)",
        border: "1px solid var(--cyan-line)",
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        fontFamily: "var(--font-serif)",
        fontSize: 14,
        color: "var(--ink)",
      }}
    >
      {nickname.slice(0, 1) || "?"}
    </span>
  );
}

export default function FriendsPage() {
  const qc = useQueryClient();
  const { data: friends = [] } = useQuery({ queryKey: ["friends"], queryFn: listFriends });
  const { data: requests } = useQuery({
    queryKey: ["friend-requests"],
    queryFn: listFriendRequests,
  });

  const [email, setEmail] = useState("");
  const [searching, setSearching] = useState(false);
  const [searched, setSearched] = useState<User | null>(null);
  const [searchError, setSearchError] = useState("");
  const [notice, setNotice] = useState("");

  const [invite, setInvite] = useState<Invite | null>(null);
  const [inviteError, setInviteError] = useState("");

  const refresh = () => {
    qc.invalidateQueries({ queryKey: ["friends"] });
    qc.invalidateQueries({ queryKey: ["friend-requests"] });
  };

  const accept = useMutation({ mutationFn: acceptFriendRequest, onSuccess: refresh });
  const reject = useMutation({ mutationFn: rejectFriendRequest, onSuccess: refresh });
  const remove = useMutation({ mutationFn: removeFriend, onSuccess: refresh });
  const send = useMutation({
    mutationFn: sendFriendRequest,
    onSuccess: () => {
      setNotice("已发送申请");
      setSearched(null);
      setEmail("");
      refresh();
    },
  });
  const inviteMu = useMutation({
    mutationFn: createInvite,
    onSuccess: (inv) => {
      setInvite(inv);
      setInviteError("");
    },
    onError: (e) => setInviteError(e instanceof Error ? e.message : "生成失败"),
  });

  const doSearch = async () => {
    setSearchError("");
    setSearched(null);
    if (!email.trim()) {
      setSearchError("请输入邮箱");
      return;
    }
    setSearching(true);
    try {
      setSearched(await searchUserByEmail(email.trim()));
    } catch (e) {
      setSearchError(e instanceof Error ? e.message : "搜索失败");
    } finally {
      setSearching(false);
    }
  };

  const copyInvite = () => {
    if (!invite) return;
    navigator.clipboard?.writeText(`${window.location.origin}${invite.url}`);
    setNotice("邀请链接已复制");
  };

  return (
    <div className="app">
      <TopNav />
      <div className="content">
        <div className="page-head">
          <div className="t">朋友</div>
          <div className="s">和认识的人一起答题。</div>
        </div>

        {notice && <div className="notice">{notice}</div>}

        <div className="sect-t">添加好友</div>
        <div style={{ display: "flex", gap: 8 }}>
          <input
            className="inp"
            placeholder="输入对方邮箱搜索"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && doSearch()}
          />
          <button className="btn btn-p" onClick={doSearch} disabled={searching}>
            {searching ? "搜索中…" : "搜索"}
          </button>
        </div>
        {searchError && <div className="err">{searchError}</div>}
        {searched && (
          <div className="row-list" style={{ marginTop: 10 }}>
            <div className="li">
              {avatar(searched.nickname)}
              <div className="txt">
                <div className="t">{searched.nickname}</div>
                <div className="s">{searched.email}</div>
              </div>
              <button
                className="btn btn-s sm"
                onClick={() => send.mutate(searched.email)}
                disabled={send.isPending}
              >
                {send.isPending ? "发送中…" : "加为好友"}
              </button>
            </div>
          </div>
        )}

        <div style={{ marginTop: 10, display: "flex", gap: 8, alignItems: "center", flexWrap: "wrap" }}>
          <button className="btn btn-g" onClick={() => inviteMu.mutate()} disabled={inviteMu.isPending}>
            {inviteMu.isPending ? "生成中…" : "生成邀请链接"}
          </button>
          {invite && (
            <>
              <span
                className="tag ghost"
                style={{ maxWidth: 240, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}
              >
                {`${window.location.origin}${invite.url}`}
              </span>
              <button className="btn btn-s sm" onClick={copyInvite}>
                复制
              </button>
            </>
          )}
        </div>
        {inviteError && <div className="err">{inviteError}</div>}

        {requests && (requests.received.length > 0 || requests.sent.length > 0) && (
          <>
            <div className="sect-t">好友申请</div>
            {requests.received.map((r) => (
              <div className="row-list" key={`r${r.id}`}>
                <div className="li">
                  {avatar(r.nickname)}
                  <div className="txt">
                    <div className="t">{r.nickname}</div>
                    <div className="s">请求加你为好友</div>
                  </div>
                  <button className="btn btn-p sm" onClick={() => accept.mutate(r.id)} disabled={accept.isPending}>
                    接受
                  </button>
                  <button className="btn btn-g sm" onClick={() => reject.mutate(r.id)} disabled={reject.isPending}>
                    拒绝
                  </button>
                </div>
              </div>
            ))}
            {requests.sent.map((r) => (
              <div className="row-list" key={`s${r.id}`}>
                <div className="li">
                  {avatar(r.nickname)}
                  <div className="txt">
                    <div className="t">{r.nickname}</div>
                    <div className="s">等待对方通过</div>
                  </div>
                  <span className="tag ghost">待通过</span>
                </div>
              </div>
            ))}
          </>
        )}

        <div className="sect-t">好友（{friends.length}）</div>
        {friends.length === 0 ? (
          <div className="empty">
            <div className="t">还没有好友</div>
            <div className="s">用邮箱搜索，或生成邀请链接发给朋友。</div>
          </div>
        ) : (
          <div className="row-list">
            {friends.map((f) => (
              <div className="li" key={f.id}>
                {avatar(f.nickname)}
                <div className="txt">
                  <div className="t">{f.nickname}</div>
                  <div className="s">{f.bio || "这个朋友很神秘"}</div>
                </div>
                <button className="btn btn-g sm" onClick={() => remove.mutate(f.id)} disabled={remove.isPending}>
                  删除
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
