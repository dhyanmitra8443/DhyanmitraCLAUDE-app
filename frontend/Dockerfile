# Ref: SRS 2.13, 18.9 - Docker image for the Next.js frontend, built via
# Docker Compose as part of the deployment strategy. Mirrors the pattern in
# backend/Dockerfile (multi-stage, non-root runtime user).

# ---- Dependencies stage --------------------------------------------------
FROM node:22-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

# ---- Build stage ----------------------------------------------------------
FROM node:22-alpine AS build
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
# BACKEND_URL isn't needed at build time (server.ts reads it at request time),
# but Next.js still evaluates route/page modules during the build - give it
# a harmless placeholder so that doesn't fail if anything touches it eagerly.
ENV BACKEND_URL=http://backend:8080
RUN npm run build

# ---- Runtime stage ---------------------------------------------------------
FROM node:22-alpine AS runtime
WORKDIR /app
RUN addgroup -S nextjs && adduser -S nextjs -G nextjs
ENV NODE_ENV=production
# next.config.ts sets output: "standalone" - this copies just the server
# bundle it produces (not the full node_modules) plus the static assets it
# doesn't inline into that bundle.
COPY --from=build --chown=nextjs:nextjs /app/.next/standalone ./
COPY --from=build --chown=nextjs:nextjs /app/.next/static ./.next/static
COPY --from=build --chown=nextjs:nextjs /app/public ./public
USER nextjs
EXPOSE 3000
ENV PORT=3000 HOSTNAME=0.0.0.0
ENTRYPOINT ["node", "server.js"]
