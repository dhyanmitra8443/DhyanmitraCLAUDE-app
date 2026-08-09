"use client";

import { useEffect, useState } from "react";
import { Moon, Sun } from "lucide-react";

export const THEME_STORAGE_KEY = "dm-theme";

/**
 * Dark-mode toggle, rendered in AppShell so it only exists for signed-in users.
 *
 * The `dark` class goes on <html> rather than on the shell wrapper because
 * dropdowns, popovers and toasts render through portals attached to <body> —
 * a class on the shell would leave every one of them stranded in light mode.
 *
 * Initial state is read in an inline script in the root layout (see
 * layout.tsx), which runs before first paint. This component therefore starts
 * by reading the class the script already applied instead of holding its own
 * default, which is what keeps the button icon from flipping on hydration.
 */
export function ThemeToggle() {
  const [isDark, setIsDark] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setIsDark(document.documentElement.classList.contains("dark"));
    setMounted(true);
  }, []);

  function toggle() {
    const next = !isDark;
    setIsDark(next);
    document.documentElement.classList.toggle("dark", next);
    try {
      localStorage.setItem(THEME_STORAGE_KEY, next ? "dark" : "light");
    } catch {
      // Private-browsing modes can throw on write; the toggle should still work
      // for the rest of the session even when the choice cannot be persisted.
    }
  }

  return (
    <button
      type="button"
      onClick={toggle}
      // Until mounted we cannot know the real theme, so the label is kept
      // generic rather than announcing a state that may be wrong.
      aria-label={mounted ? (isDark ? "Switch to light mode" : "Switch to dark mode") : "Toggle dark mode"}
      aria-pressed={mounted ? isDark : undefined}
      title={mounted && isDark ? "Light mode" : "Dark mode"}
      className="text-muted-foreground hover:text-foreground hover:bg-muted focus-visible:ring-ring/50 rounded-full p-2 transition-colors outline-none focus-visible:ring-3"
    >
      {/* Both icons are always in the DOM; only one is shown, so the swap costs
          no layout shift. suppressHydrationWarning is unnecessary because the
          server always renders the Moon (mounted === false). */}
      {mounted && isDark ? <Sun className="size-5" /> : <Moon className="size-5" />}
    </button>
  );
}
