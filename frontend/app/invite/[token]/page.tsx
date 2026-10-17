import InviteView from "./InviteView";

// 动态路由按请求渲染，读取 params 属运行时数据，跳过静态 shell 预渲染校验。
export const instant = false;

export default async function Page({
  params,
}: {
  params: Promise<{ token: string }>;
}) {
  const { token } = await params;
  return <InviteView token={token} />;
}
