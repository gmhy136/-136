import DailyQuoteCard from "@/components/DailyQuoteCard";
import TopNav from "@/components/TopNav";

export default function Home() {
  return (
    <div className="app">
      <TopNav />
      <DailyQuoteCard />
    </div>
  );
}
