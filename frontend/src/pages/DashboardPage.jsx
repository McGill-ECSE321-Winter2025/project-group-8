import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card.jsx";
import {Avatar, AvatarFallback, AvatarImage} from "@/components/ui/avatar.jsx";
import {Tabs, TabsList, TabsTrigger} from "@/components/ui/tabs.jsx";
import DashboardBorrowRequests from "@/components/dashboard-page/DashboardBorrowRequests.jsx";
import DashboardRegisteredEvents from "@/components/dashboard-page/DashboardRegisteredEvents.jsx";
import DashboardGameLibrary from "@/components/dashboard-page/DashboardGameLibrary.jsx";
import DashboardLendingRecord from "@/components/dashboard-page/DashboardLendingRecord.jsx";
import SideMenuBar from "@/components/dashboard-page/SideMenuBar.jsx";
import {Route, Routes} from "react-router-dom";
import DashboardMyEvents from "@/components/dashboard-page/DashboardMyEvents.jsx";

export default function DashboardPage() {
  const userType = "owner";
  return (
    <div className="flex flex-col">
      <main className="flex py-5 mx-10 justify-between space-x-10">
        <aside className="min-w-[300px]">
          <div className="flex flex-row gap-4">
            <Card className="w-full flex flex-col gap-4">
              <CardHeader className="w-[1/4] flex flex-row items-center gap-4">
                <Avatar className="h-12 w-12">
                  <AvatarImage src="/placeholder.svg?height=48&width=48" alt="User"/>
                  <AvatarFallback>JD</AvatarFallback>
                </Avatar>
                <div>
                  <CardTitle>John Doe</CardTitle>
                  <CardDescription>{userType === "owner" ? "Game Owner" : "Player"}</CardDescription>
                </div>
              </CardHeader>
              <CardContent>
                <nav className="flex flex-col gap-2 pt-4">
                  <Routes>
                    <Route path="profile" element={<SideMenuBar userType={userType}/>} />
                  </Routes>
                </nav>
              </CardContent>
            </Card>
          </div>
        </aside>
        <div className="flex-1 w-full min-w-[420px]">
          <Tabs className="bg-background">
            <TabsList className="w-full h-10 mb-2">
              <TabsTrigger value="games">Game Library</TabsTrigger>
              <TabsTrigger value="myevents">My Events</TabsTrigger>
              <TabsTrigger value="events">Upcoming Events</TabsTrigger>
              <TabsTrigger value="requests">Borrow Requests</TabsTrigger>
              <TabsTrigger value="borrowing">Lending History</TabsTrigger>
            </TabsList>
            <DashboardGameLibrary userType={userType}/>
            <DashboardRegisteredEvents/>
            <DashboardMyEvents/>
            <DashboardBorrowRequests/>
            <DashboardLendingRecord/>
          </Tabs>
        </div>
      </main>
    </div>
  )
}