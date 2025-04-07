import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card.jsx";
import {Avatar, AvatarFallback, AvatarImage} from "@/components/ui/avatar.jsx";
import {Tabs, TabsList, TabsTrigger} from "@/components/ui/tabs.jsx";
import DashboardBorrowRequests from "@/components/dashboard-page/DashboardBorrowRequests.jsx";
import DashboardEvents from "@/components/dashboard-page/DashboardEvents.jsx";
import DashboardGameLibrary from "@/components/dashboard-page/DashboardGameLibrary.jsx";
import DashboardLendingRecord from "@/components/dashboard-page/DashboardLendingRecord.jsx";
import SideMenuBar from "@/components/dashboard-page/SideMenuBar.jsx";
import { Route, Routes } from "react-router-dom";
import { useState, useEffect } from "react"; // Import hooks
import { getUserInfoByEmail } from "../service/user-api.js"; // Import from user-api.js
import { Loader2 } from "lucide-react"; // Import loader

export default function DashboardPage() {
  // State for user info and loading/error
  const [accountInfo, setAccountInfo] = useState(null);
  const [userType, setUserType] = useState(null); // 'player' or 'owner'
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAccountInfo = async () => {
      setIsLoading(true);
      setError(null);
      const email = localStorage.getItem("userEmail");

      if (!email) {
        setError("User email not found. Please log in again.");
        setIsLoading(false);
        return;
      }

      try {
        const data = await getUserInfoByEmail(email); // Use correct function name
        setAccountInfo(data);
        setUserType(data.gameOwner ? "owner" : "player"); // Determine user type
      } catch (err) {
        console.error("Failed to fetch account info:", err);
        setError(err.message || "Could not load user information.");
      } finally {
        setIsLoading(false);
      }
    };

    fetchAccountInfo();
  }, []); // Fetch on mount

  // Loading state
  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[calc(100vh-100px)]"> {/* Adjust height as needed */}
        <Loader2 className="h-16 w-16 animate-spin text-muted-foreground" />
      </div>
    );
  }

  // Error state
  if (error) {
     return (
       <div className="flex justify-center items-center min-h-[calc(100vh-100px)] text-destructive">
         <p>Error: {error}</p>
       </div>
     );
  }

  // Render dashboard once data is loaded
  return (
    <div className="flex flex-col">
      <main className="flex py-5 mx-10 justify-between space-x-10">
        <aside className="min-w-[300px]">
          <div className="flex flex-row gap-4">
            <Card className="w-full flex flex-col gap-4">
              <CardHeader className="w-[1/4] flex flex-row items-center gap-4">
                <Avatar className="h-12 w-12">
                  {/* TODO: Add actual user avatar if available */}
                  <AvatarImage src="/placeholder.svg?height=48&width=48" alt={accountInfo?.username || 'User'}/>
                  {/* Fallback uses initials from username */}
                  <AvatarFallback>{accountInfo?.username ? accountInfo.username.substring(0, 2).toUpperCase() : 'U'}</AvatarFallback>
                </Avatar>
                <div>
                  {/* Use username from the fetched accountInfo */}
                  <CardTitle>{accountInfo?.username || 'User'}</CardTitle>
                  <CardDescription>{userType === "owner" ? "Game Owner" : "Player"}</CardDescription>
                </div>
              </CardHeader>
              <CardContent>
                <nav className="flex flex-col gap-2 pt-4">
                  <SideMenuBar userType={userType}/>
                </nav>
              </CardContent>
            </Card>
          </div>
        </aside>
        <div className="flex-1 w-full min-w-[420px]">
          <Tabs className="bg-background">
            <TabsList className="w-full h-10 mb-2">
              {/* Conditionally render Games tab trigger if user is owner? Or handle inside component */}
              <TabsTrigger value="events">Events</TabsTrigger>
              <TabsTrigger value="games">Game Library</TabsTrigger>
              <TabsTrigger value="requests">Borrow Requests</TabsTrigger>
              <TabsTrigger value="borrowing">Lending History</TabsTrigger>
            </TabsList>
            {/* Pass fetched userType to child components */}
            <DashboardGameLibrary userType={userType} />
            <DashboardEvents userType={userType} />
            <DashboardBorrowRequests userType={userType} />
            <DashboardLendingRecord userType={userType} />
          </Tabs>
        </div>
      </main>
    </div>
  )
}
