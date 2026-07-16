import type { Metadata } from "next";
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
    <html lang="en">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <Providers>
          {children}
          <Toaster richColors position="top-center" />
        </Providers>
      </body>
    </html>
  );
}
