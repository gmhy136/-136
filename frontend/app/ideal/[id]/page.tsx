import IdealDetail from "./IdealDetail";

// 动态路由按请求渲染，读取 params 属运行时数据，跳过静态 shell 预渲染校验。
export const instant = false;

export default async function Page({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <IdealDetail id={Number(id)} />;
}
