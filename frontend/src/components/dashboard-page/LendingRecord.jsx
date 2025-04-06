import { useState } from 'react';
import { Card, CardContent } from "@/components/ui/card.jsx";
import { Badge } from "@/components/ui/badge.jsx"; // Import Badge
import { Button } from "@/components/ui/button.jsx";
import { markAsReturned } from '@/service/dashboard-api.js';
// import { toast } from 'react-toastify'; // Example

export default function LendingRecord({ id, name, requester, startDate, endDate, status, refreshRecords, imageSrc }) { // Add imageSrc prop
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleMarkReturned = async () => {
    if (!id) {
      console.error("Lending record ID is missing!");
      // toast.error("Cannot process request: ID missing.");
      setError("Cannot process request: ID missing.");
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      // The API function `markAsReturned` expects (lendingId, information)
      // Assuming no extra information is needed for now, passing null or an empty object.
      // Adjust if specific 'information' payload is required by the backend.
      await markAsReturned(id, {});
      // toast.success("Game marked as returned successfully!");
      if (refreshRecords) {
        refreshRecords(); // Refresh the list in the parent component
      }
    } catch (err) {
      console.error("Failed to mark as returned:", err);
      // toast.error("Failed to mark as returned. Please try again.");
      setError("Failed to mark as returned.");
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
          />
        </div>
        <div className="flex-1">
          <div className="flex justify-between">
            <h3 className="text-xl font-semibold">"{name}" has been lent</h3>
            <Badge
              variant={
                status === 'Returned' ? 'positive' :
                status === 'Overdue' ? 'destructive' :
                'outline' // Default for Active or other statuses
              }
              className="text-xs"
            >
              {status || 'Active'} {/* Display status, default to Active */}
            </Badge>
          </div>
          {error && <p className="text-red-500 text-sm mt-2">{error}</p>}
          <div className="grid gap-1 mt-2">
            <div className="text-sm">
              <span className="font-medium">Borrower:</span> {requester}
            </div>
            <div className="text-sm">
              <span className="font-medium">Lent on:</span> {startDate}
            </div>
            <div className="text-sm">
              <span className="font-medium">Return by:</span> {endDate}
            </div>
          </div>
          {status !== 'Returned' && ( // Only show button if not already returned
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