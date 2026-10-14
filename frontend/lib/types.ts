export interface User {
  id: number;
  email: string;
  nickname: string;
  avatarUrl: string | null;
  bio: string | null;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface Mood {
  id: number;
  name: string;
  weatherIcon: string;
}

/** 记录卡（对应后端 RecordVO）。 */
export interface RecordCard {
  id: number;
  content: string;
  moodId: number | null;
  moodName: string | null;
  moodIcon: string | null;
  styleKey: string | null;
  aiType: string | null;
  imageUrls: string[];
  createdAt: string;
  updatedAt: string;
}

export interface PageVO<T> {
  records: T[];
  total: number;
  page: number;
  size: number;
  hasMore: boolean;
}

export interface RecordInput {
  content: string;
  moodId: number | null;
  styleKey: string | null;
  aiType: string | null;
  imageUrls: string[];
}

/** AI 风格推荐结果。 */
export interface StyleRecommendation {
  styleKey: string;
  reason: string;
  remaining: number;
}

/** 理想卡（列表项）。 */
export interface IdealCard {
  id: number;
  title: string;
  description: string | null;
  visibility: "PRIVATE" | "PUBLIC";
  status: "ACTIVE" | "DONE";
  targetDate: string | null;
  taskCount: number;
  completedTaskCount: number;
}

/** 理想任务。 */
export interface IdealTask {
  id: number;
  content: string;
  completed: boolean;
  sortOrder: number;
}

/** 理想详情（含任务与打卡聚合）。 */
export interface IdealDetail extends IdealCard {
  tasks: IdealTask[];
  todayChecked: boolean;
  streakDays: number;
  monthCheckInDays: number;
  checkInDates: string[];
}

/** 打卡日历（按月份）。 */
export interface IdealCalendar {
  month: string;
  days: number[];
  streakDays: number;
  monthCount: number;
}

/** AI 理想拆解结果。 */
export interface SplitIdealResult {
  tasks: string[];
  remaining: number;
}

/** 创建理想入参。 */
export interface IdealInput {
  title: string;
  description: string | null;
  visibility: "PRIVATE" | "PUBLIC";
  targetDate: string | null;
}

/** 时间胶囊（列表项，锁定时不含内容）。 */
export interface CapsuleCard {
  id: number;
  title: string | null;
  openDate: string;
  isOpened: boolean;
  daysLeft: number;
  state: "LOCKED" | "READY" | "OPENED";
}

/** 时间胶囊详情（开启后 content 可见，锁定时为 null）。 */
export interface CapsuleDetail extends CapsuleCard {
  content: string | null;
  createdAt: string;
}

/** 写信入参。 */
export interface CapsuleInput {
  title: string | null;
  content: string;
  openDate: string;
}

/** 今日剩余 AI 次数（各场景取最小剩余）。 */
export interface AiRemaining {
  limit: number;
  remaining: number;
}

/** 个人主页聚合（对应后端 OverviewVO）。 */
export interface Overview {
  user: User;
  recordCount: number;
  recordDays: number;
  streakDays: number;
  monthCheckInCount: number;
  idealCount: number;
  idealDoneCount: number;
  capsuleCount: number;
  capsuleOpenedCount: number;
  recentRecords: RecordCard[];
  ideals: IdealCard[];
}
