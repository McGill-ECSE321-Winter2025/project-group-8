import BorrowRequest from "@/components/dashboard-page/BorrowRequest.jsx";
import {TabsContent} from "@/components/ui/tabs.jsx";
import { useEffect, useState, useCallback } from "react";
import { getIncomingBorrowRequests } from "@/service/dashboard-api";
import { UnauthorizedError } from "@/service/apiClient"; // Import UnauthorizedError
import { useAuth } from "@/context/AuthContext";
import { Loader2 } from "lucide-react";

export default function DashboardBorrowRequests() {
  const [borrowRequests, setBorrowRequests] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [fetchAttempted, setFetchAttempted] = useState(false);
  const { user, isAuthenticated, authReady } = useAuth();

  // Use useCallback to memoize the fetchBorrowRequests function
  const fetchBorrowRequests = useCallback(async () => {
    if (!user?.id || !isAuthenticated || !authReady) {
      if (!isLoading) return; // Don't update state if not loading
      setIsLoading(false);
      return;
    }
    
    // Prevent excessive retries
    if (fetchAttempted && !isLoading) return;
    
    try {
      setIsLoading(true);
      setFetchAttempted(true);
      // Wait a bit to ensure auth is fully established
      await new Promise(resolve => setTimeout(resolve, 300));
      
      const requests = await getIncomingBorrowRequests(user.id);
      setBorrowRequests(requests);
      setError(null); // Clear any previous errors
    } catch (err) {
      console.error("Error fetching borrow requests:", err);
      if (err instanceof UnauthorizedError) {
        setError("Authentication error. Please try logging in again.");
      } else {
        setError("Failed to load borrow requests. Please try again later.");
      }
    } finally {
      setIsLoading(false);
    }
  }, [user, isAuthenticated, authReady, isLoading, fetchAttempted]);

  // Reset fetch attempted when auth state changes
  useEffect(() => {
    if (authReady && isAuthenticated && user?.id) {
      setFetchAttempted(false);
    }
  }, [authReady, isAuthenticated, user]);

  // Initial fetch when component mounts or user/auth state changes
  useEffect(() => {
    // Add a delay before attempting to fetch to ensure auth is fully established
    const timer = setTimeout(() => {
      if (authReady && isAuthenticated && user?.id) {
        fetchBorrowRequests();
      }
    }, 1000); // Increase delay to 1 second
    
    return () => clearTimeout(timer);
  }, [fetchBorrowRequests, authReady, isAuthenticated, user]);

  return <TabsContent value="requests" className="space-y-6">
    <div className="flex justify-between items-center">
      <h2 className="text-2xl font-bold">Incoming Borrow Requests</h2>
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
            imageSrc={request.gameImage} // Pass the image source
          />
        )
      )}
    </div>
  </TabsContent>
}