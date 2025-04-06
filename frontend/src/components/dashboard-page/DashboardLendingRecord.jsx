import {TabsContent} from "@/components/ui/tabs.jsx";
import LendingRecord from "@/components/dashboard-page/LendingRecord.jsx";
import { useEffect, useState } from "react";
import { getLendingHistory } from "@/service/dashboard-api";
import { useAuth } from "@/context/AuthContext";
import { Loader2 } from "lucide-react";

export default function DashboardLendingRecord() {
  const [lendingRecords, setLendingRecords] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const { user } = useAuth();

  useEffect(() => {
    async function fetchLendingRecords() {
      if (!user?.id) return;
      
      try {
        setIsLoading(true);
        const records = await getLendingHistory(user.id, true); // true indicates user is the owner
        setLendingRecords(records);
      } catch (err) {
        console.error("Error fetching lending records:", err);
        setError("Failed to load lending records. Please try again later.");
      } finally {
        setIsLoading(false);
      }
    }

    fetchLendingRecords();
  }, [user]);

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