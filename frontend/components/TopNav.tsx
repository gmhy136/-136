"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuthStore } from "@/store/auth";

const LINKS = [
  { href: "/", label: "首页" },
  { href: "/record", label: "记录" },
  { href: "/ideal", label: "理想" },
  { href: "/capsule", label: "胶囊" },
  { href: "/me", label: "我的" },
];

export default function TopNav() {
  const pathname = usePathname();
  const user = useAuthStore((s) => s.user);
  const avatarChar = user?.nickname?.slice(0, 1) || "我";

  return (
    <div className="topnav">
      <div className="logo">云记录</div>
      <nav className="nav-links">
        {LINKS.map((l) => {
          const on = l.href === "/" ? pathname === "/" : pathname.startsWith(l.href);
          return (
            <Link key={l.href} href={l.href} className={on ? "on" : undefined}>
              {l.label}
            </Link>
          );
        })}
      </nav>
      <div className="avatar" suppressHydrationWarning>
        {avatarChar}
      </div>
    </div>
  );
}
