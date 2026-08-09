import type { Metadata, Viewport } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { Toaster } from "@/components/ui/sonner";
import { Providers } from "./providers";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
};

export const metadata: Metadata = {
  metadataBase: new URL("https://dhyanmitra.in"),
  title: {
    default: "Dhyan Mitra | Online Yoga Courses, Live Classes & Certification",
    template: "%s | Dhyan Mitra",
  },
  description:
    "Dhyan Mitra (DYJK) offers online yoga courses, live instructor-led classes, and certification for students at every level.",
  openGraph: {
    siteName: "Dhyan Mitra",
    type: "website",
    url: "https://dhyanmitra.in",
    title: "Dhyan Mitra | Online Yoga Courses, Live Classes & Certification",
    description:
      "Dhyan Mitra (DYJK) offers online yoga courses, live instructor-led classes, and certification for students at every level.",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    // suppressHydrationWarning: the inline script below adds the `dark` class to
    // <html> before React hydrates, so the client markup intentionally differs
    // from what the server rendered.
    <html lang="en" suppressHydrationWarning>
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        {/*
          Applies the stored theme before first paint. Without this the page
          would render light and then snap to dark once React hydrated.

          It lives here as the first child of <body> rather than in a <head>
          block because the App Router owns <head> and drops inline scripts
          placed there.

          Only an explicit stored choice is honoured — the OS `prefers-color-scheme`
          is deliberately ignored, so visitors who never touched the toggle
          (including everyone signed out, who has no toggle) always get the
          light brand palette the marketing pages were designed around.
        */}
        <script
          dangerouslySetInnerHTML={{
            __html: `try{if(localStorage.getItem('dm-theme')==='dark'){document.documentElement.classList.add('dark')}}catch(e){}`,
          }}
        />
        <Providers>
          {children}
          <Toaster richColors position="top-center" />
        </Providers>
      </body>
    </html>
  );
}
