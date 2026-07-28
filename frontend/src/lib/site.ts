// Single source of truth for public-facing brand + contact details, shared by
// the header, footer, and the /contact page. Update here when real details
// change. Social links are only rendered when set — leave a field empty
// (undefined) to hide that icon rather than link somewhere broken.
export const SITE = {
  name: "Dhyan Mitra",
  tagline: "Awaken Your Inner Peace",
  legalName: "DYJK Dhyan Mitra — Yoga & Meditation Institute",
  proprietor: "Shiv Narayan Gupta",
  email: "admin@dhyanmitra.in",
  phone: "+91 95696 93176",
  location: "India",
  domain: "dhyanmitra.in",
  socials: {
    facebook: "https://www.facebook.com/profile.php?id=100009266976523",
    instagram: "",
    youtube: "",
    linkedin: "",
  },
} as const;

/** Non-empty social links only, so the UI never renders a dead icon. */
export function activeSocials() {
  return (Object.entries(SITE.socials) as [keyof typeof SITE.socials, string][]).filter(
    ([, url]) => url.length > 0,
  );
}
