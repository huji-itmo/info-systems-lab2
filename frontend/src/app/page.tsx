"use client"

import { MainContent } from "@/components/main-content";
import { Card, CardContent } from "@/components/ui/card";

export default function Home() {
  return (
    <div className="flex justify-center items-center">
      <Card className="">
        <CardContent className="flex flex-col justify-center items-center gap-2">
          <MainContent></MainContent>
        </CardContent>
      </Card>
    </div>
  );
}
