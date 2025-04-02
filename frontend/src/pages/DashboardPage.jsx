import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card.jsx";
import {Avatar, AvatarFallback, AvatarImage} from "@/components/ui/avatar.jsx";
import {Button} from "@/components/ui/button.jsx";
import {Link} from "react-router-dom";
import {Menubar} from "@/components/ui/menubar.jsx";
import {Tabs, TabsList, TabsTrigger} from "@/components/ui/tabs.jsx";
import DashboardBorrowRequests from "@/components/dashboard-page/DashboardEvents.jsx";
import DashboardEvents from "@/components/dashboard-page/DashboardEvents.jsx";
import DashboardGameLibrary from "@/components/dashboard-page/DashboardGameLibrary.jsx";
import DashboardLendingRecord from "@/components/dashboard-page/DashboardLendingRecord.jsx";

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
                  <Link to="/dashboard">
                    <Button variant="ghost" className="w-full justify-start gap-2">
                      Profile
                    </Button>
                  </Link>
                  <Link to="/dashboard/games">
                    <Button variant="ghost" className="w-full justify-start gap-2">
                      My Games
                    </Button>
                  </Link>
                  <Link to="/dashboard/events">
                    <Button variant="ghost" className="w-full justify-start gap-2">
                      My Events
                    </Button>
                  </Link>
                  {userType === "owner" && (
                    <Link to="/dashboard/requests">
                      <Button variant="ghost" className="w-full justify-start gap-2">
                        Borrow Requests
                      </Button>
                    </Link>
                  )}
                  <Link to="/dashboard/settings">
                    <Button variant="ghost" className="w-full justify-start gap-2">
                      Settings
                    </Button>
                  </Link>
                </nav>
              </CardContent>
            </Card>
          </div>
        </aside>
        <div className="flex-1 w-full min-w-[420px]">
          <Tabs className="bg-background">
            <TabsList className="w-full h-10 mb-2">
              <TabsTrigger value="games">Game Library</TabsTrigger>
              <TabsTrigger value="events">Events</TabsTrigger>
              <TabsTrigger value="requests">Borrow Requests</TabsTrigger>
              <TabsTrigger value="borrowing">Lending History</TabsTrigger>
            </TabsList>
            <DashboardEvents/>
            <DashboardBorrowRequests/>
            <DashboardGameLibrary/>
            <DashboardLendingRecord/>
          </Tabs>
        </div>
      </main>
    </div>
  )
}