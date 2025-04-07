import { useState } from 'react';
import { Card, CardContent } from "@/components/ui/card.jsx";
import { Badge } from "@/components/ui/badge.jsx"; // Import Badge
import { Button } from "@/components/ui/button.jsx";
import { markAsReturned } from '@/service/dashboard-api.js';

export default function LendingRecord({ id, name, requester, startDate, endDate, status, refreshRecords, imageSrc }) { // Add imageSrc prop
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isReturned, setIsReturned] = useState(status === 'Returned' || status === 'CLOSED');

  // Calculate if the lending is overdue based on the current date and end date
  const isOverdue = new Date() > new Date(endDate) && status !== 'Returned' && status !== 'CLOSED';
  
  // Determine the current status to display
  const displayStatus = isReturned 
    ? 'Returned' 
    : (isOverdue ? 'Overdue' : (status || 'Active'));

  const handleMarkReturned = async () => {
    if (!id) {
      console.error("Lending record ID is missing!");
      setError("Cannot process request: ID missing.");
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      console.log(`Attempting to mark record ${id} as returned...`);
      // Use an empty object for the data payload
      const response = await markAsReturned(id, {});
      console.log("Mark as returned response:", response);
      
      // If we get a successful response, update the UI
      // The backend updates to OVERDUE status as a placeholder for "awaiting owner confirmation"
      // but we want to show it as "Returned" in the UI
      setIsReturned(true);
      
      console.log("Success: Game marked as returned successfully!");
      
      // Add a delay before refreshing to ensure backend processes the update
      setTimeout(() => {
        if (refreshRecords) {
          refreshRecords(); // Refresh the list in the parent component
        }
      }, 500);
    } catch (err) {
      console.error("Failed to mark as returned:", err);
      // Create a more user-friendly error message
      let errorMessage = "Failed to mark as returned.";
      if (err.message && err.message.includes("404")) {
        errorMessage = "Record not found. It may have been deleted or already processed.";
      } else if (err.message && err.message.includes("403")) {
        errorMessage = "You don't have permission to mark this record as returned.";
      } else if (err.message) {
        // Include the actual error message for debugging
        errorMessage = `Failed to mark as returned: ${err.message}`;
      }
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return <Card>
    <CardContent className="p-4">
      <div className="flex flex-col md:flex-row gap-4">
        <div className="md:w-1/4">
          <img
            src={imageSrc || "https://placehold.co/200x200/e9e9e9/1d1d1d?text=No+Image"}
            alt={`Cover art for ${name}`}
            className="w-full h-full object-cover rounded-lg aspect-square"
            onError={(e) => {
              e.target.src = "https://placehold.co/200x200/e9e9e9/1d1d1d?text=No+Image";
            }}
          />
        </div>
        <div className="flex-1">
          <div className="flex justify-between">
            <h3 className="text-xl font-semibold">"{name}" has been lent</h3>
            <Badge
              variant={
                displayStatus === 'Returned' ? 'positive' :
                displayStatus === 'Overdue' ? 'destructive' :
                'outline' // Default for Active or other statuses
              }
              className="text-xs"
            >
              {displayStatus}
            </Badge>
          </div>
          {error && <p className="text-red-500 text-sm mt-2">{error}</p>}
          <div className="grid gap-1 mt-2">
            <div className="text-sm">
              <span className="font-medium">Borrower:</span> {requester}
            </div>
            <div className="text-sm">
              <span className="font-medium">Lent on:</span> {startDate ? new Date(startDate).toLocaleDateString() : 'Unknown'}
            </div>
            <div className="text-sm">
              <span className="font-medium">Return by:</span> {endDate ? new Date(endDate).toLocaleDateString() : 'Unknown'}
            </div>
          </div>
          {!isReturned && ( // Only show button if not already returned
            <div className="flex mt-4">
              <Button variant="positive" size="sm" onClick={handleMarkReturned} disabled={isLoading}>
                {isLoading ? 'Processing...' : 'Mark as returned'}
              </Button>
            </div>
          )}
        </div>
      </div>
    </CardContent>
  </Card>
}