import { useState } from 'react';
import { Button } from "@/components/ui/button.jsx";
import { Card, CardContent } from "@/components/ui/card.jsx";
import { Badge } from "@/components/ui/badge.jsx"; // Import Badge
import { actOnBorrowRequest } from '@/service/dashboard-api.js'; // Assuming toast is available globally or via context
// import { toast } from 'react-toastify'; // Example: if using react-toastify

export default function BorrowRequest({ id, name, requester, date, endDate, status, refreshRequests, imageSrc }) { // Add imageSrc prop
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleAction = async (status) => {
    if (!id) {
      console.error("Borrow request ID is missing!");
      // toast.error("Cannot process request: ID missing.");
      setError("Cannot process request: ID missing.");
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      await actOnBorrowRequest(id, { status });
      // toast.success(`Request ${status.toLowerCase()}ed successfully!`);
      if (refreshRequests) {
        refreshRequests(); // Refresh the list in the parent component
      }
    } catch (err) {
      console.error(`Failed to ${status.toLowerCase()} borrow request:`, err);
      // toast.error(`Failed to ${status.toLowerCase()} request. Please try again.`);
      setError(`Failed to ${status.toLowerCase()} request.`);
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
            <h3 className="text-xl font-semibold">Request for "{name}"</h3>
            <Badge
              variant={
                status === 'Approved' ? 'positive' :
                status === 'Declined' ? 'destructive' :
                'outline' // Default for Pending or other statuses
              }
              className="text-xs"
            >
              {status || 'Pending'} {/* Display status, default to Pending */}
            </Badge>
          </div>
          {error && <p className="text-red-500 text-sm mt-2">{error}</p>}
          <div className="grid gap-1 mt-2">
            <div className="text-sm">
              <span className="font-medium">From:</span> {requester}
            </div>
            <div className="text-sm">
              <span className="font-medium">Requested on:</span> {date}
            </div>
            <div className="text-sm">
              <span className="font-medium">End Date:</span> {endDate}
            </div>
          </div>
          <div className="flex gap-2 mt-4">
            <Button variant="positive" size="sm" onClick={() => handleAction('Approved')} disabled={isLoading}>
              {isLoading ? 'Processing...' : 'Accept'}
            </Button>
            <Button variant="destructive" size="sm" onClick={() => handleAction('Declined')} disabled={isLoading}>
              {isLoading ? 'Processing...' : 'Decline'}
            </Button>
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
}