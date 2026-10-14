import type { Metadata } from "next";
import "./globals.css";
// 字体本地化（Fontsource）：墙内无需访问 Google Fonts，按需加载子集
import "@fontsource/noto-sans-sc/400.css";
import "@fontsource/noto-sans-sc/500.css";
import "@fontsource/noto-sans-sc/600.css";
import "@fontsource/noto-sans-sc/700.css";
import "@fontsource/noto-serif-sc/500.css";
import "@fontsource/noto-serif-sc/600.css";
import "@fontsource/noto-serif-sc/700.css";
import "@fontsource/ma-shan-zheng/400.css";
import { Providers } from "./providers";

export const metadata: Metadata = {
  title: "云记录",
  description: "温柔的个人记录与理想规划空间",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="zh-CN">
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
