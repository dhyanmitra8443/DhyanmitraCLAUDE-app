import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Ref: SRS 18.9 - produces a self-contained server bundle (no full
  // node_modules needed at runtime), which is what frontend/Dockerfile's
  // runtime stage copies into the production image.
  output: "standalone",
};

export default nextConfig;
