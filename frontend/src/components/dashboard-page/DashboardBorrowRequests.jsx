import BorrowRequest from "@/components/dashboard-page/BorrowRequest.jsx";
import {TabsContent} from "@/components/ui/tabs.jsx";
import { useEffect, useState } from "react";
import { getOutgoingBorrowRequests } from "@/service/dashboard-api";
import { useAuth } from "@/context/AuthContext";
import { Loader2 } from "lucide-react";

export default function DashboardBorrowRequests() {
  const [borrowRequests, setBorrowRequests] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user } = useAuth();

  useEffect(() => {
    async function fetchBorrowRequests() {
      if (!user?.id) return;
      
      try {
        setIsLoading(true);
        const requests = await getOutgoingBorrowRequests(user.id);
        setBorrowRequests(requests);
      } catch (err) {
        console.error("Error fetching borrow requests:", err);
        setError("Failed to load borrow requests. Please try again later.");
      } finally {
        setIsLoading(false);
      }
    }

    fetchBorrowRequests();
  }, [user]);

  return <TabsContent value="requests" className="space-y-6">
    <div className="flex justify-between items-center">
      <h2 className="text-2xl font-bold">My Borrow Requests</h2>
    </div>
    <div className="space-y-4">
      {isLoading ? (
        <div className="flex justify-center items-center py-10">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </div>
      ) : error ? (
        <div className="text-center py-10 text-red-500">{error}</div>
      ) : borrowRequests.length === 0 ? (
        <div className="text-center py-10 text-muted-foreground">No borrow requests found.</div>
      ) : (
        borrowRequests.map(request => 
          <BorrowRequest 
            key={request.id} 
            id={request.id}
            name={request.gameName || "Unknown Game"}
            requester={request.requesterName || user?.name || "You"}
            date={request.requestDate}
            endDate={request.endDate}
            status={request.status}
          />
        )
      )}
    </div>
  </TabsContent>
}