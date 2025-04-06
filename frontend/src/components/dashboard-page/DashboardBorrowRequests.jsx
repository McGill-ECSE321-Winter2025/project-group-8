import { useEffect, useState, useCallback } from "react";
import BorrowRequest from "@/components/dashboard-page/BorrowRequest.jsx";
import { TabsContent } from "@/components/ui/tabs.jsx";
// Imports kept/adapted from HEAD for Auth/Loading/Error handling
import { UnauthorizedError } from "@/service/apiClient";
import { useAuth } from "@/context/AuthContext";
import { Loader2 } from "lucide-react";
// Imports added from origin/dev-Yessine-D3 for logic/UI
import { getBorrowRequestsByOwner } from "@/service/borrow_request-api.js"; // Use this fetch
import { getGameById } from "@/service/game-api.js"; // For enriching data
import { toast } from "sonner"; // For notifications

export default function DashboardBorrowRequests() {
  // State from HEAD (more robust)
  const [borrowRequests, setBorrowRequests] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [fetchAttempted, setFetchAttempted] = useState(false);
  const { user, isAuthenticated, authReady } = useAuth(); // Auth context from HEAD

  // Adapted fetch function
  const fetchBorrowRequests = useCallback(async () => {
    // Checks from HEAD
    if (!user?.id || !isAuthenticated || !authReady) {
      if (!isLoading) return;
      setIsLoading(false);
      return;
    }
    // Prevent excessive retries - check if still needed
    // if (fetchAttempted && !isLoading) return;

    try {
      setIsLoading(true);
      setFetchAttempted(true); // Mark that fetch was attempted
      setError(null); // Clear previous errors

      // Fetch logic from origin/dev-Yessine-D3, using user.id from context
      const ownerRequests = await getBorrowRequestsByOwner(user.id);

      // Enriching logic from origin/dev-Yessine-D3
      const enrichedRequests = await Promise.all(
        ownerRequests.map(async (req) => {
          try {
            // Assuming requestedGameId exists on the request object
            const game = await getGameById(req.requestedGameId);
            // Include game name and potentially image URL
            return { ...req, requestedGameName: game.name, gameImage: game.imageUrl, requesterName: req.requesterName }; // Pass requester name if available
          } catch (error) {
            console.error(`Error fetching game details for request ${req.id}, game ${req.requestedGameId}:`, error);
            // Return request with placeholder data if game fetch fails
            return { ...req, requestedGameName: "(Unknown Game)", gameImage: null, requesterName: req.requesterName };
          }
        })
      );

      setBorrowRequests(enrichedRequests); // Update state with enriched data

    } catch (err) {
      console.error("Error fetching borrow requests:", err);
      // Error handling combining HEAD's state and origin's toast
      const errorMsg = err instanceof UnauthorizedError
        ? "Authentication error. Please try logging in again."
        : "Failed to load borrow requests: " + (err.message || "Please try again later.");

      setError(errorMsg);
      toast.error(errorMsg);
      setBorrowRequests([]); // Clear data on error
    } finally {
      setIsLoading(false);
    }
    // Dependencies adapted: fetchAttempted removed as it might cause issues with re-fetching
  }, [user, isAuthenticated, authReady]);

  // useEffect hooks from HEAD to manage fetching based on auth state
  useEffect(() => {
    // Reset fetch attempted flag when auth state becomes ready and authenticated
    // This allows re-fetching if the user logs in after initial load
    if (authReady && isAuthenticated && user?.id) {
      setFetchAttempted(false);
    }
  }, [authReady, isAuthenticated, user]);

  useEffect(() => {
    // Fetch only when auth is ready, user is authenticated, and fetch hasn't been attempted yet for this auth state
    if (authReady && isAuthenticated && user?.id && !fetchAttempted) {
       const timer = setTimeout(() => {
         fetchBorrowRequests();
       }, 300); // Short delay might still be useful
       return () => clearTimeout(timer);
    } else if (authReady && (!isAuthenticated || !user?.id)) {
       // If auth is ready but user is not logged in, ensure loading is false and clear data
       setIsLoading(false);
       setError(null);
       setBorrowRequests([]);
       setFetchAttempted(false); // Reset fetch attempt flag
    }
  }, [fetchBorrowRequests, authReady, isAuthenticated, user, fetchAttempted]);


  // JSX structure from HEAD, title from origin/dev-Yessine-D3, mapping adapted
  return (
    <TabsContent value="requests" className="space-y-6">
      <div className="flex justify-between items-center">
        {/* Title kept as 'Incoming Borrow Requests' to match API call */}
        <h2 className="text-2xl font-bold">Incoming Borrow Requests</h2>
      </div>
      <div className="space-y-4">
        {/* Conditional rendering from HEAD */}
        {isLoading ? (
          <div className="flex justify-center items-center py-10">
            <Loader2 className="w-8 h-8 animate-spin text-primary" />
          </div>
        ) : error ? (
          <div className="text-center py-10 text-red-500">{error}</div>
        ) : borrowRequests.length === 0 ? (
          <div className="text-center py-10 text-muted-foreground">No incoming borrow requests found.</div>
        ) : (
          // Mapping adapted for enriched data
          borrowRequests.map(request =>
            <BorrowRequest
              key={request.id}
              id={request.id}
              name={request.requestedGameName || "(Unknown Game)"} // Use enriched name
              // Displaying requester info for incoming requests
              requester={request.requesterName || request.requesterId || "(Unknown Requester)"}
              date={new Date(request.startDate || request.requestDate).toLocaleString()} // Use startDate or requestDate
              endDate={new Date(request.endDate).toLocaleString()}
              status={request.status}
              imageSrc={request.gameImage} // Use enriched image
            />
          )
        )}
      </div>
    </TabsContent>
  );
}
