import { clearTokens, getAccessToken, getRefreshToken, setTokens } from "./auth";
import type {
  AiRemaining,
  AuthResponse,
  Badge,
  CapsuleCard,
  CapsuleDetail,
  CapsuleInput,
  DailyQuote,
  IdealCalendar,
  IdealCard,
  IdealDetail,
  IdealInput,
  IdealTask,
  Mood,
  MoodReport,
  Overview,
  PageVO,
  RecordCard,
  RecordInput,
  SplitIdealResult,
  TimelineEvent,
  User,
} from "./types";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080/api/v1";

export class ApiError extends Error {
  code: number;
  constructor(code: number, message: string) {
    super(message);
    this.code = code;
  }
}

interface Result<T> {
  code: number;
  message: string;
  data: T;
}

async function request<T>(path: string, init: RequestInit, token: string | null): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body) headers.set("Content-Type", "application/json");
  if (token) headers.set("Authorization", `Bearer ${token}`);

  const res = await fetch(`${API_BASE}${path}`, { ...init, headers });
  const body = (await res.json().catch(() => null)) as Result<T> | null;
  if (!res.ok || !body || body.code !== 0) {
    throw new ApiError(body?.code ?? res.status, body?.message ?? "请求失败");
  }
  return body.data;
}

export async function refreshTokens(): Promise<boolean> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;
  try {
    const data = await request<AuthResponse>(
      "/auth/refresh",
      { method: "POST", body: JSON.stringify({ refreshToken }) },
      null
    );
    setTokens(data.accessToken, data.refreshToken, data.user);
    return true;
  } catch {
    clearTokens();
    return false;
  }
}

/** 统一请求入口：注入 Bearer、401 自动刷新重试一次。 */
export async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  try {
    return await request<T>(path, init, getAccessToken());
  } catch (e) {
    if (e instanceof ApiError && e.code === 401 && (await refreshTokens())) {
      return await request<T>(path, init, getAccessToken());
    }
    throw e;
  }
}

export function sendCode(email: string, purpose = "REGISTER"): Promise<void> {
  return api<void>("/auth/send-code", {
    method: "POST",
    body: JSON.stringify({ email, purpose }),
  });
}

export function login(email: string, password: string): Promise<AuthResponse> {
  return api<AuthResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function register(input: {
  email: string;
  code: string;
  nickname: string;
  password: string;
}): Promise<AuthResponse> {
  return api<AuthResponse>("/auth/register", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function getMe(): Promise<User> {
  return api<User>("/user/me");
}

export function updateMe(input: {
  nickname?: string;
  avatarUrl?: string;
  bio?: string;
}): Promise<User> {
  return api<User>("/user/me", { method: "PUT", body: JSON.stringify(input) });
}

export function getOverview(): Promise<Overview> {
  return api<Overview>("/user/me/overview");
}

export function getAiRemaining(): Promise<AiRemaining> {
  return api<AiRemaining>("/ai/remaining");
}

export function getDailyQuote(): Promise<DailyQuote> {
  return api<DailyQuote>("/ai/daily-quote");
}

export function getMoodReport(month?: string): Promise<MoodReport> {
  const q = month ? `?month=${month}` : "";
  return api<MoodReport>(`/reports/mood${q}`);
}

export function getTimeline(): Promise<TimelineEvent[]> {
  return api<TimelineEvent[]>("/reports/timeline");
}

export function getBadges(): Promise<Badge[]> {
  return api<Badge[]>("/badges");
}

export function listMoods(): Promise<Mood[]> {
  return api<Mood[]>("/moods");
}

export function listRecords(page: number, size = 10): Promise<PageVO<RecordCard>> {
  return api<PageVO<RecordCard>>(`/records?page=${page}&size=${size}`);
}

export function getRecord(id: number): Promise<RecordCard> {
  return api<RecordCard>(`/records/${id}`);
}

export function createRecord(input: RecordInput): Promise<RecordCard> {
  return api<RecordCard>("/records", { method: "POST", body: JSON.stringify(input) });
}

export function updateRecord(id: number, input: RecordInput): Promise<RecordCard> {
  return api<RecordCard>(`/records/${id}`, { method: "PUT", body: JSON.stringify(input) });
}

export function deleteRecord(id: number): Promise<void> {
  return api<void>(`/records/${id}`, { method: "DELETE" });
}

export function listIdeals(): Promise<IdealCard[]> {
  return api<IdealCard[]>("/ideals");
}

export function getIdeal(id: number): Promise<IdealDetail> {
  return api<IdealDetail>(`/ideals/${id}`);
}

export function createIdeal(input: IdealInput): Promise<IdealCard> {
  return api<IdealCard>("/ideals", { method: "POST", body: JSON.stringify(input) });
}

export function deleteIdeal(id: number): Promise<void> {
  return api<void>(`/ideals/${id}`, { method: "DELETE" });
}

export function addIdealTask(id: number, content: string): Promise<IdealTask> {
  return api<IdealTask>(`/ideals/${id}/tasks`, {
    method: "POST",
    body: JSON.stringify({ content }),
  });
}

export function toggleIdealTask(id: number, taskId: number): Promise<IdealTask> {
  return api<IdealTask>(`/ideals/${id}/tasks/${taskId}/toggle`, { method: "POST" });
}

export function checkInIdeal(id: number): Promise<IdealDetail> {
  return api<IdealDetail>(`/ideals/${id}/checkin`, { method: "POST" });
}

export function getIdealCalendar(id: number, month: string): Promise<IdealCalendar> {
  return api<IdealCalendar>(`/ideals/${id}/calendar?month=${month}`);
}

export function splitIdeal(title: string, description: string | null): Promise<SplitIdealResult> {
  return api<SplitIdealResult>("/ai/split-ideal", {
    method: "POST",
    body: JSON.stringify({ title, description }),
  });
}

export function listCapsules(): Promise<CapsuleCard[]> {
  return api<CapsuleCard[]>("/capsules");
}

export function getCapsule(id: number): Promise<CapsuleDetail> {
  return api<CapsuleDetail>(`/capsules/${id}`);
}

export function createCapsule(input: CapsuleInput): Promise<CapsuleCard> {
  return api<CapsuleCard>("/capsules", { method: "POST", body: JSON.stringify(input) });
}

export function deleteCapsule(id: number): Promise<void> {
  return api<void>(`/capsules/${id}`, { method: "DELETE" });
}

const API_ORIGIN = API_BASE.replace(/\/api\/v1\/?$/, "");

/** 把后端返回的相对路径（/uploads/...）拼成可访问的完整地址。 */
export function assetUrl(path: string | null | undefined): string {
  if (!path) return "";
  if (/^https?:\/\//.test(path)) return path;
  return API_ORIGIN + path;
}

/** 图片上传（FormData，不能复用 JSON 的 api()，避免 Content-Type 被写死）。 */
export async function uploadImage(file: File): Promise<{ url: string }> {
  const doUpload = (token: string | null) => {
    const form = new FormData();
    form.append("file", file);
    return fetch(`${API_BASE}/files/upload`, {
      method: "POST",
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
      body: form,
    });
  };

  let res = await doUpload(getAccessToken());
  let body = (await res.json().catch(() => null)) as Result<{ url: string }> | null;
  if (res.status === 401 && (await refreshTokens())) {
    res = await doUpload(getAccessToken());
    body = (await res.json().catch(() => null)) as Result<{ url: string }> | null;
  }
  if (!res.ok || !body || body.code !== 0) {
    throw new ApiError(body?.code ?? res.status, body?.message ?? "上传失败");
  }
  return body.data;
}
