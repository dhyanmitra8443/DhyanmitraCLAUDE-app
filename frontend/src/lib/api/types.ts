import type { components } from "./schema";

/**
 * Convenience aliases over the types generated from the backend's
 * openapi.yaml (schema.d.ts is generated - never edit it by hand; run
 * `npm run generate:api` after the contract changes).
 *
 * Importing from here rather than reaching into `components["schemas"]`
 * everywhere means a contract change surfaces as a type error at the point
 * of use, which is the whole reason for generating these.
 */
type Schemas = components["schemas"];

export type UserSummary = Schemas["UserSummary"];
export type UserProfile = Schemas["UserProfile"];
export type UserFullProfile = Schemas["UserFullProfile"];
export type UpdateOwnProfileRequest = Schemas["UpdateOwnProfileRequest"];
export type CourseSummary = Schemas["CourseSummary"];
export type CourseDetail = Schemas["CourseDetail"];
export type CreateCourseRequest = Schemas["CreateCourseRequest"];
export type CategorySummary = Schemas["CategorySummary"];
export type CreateCategoryRequest = Schemas["CreateCategoryRequest"];
export type SectionDetail = Schemas["SectionDetail"];
export type CreateSectionRequest = Schemas["CreateSectionRequest"];
export type LessonSummary = Schemas["LessonSummary"];
export type LessonDetail = Schemas["LessonDetail"];
export type CreateLessonRequest = Schemas["CreateLessonRequest"];
export type LessonResourceSummary = Schemas["LessonResourceSummary"];
export type CreateLessonResourceRequest = Schemas["CreateLessonResourceRequest"];
export type SubscriptionPlanSummary = Schemas["SubscriptionPlanSummary"];
export type CreateSubscriptionPlanRequest = Schemas["CreateSubscriptionPlanRequest"];
export type SubscriptionSummary = Schemas["SubscriptionSummary"];
export type SubscriptionStatus = "ACTIVE" | "EXPIRED" | "CANCELLED" | "PENDING";
export type OrderSummary = Schemas["OrderSummary"];
export type PaymentSummary = Schemas["PaymentSummary"];
export type OrderStatus = "PENDING" | "PAID" | "FAILED" | "CANCELLED";
export type PaymentStatus = "PENDING" | "SUCCESS" | "FAILED" | "CANCELLED";
export type PaymentMethod = "CREDIT_CARD" | "DEBIT_CARD" | "UPI" | "NET_BANKING" | "WALLET";
export type GatewayEnvironment = "SANDBOX" | "PRODUCTION";
export type PaymentGatewaySettingsView = Schemas["PaymentGatewaySettingsView"];
export type PaymentGatewaySettings = Schemas["PaymentGatewaySettings"];

/** Ref: SRS 10.3, 10.4 - POST /orders body. No named schema in openapi.yaml (inlined). */
export interface CreateOrderRequest {
  courseId: string;
  subscriptionPlanId: string;
}

/** Ref: SRS 10.12 - fields needed to launch Razorpay Checkout. No named schema in openapi.yaml (inlined). */
export interface RazorpayOrderResponse {
  razorpayOrderId: string;
  razorpayKeyId: string;
  /** Amount in the smallest currency unit (paise). */
  amount: number;
  currency: string;
}
export type CourseProgressSummary = Schemas["CourseProgressSummary"];
export type CertificateSummary = Schemas["CertificateSummary"];

/** Ref: SRS 12.12, 17.24 - GET /certificates/{id}/download response. No named schema in openapi.yaml (inlined). */
export interface CertificateDownloadUrlResponse {
  downloadUrl: string;
  expiresInSeconds: number;
}

/** Ref: SRS 12.13, 12.17 - GET /certificates/verify/{id} response. No named schema in openapi.yaml (inlined). */
export interface CertificateVerifyResponse {
  valid: boolean;
  studentName: string;
  courseName: string;
  completionDate: string;
  issueDate: string;
  certificateNumber: string;
}
export type LiveClassSummary = Schemas["LiveClassSummary"];
export type CreateLiveClassRequest = Schemas["CreateLiveClassRequest"];
export type LiveClassStatus = "SCHEDULED" | "CANCELLED" | "COMPLETED";

/** Ref: SRS 11.8-11.10 - POST /live-classes/{id}/join response. No named schema in openapi.yaml (inlined). */
export interface JoinLiveClassResponse {
  meetingUrl: string;
  meetingPassword?: string | null;
}
export type StudentDashboard = Schemas["StudentDashboard"];
export type AdminDashboard = Schemas["AdminDashboard"];
export type InstructorDashboard = Schemas["InstructorDashboard"];
export type RecentActivity = Schemas["RecentActivity"];
export type PageMeta = Schemas["PageMeta"];

/** Ref: SRS 3.13 - the three roles, exactly as the backend spells them. */
export type UserRole = "ADMINISTRATOR" | "INSTRUCTOR" | "STUDENT";

/** The `{ success, message, data }` envelope every backend endpoint returns (Ref: SRS 2.8). */
export interface ApiEnvelope<T> {
  success: boolean;
  message: string;
  data: T;
}

/** Paginated list payload: `data.content` + `data.page` (Ref: openapi.yaml pagination convention). */
export interface Paginated<T> {
  content: T[];
  page: PageMeta;
}

export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: UserSummary;
}
