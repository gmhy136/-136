import TopNav from "@/components/TopNav";

export default function Home() {
  return (
    <div className="app">
      <TopNav />

      <div className="content">
        <div className="page-head">
          <div className="t">今天也要好好记录</div>
          <div className="s">2026 年 10 月 13 日 · 星期二</div>
        </div>

        <div className="rec st-quiet">
          <div className="top">
            <div className="mood">
              <span className="mdot md-calm" /> 平静
            </div>
            <span className="time">刚刚</span>
          </div>
          <p>写下一段文字，记录今天的心情。</p>
          <div className="foot">
            <span className="time">原创</span>
          </div>
        </div>

        <button className="btn btn-p">写一张新卡</button>
      </div>
    </div>
  );
}
