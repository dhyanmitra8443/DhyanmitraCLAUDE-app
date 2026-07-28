import type { MetadataRoute } from "next";
import { listCourses } from "@/lib/courses/queries";

const BASE_URL = "https://dhyanmitra.in";

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const staticRoutes: MetadataRoute.Sitemap = [
    { url: `${BASE_URL}/`, changeFrequency: "weekly", priority: 1 },
    { url: `${BASE_URL}/about`, changeFrequency: "monthly", priority: 0.7 },
    { url: `${BASE_URL}/courses`, changeFrequency: "daily", priority: 0.9 },
    { url: `${BASE_URL}/programs`, changeFrequency: "monthly", priority: 0.7 },
    { url: `${BASE_URL}/events`, changeFrequency: "weekly", priority: 0.6 },
    { url: `${BASE_URL}/gallery`, changeFrequency: "monthly", priority: 0.5 },
    { url: `${BASE_URL}/blog`, changeFrequency: "monthly", priority: 0.4 },
    { url: `${BASE_URL}/contact`, changeFrequency: "yearly", priority: 0.5 },
    { url: `${BASE_URL}/sign-in`, changeFrequency: "yearly", priority: 0.2 },
    { url: `${BASE_URL}/register`, changeFrequency: "yearly", priority: 0.2 },
  ];

  // Published courses only - matches the public catalog's own forced filter (SRS 5.9).
  const { content } = await listCourses({ size: 100, status: "PUBLISHED", sort: "publishedAt,desc" });
  const courseRoutes: MetadataRoute.Sitemap = content.map((course) => ({
    url: `${BASE_URL}/courses/${course.id}`,
    changeFrequency: "weekly",
    priority: 0.7,
  }));

  return [...staticRoutes, ...courseRoutes];
}
