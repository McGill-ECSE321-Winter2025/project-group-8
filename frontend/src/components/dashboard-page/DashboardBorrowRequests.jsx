import { useEffect, useState } from "react";
import BorrowRequest from "@/components/dashboard-page/BorrowRequest.jsx";
import { TabsContent } from "@/components/ui/tabs.jsx";
import { getBorrowRequestsByOwner } from "@/service/borrow_request-api.js";
import { getGameById } from "@/service/game-api.js";
import { toast } from "sonner";

export default function DashboardBorrowRequests() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchOwnerRequests = async () => {
    try {
      const ownerId = localStorage.getItem("userId");
      if (!ownerId) throw new Error("Owner ID not found in localStorage.");
      
      const ownerRequests = await getBorrowRequestsByOwner(ownerId);
      
      // Fetch game name for each request using game ID
      const enrichedRequests = await Promise.all(
        ownerRequests.map(async (req) => {
          try {
            const game = await getGameById(req.requestedGameId);
            return { ...req, requestedGameName: game.name };
          } catch (error) {
            return { ...req, requestedGameName: "(Unknown Game)" };
          }
        })
      );
      
      setRequests(enrichedRequests);
    } catch (error) {
      toast.error("Failed to fetch borrow requests: " + error.message);
    } finally {
      setLoading(false);
    }
  };

  // Handler for status changes
  const handleStatusChange = (requestId, newStatus) => {
    // Update the local state immediately
    setRequests(prevRequests => 
      prevRequests.map(req => 
        req.id === requestId ? { ...req, status: newStatus } : req
      )
    );
    
    // Show a confirmation toast
    toast.success(`Request ${newStatus.toLowerCase()}`);
  };

  useEffect(() => {
    fetchOwnerRequests();
  }, []);

  return (
    <TabsContent value="requests" className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold">My Borrow Requests</h2>
      </div>
      
      {loading ? (
        <p className="text-muted-foreground">Loading borrow requests...</p>
      ) : requests.length === 0 ? (
        <p className="text-muted-foreground">No borrow requests yet.</p>
      ) : (
        <div className="space-y-4">
          {requests.map((request) => (
            <BorrowRequest
              key={request.id}
              id={request.id}
              name={request.requestedGameName}
              requester={request.requesterId || "(Unknown Requester)"}
              date={new Date(request.startDate).toLocaleString()}
              endDate={new Date(request.endDate).toLocaleString()}
              status={request.status}
              onStatusChange={handleStatusChange}
            />
          ))}
        </div>
      )}
    </TabsContent>
  );
}