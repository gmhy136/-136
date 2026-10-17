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

/** 心情天气月报（对应后端 MoodReportVO）。 */
export interface MoodReport {
  month: string;
  days: Record<number, string>;
  sunnyCount: number;
  cloudyCount: number;
  rainyCount: number;
  quote: string;
}

/** 成长时间线事件（对应后端 TimelineEventVO）。 */
export interface TimelineEvent {
  date: string;
  type: "RECORD" | "IDEAL_ACTIVE" | "IDEAL_START" | "IDEAL_DONE" | "CAPSULE_OPEN";
  label: string;
  detail: string;
}

/** 成就徽章（对应后端 BadgeVO）。 */
export interface Badge {
  key: string;
  seal: string;
  name: string;
  desc: string;
  unlocked: boolean;
}

/** AI 今日一句（对应后端 DailyQuoteVO）。 */
export interface DailyQuote {
  content: string;
}

/** 好友（对应后端 FriendVO）。 */
export interface Friend {
  id: number;
  nickname: string;
  avatarUrl: string | null;
  bio: string | null;
  since: string;
}

/** 好友申请（对应后端 FriendRequestVO）。 */
export interface FriendRequest {
  id: number;
  userId: number;
  nickname: string;
  avatarUrl: string | null;
  createdAt: string;
}

/** 好友申请汇总。 */
export interface FriendRequests {
  received: FriendRequest[];
  sent: FriendRequest[];
}

/** 邀请链接（对应后端 InviteVO）。 */
export interface Invite {
  token: string;
  url: string;
  expiresAt: string;
}

/** 问答列表项（对应后端 QuizVO）。 */
export interface QuizCard {
  id: number;
  title: string;
  description: string | null;
  status: "DRAFT" | "PUBLISHED";
  questionCount: number;
  publishedAt: string | null;
  createdAt: string;
}

/** 出题人视角选项（含是否正确）。 */
export interface QuizOption {
  id: number;
  label: string;
  isCorrect: boolean;
}

/** 出题人视角题目（含答案）。 */
export interface QuizQuestion {
  id: number;
  type: "CHOICE" | "JUDGE";
  content: string;
  judgeAnswer: boolean | null;
  options: QuizOption[];
}

/** 出题人视角详情（含答案）。 */
export interface QuizDetail {
  id: number;
  title: string;
  description: string | null;
  status: "DRAFT" | "PUBLISHED";
  publishedAt: string | null;
  createdAt: string;
  questions: QuizQuestion[];
}

/** 朋友圈题目（对应后端 FeedQuizVO）。 */
export interface FeedQuiz {
  id: number;
  title: string;
  description: string | null;
  questionCount: number;
  publishedAt: string;
  ownerId: number;
  ownerNickname: string;
  ownerAvatarUrl: string | null;
  answered: boolean;
}

/** 答题视角选项（不含答案）。 */
export interface PlayOption {
  id: number;
  label: string;
}

/** 答题视角题目（不含答案）。 */
export interface PlayQuestion {
  id: number;
  type: "CHOICE" | "JUDGE";
  content: string;
  options: PlayOption[];
}

/** 答题视角（对应后端 PlayQuizVO）。 */
export interface PlayQuiz {
  id: number;
  title: string;
  description: string | null;
  questionCount: number;
  questions: PlayQuestion[];
}

/** 单题答题结果。 */
export interface AnswerItemResult {
  questionId: number;
  isCorrect: boolean;
  correctOptionId: number | null;
  correctBool: boolean | null;
}

/** 答题结果（对应后端 AnswerResultVO）。 */
export interface AnswerResult {
  score: number;
  total: number;
  items: AnswerItemResult[];
}

/** 出题入参。 */
export interface OptionInput {
  label: string;
  isCorrect: boolean;
}

export interface QuestionInput {
  type: "CHOICE" | "JUDGE";
  content: string;
  options?: OptionInput[];
  judgeAnswer?: boolean | null;
}

export interface CreateQuizInput {
  title: string;
  description: string | null;
  questions: QuestionInput[];
}

/** 答题入参。 */
export interface AnswerInput {
  questionId: number;
  optionId?: number | null;
  chosenBool?: boolean | null;
}

export interface SubmitAnswerInput {
  anonymous: boolean;
  answers: AnswerInput[];
}
