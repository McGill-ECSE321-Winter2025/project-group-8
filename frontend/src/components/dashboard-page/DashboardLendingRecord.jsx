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
    // Early return conditions
    if (!user?.id || !isAuthenticated || !authReady) {
      console.log("Skipping fetch: Auth prerequisites not met", { 
        userId: user?.id,
        isAuthenticated,
        authReady
      });
      setIsLoading(false);
      return;
    }
    
    // Prevent duplicate fetches while already loading
    if (isLoading && fetchAttempted) {
      console.log("Skipping duplicate fetch: Already loading");
      return;
    }
    
    try {
      setIsLoading(true);
      setFetchAttempted(true);
      
      // Log cookie state before fetching
      const cookieState = getCookieAuthState();
      console.log("Cookie state before fetching:", cookieState);
      
      console.log(`Fetching lending history for user ID: ${user.id}`);
      const records = await getLendingHistory(user.id, true); // true indicates user is the owner
      console.log(`Received ${records.length} lending records`);
      
      // Check if records is an array
      if (!Array.isArray(records)) {
        console.error("Expected array of records but got:", records);
        setLendingRecords([]);
        setError("Invalid data format received from the server.");
        return;
      }
      
      // Transform records to include proper status
      const processedRecords = records.map(record => {
        if (!record) return null; // Skip null/undefined records
        
        // Calculate if the record is overdue
        const isOverdue = record.endDate && new Date() > new Date(record.endDate) && 
                         record.status !== 'CLOSED';
        
        // Determine display status
        const status = record.status === 'CLOSED' 
          ? 'Returned' 
          : (isOverdue ? 'Overdue' : 'Active');
          
        return {
          ...record,
          status: status
        };
      }).filter(Boolean); // Remove any null entries
      
      setLendingRecords(processedRecords);
      setError(null); // Clear any previous errors
    } catch (err) {
      console.error("Error fetching lending records:", err);
      if (err instanceof UnauthorizedError) {
        setError("Authentication error. Please try logging in again.");
      } else {
        setError("Failed to load lending records. Please try again later.");
      }
      setLendingRecords([]); // Set empty array on error
    } finally {
      setIsLoading(false);
    }
  }, [user?.id, isAuthenticated, authReady, isLoading, fetchAttempted]);

  // Reset fetch attempted when auth state changes
  useEffect(() => {
    if (authReady && isAuthenticated && user?.id) {
      setFetchAttempted(false);
    }
  }, [authReady, isAuthenticated, user?.id]);

  // Initial fetch when component mounts or user/auth state changes
  useEffect(() => {
    // Only fetch when auth is ready and not already loading
    if (authReady && isAuthenticated && user?.id && !isLoading && !fetchAttempted) {
      console.log("Initial fetch of lending records triggered");
      fetchLendingRecords();
    }
  }, [fetchLendingRecords, authReady, isAuthenticated, user?.id, isLoading, fetchAttempted]);

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
            name={record.game?.name || "Unknown Game"}
            requester={record.borrower?.name || "Unknown User"}
            startDate={record.startDate}
            endDate={record.endDate}
            status={record.status}
            imageSrc={record.game?.imageUrl || record.gameImage} // Try to use image URL from game object first
            refreshRecords={fetchLendingRecords} // Pass the refresh function
          />
        )
      )}
    </div>
  </TabsContent>
}