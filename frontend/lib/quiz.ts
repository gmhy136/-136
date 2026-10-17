import { api } from "./api";
import type {
  AnswerResult,
  CreateQuizInput,
  FeedQuiz,
  PlayQuiz,
  QuizCard,
  QuizDetail,
  SubmitAnswerInput,
} from "./types";

export function listQuizzes(): Promise<QuizCard[]> {
  return api<QuizCard[]>("/quizzes");
}

export function listFeed(): Promise<FeedQuiz[]> {
  return api<FeedQuiz[]>("/quizzes/feed");
}

export function getQuiz(id: number): Promise<QuizDetail> {
  return api<QuizDetail>(`/quizzes/${id}`);
}

export function createQuiz(input: CreateQuizInput): Promise<QuizDetail> {
  return api<QuizDetail>("/quizzes", { method: "POST", body: JSON.stringify(input) });
}

export function updateQuiz(id: number, input: CreateQuizInput): Promise<QuizDetail> {
  return api<QuizDetail>(`/quizzes/${id}`, { method: "PUT", body: JSON.stringify(input) });
}

export function deleteQuiz(id: number): Promise<void> {
  return api<void>(`/quizzes/${id}`, { method: "DELETE" });
}

export function publishQuiz(id: number): Promise<QuizDetail> {
  return api<QuizDetail>(`/quizzes/${id}/publish`, { method: "POST" });
}

export function playQuiz(id: number): Promise<PlayQuiz> {
  return api<PlayQuiz>(`/quizzes/${id}/play`);
}

export function submitAnswer(id: number, input: SubmitAnswerInput): Promise<AnswerResult> {
  return api<AnswerResult>(`/quizzes/${id}/answers`, {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function getMyAnswer(id: number): Promise<AnswerResult> {
  return api<AnswerResult>(`/quizzes/${id}/my-answer`);
}
