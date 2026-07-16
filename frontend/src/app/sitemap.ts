import type { MetadataRoute } from "next";
import { listCourses } from "@/lib/courses/queries";

const BASE_URL = "https://dhyanmitra.in";

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const staticRoutes: MetadataRoute.Sitemap = [
    { url: `${BASE_URL}/`, changeFrequency: "weekly", priority: 1 },
    { url: `${BASE_URL}/courses`, changeFrequency: "daily", priority: 0.9 },
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
