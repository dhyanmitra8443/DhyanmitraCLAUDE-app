import type { MetadataRoute } from "next";

const BASE_URL = "https://dhyanmitra.in";

export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: "*",
      allow: "/",
      disallow: ["/admin", "/teach", "/learn", "/api"],
    },
    sitemap: `${BASE_URL}/sitemap.xml`,
  };
}
