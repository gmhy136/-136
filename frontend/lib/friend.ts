import { api } from "./api";
import type { Friend, FriendRequest, FriendRequests, Invite, User } from "./types";

export function listFriends(): Promise<Friend[]> {
  return api<Friend[]>("/friends");
}

export function listFriendRequests(): Promise<FriendRequests> {
  return api<FriendRequests>("/friends/requests");
}

export function searchUserByEmail(email: string): Promise<User> {
  return api<User>(`/friends/search?email=${encodeURIComponent(email)}`);
}

export function sendFriendRequest(email: string): Promise<FriendRequest> {
  return api<FriendRequest>("/friends/requests", {
    method: "POST",
    body: JSON.stringify({ email }),
  });
}

export function acceptFriendRequest(id: number): Promise<Friend> {
  return api<Friend>(`/friends/requests/${id}/accept`, { method: "POST" });
}

export function rejectFriendRequest(id: number): Promise<void> {
  return api<void>(`/friends/requests/${id}/reject`, { method: "POST" });
}

export function removeFriend(id: number): Promise<void> {
  return api<void>(`/friends/${id}`, { method: "DELETE" });
}

export function createInvite(): Promise<Invite> {
  return api<Invite>("/friends/invites", { method: "POST" });
}

export function acceptInvite(token: string): Promise<void> {
  return api<void>(`/friends/invites/${token}`, { method: "POST" });
}
