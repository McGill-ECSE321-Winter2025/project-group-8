import {TabsContent} from "@/components/ui/tabs.jsx";
import LendingRecord from "@/components/dashboard-page/LendingRecord.jsx";
import { useEffect, useState } from "react";
import { getLendingHistory } from "@/service/dashboard-api";
import { UnauthorizedError } from "@/service/apiClient"; // Import UnauthorizedError
import { useAuth } from "@/context/AuthContext";
import { Loader2 } from "lucide-react";

export default function DashboardLendingRecord() {
  const [lendingRecords, setLendingRecords] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user, isSessionExpired, handleSessionExpired, logout } = useAuth();
  const [retryCount, setRetryCount] = useState(0);
  const MAX_RETRIES = 2; // Reduced from 3 to minimize unnecessary retries

  useEffect(() => {
    async function fetchLendingRecords() {
      if (!user?.id) return;
      
      // Don't attempt to fetch if session is known to be expired
      if (isSessionExpired) {
        setIsLoading(false);
        return;
      }
      
      try {
        setIsLoading(true);
        const records = await getLendingHistory(user.id, true); // true indicates user is the owner
        setLendingRecords(records);
        // Reset retry count on success
        setRetryCount(0);
      } catch (err) {
        if (err instanceof UnauthorizedError) {
          console.warn(`Unauthorized access fetching lending records (attempt ${retryCount + 1}/${MAX_RETRIES}).`, err);
          
          // Check if it's a session expired error
          if (err.message === 'Session expired') {
            // Notify the auth context about the session expiration
            handleSessionExpired();
            setIsLoading(false);
            return;
          }
          
          // Implement retry logic before logging out
          if (retryCount < MAX_RETRIES - 1) {
            console.log(`Retrying in 1 second... (attempt ${retryCount + 1}/${MAX_RETRIES})`);
            setRetryCount(prevCount => prevCount + 1);
            
            // Schedule a retry after 1 second
            setTimeout(() => {
              fetchLendingRecords();
            }, 1000);
            return; // Exit to avoid setting loading to false
          } else {
            // Max retries reached, now logout
            console.warn(`Max retries (${MAX_RETRIES}) reached. Logging out.`);
            logout();
          }
        } else {
          console.error("Error fetching lending records:", err);
          setError("Failed to load lending records. Please try again later.");
        }
      } finally {
        // Only set loading to false if we're not retrying
        if (retryCount >= MAX_RETRIES - 1 || !error) {
          setIsLoading(false);
        }
      }
    }

    fetchLendingRecords();
  }, [user, isSessionExpired, handleSessionExpired]);

  // Effect to handle session expiration state changes
  useEffect(() => {
    if (isSessionExpired) {
      setIsLoading(false);
      setError("Your session has expired. Please log in again.");
    }
  }, [isSessionExpired]);

  return <TabsContent value="borrowing" className="space-y-6">
    <div className="flex justify-between items-center">
      <h2 className="text-2xl font-bold">Lending History</h2>
    </div>
    <div className="space-y-4">
      {isLoading ? (
        <div className="flex justify-center items-center py-10">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
        </div>
      ) : isSessionExpired ? (
        <div className="text-center py-10 text-red-500">Your session has expired. Please log in again.</div>
      ) : error ? (
        <div className="text-center py-10 text-red-500">{error}</div>
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