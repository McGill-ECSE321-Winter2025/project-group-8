import {TabsContent} from "@/components/ui/tabs.jsx";
import LendingRecord from "@/components/dashboard-page/LendingRecord.jsx";
import { useEffect, useState, useCallback } from "react";
import { getLendingHistory } from "@/service/dashboard-api";
import { UnauthorizedError, getCookieAuthState } from "@/service/apiClient"; // Import getCookieAuthState
import { useAuth } from "@/context/AuthContext";
import { Loader2 } from "lucide-react";

export default function DashboardLendingRecord() {
  const [lendingRecords, setLendingRecords] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [fetchAttempted, setFetchAttempted] = useState(false);
  const [retryCount, setRetryCount] = useState(0); // Track retry attempts
  const { user, isAuthenticated, authReady } = useAuth();

  // Use useCallback to memoize the fetchLendingRecords function
  const fetchLendingRecords = useCallback(async () => {
    if (!user?.id || !isAuthenticated || !authReady) {
      if (!isLoading) return; // Don't update state if not loading
      setIsLoading(false);
      return;
    }
    
    // Prevent excessive retries
    if (fetchAttempted && !isLoading && retryCount >= 3) {
      console.log(`Maximum retry count (${retryCount}) reached for lending records fetch`);
      return;
    }
    
    try {
      setIsLoading(true);
      setFetchAttempted(true);
      console.log(`Fetching lending records for user: ${user.id}, attempt ${retryCount + 1}`);
      
      // Log cookie state before fetching
      const cookieState = getCookieAuthState();
      console.log('Cookie auth state before lending records fetch:', cookieState);
      
      // Wait longer for each retry
      const delay = retryCount * 500 + 300; // 300ms, 800ms, 1300ms
      await new Promise(resolve => setTimeout(resolve, delay));
      
      const records = await getLendingHistory(user.id, true); // true indicates user is the owner
      setLendingRecords(records);
      setError(null); // Clear any previous errors
      setRetryCount(0); // Reset retry count on success
    } catch (err) {
      console.error("Error fetching lending records:", err);
      if (err instanceof UnauthorizedError) {
        setError("Authentication error. Please try logging in again.");
        
        // Only retry a limited number of times
        if (retryCount < 3) {
          console.log(`Auth error, will retry (${retryCount + 1}/3)...`);
          setRetryCount(prevCount => prevCount + 1);
          
          // Schedule retry after a delay
          setTimeout(() => {
            fetchLendingRecords();
          }, 1000 * (retryCount + 1)); // Increasing backoff: 1s, 2s, 3s
        }
      } else {
        setError("Failed to load lending records. Please try again later.");
      }
    } finally {
      setIsLoading(false);
    }
  }, [user, isAuthenticated, authReady, isLoading, fetchAttempted, retryCount]);

  // Reset fetch attempted when auth state changes
  useEffect(() => {
    if (authReady && isAuthenticated && user?.id) {
      setFetchAttempted(false);
      setRetryCount(0); // Reset retry count when auth state changes
    }
  }, [authReady, isAuthenticated, user]);

  // Initial fetch when component mounts or user/auth state changes
  useEffect(() => {
    // Add a delay before attempting to fetch to ensure auth is fully established
    const timer = setTimeout(() => {
      // Only fetch when authReady is true
      if (authReady && isAuthenticated && user?.id) {
        fetchLendingRecords();
      }
    }, 1500); // Increase delay to 1.5 seconds
    
    return () => clearTimeout(timer);
  }, [fetchLendingRecords, authReady, isAuthenticated, user]);

  return <TabsContent value="borrowing" className="space-y-6">
    <div className="flex justify-between items-center">
      <h2 className="text-2xl font-bold">Lending History</h2>
    </div>
    <div className="space-y-4">
      {isLoading ? (
        <div className="flex justify-center items-center py-10">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </div>
      ) : error ? (
        <div className="text-center py-10 text-red-500">
          {error}
          {retryCount > 0 && <div className="mt-2 text-sm">Retrying... ({retryCount}/3)</div>}
        </div>
      ) : lendingRecords.length === 0 ? (
        <div className="text-center py-10 text-muted-foreground">No lending records found.</div>
      ) : (
        lendingRecords.map(record => 
          <LendingRecord 
            key={record.id} 
            id={record.id}
            name={record.gameName || "Unknown Game"}
            requester={record.borrowerName || "Unknown User"}
            startDate={record.startDate}
            endDate={record.endDate}
          />
        )
      )}
    </div>
  </TabsContent>
}