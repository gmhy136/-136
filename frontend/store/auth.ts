"use client";

import { create } from "zustand";
import { clearTokens, getStoredUser } from "@/lib/auth";
import type { User } from "@/lib/types";

interface AuthState {
  user: User | null;
  setAuth: (user: User) => void;
  hydrate: () => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  // SSR 与客户端首帧都渲染为未登录，挂载后再从 localStorage 恢复，
  // 避免服务端 HTML 与客户端注水时的用户信息不一致。
  user: null,
  setAuth: (user) => set({ user }),
  hydrate: () => set({ user: getStoredUser() }),
  logout: () => {
    clearTokens();
    set({ user: null });
  },
}));
