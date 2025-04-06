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
  const { user, isAuthenticated, authReady } = useAuth();

  // Use useCallback to memoize the fetchLendingRecords function
  const fetchLendingRecords = useCallback(async () => {
    if (!user?.id || !isAuthenticated || !authReady) {
      if (!isLoading) return; // Don't update state if not loading
      setIsLoading(false);
      return;
    }
    
    
    try {
      setIsLoading(true);
      setFetchAttempted(true);
      // Log cookie state before fetching
      const cookieState = getCookieAuthState();
      
      const records = await getLendingHistory(user.id, true); // true indicates user is the owner
      setLendingRecords(records);
      setError(null); // Clear any previous errors
    } catch (err) {
      console.error("Error fetching lending records:", err);
      if (err instanceof UnauthorizedError) {
        setError("Authentication error. Please try logging in again.");
      } else {
        setError("Failed to load lending records. Please try again later.");
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
            imageSrc={record.gameImage} // Pass the image source
          />
        )
      )}
    </div>
  </TabsContent>
}