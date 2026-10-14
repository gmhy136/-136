"use client";

import { create } from "zustand";
import { clearTokens, getStoredUser } from "@/lib/auth";
import type { User } from "@/lib/types";

interface AuthState {
  user: User | null;
  setAuth: (user: User) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: getStoredUser(),
  setAuth: (user) => set({ user }),
  logout: () => {
    clearTokens();
    set({ user: null });
  },
}));
