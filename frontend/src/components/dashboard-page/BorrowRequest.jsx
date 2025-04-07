import { Button } from "@/components/ui/button.jsx";
import { Card, CardContent } from "@/components/ui/card.jsx";
import { useState } from "react";
import { updateBorrowRequestStatusById } from "../../service/borrow_request-api.js";

export default function BorrowRequest({ id, name, requester, date, endDate, status: initialStatus = "PENDING" }) {
  // Initialize state with the status prop value
  const [status, setStatus] = useState(initialStatus);

  const handleAccept = async () => {
    // Only allow action if status is still PENDING
    if (status !== "PENDING") return;
    
    try {
      await updateBorrowRequestStatusById(id, "APPROVED");
      setStatus("APPROVED");
    } catch (error) {
      console.error("Failed to update status:", error);
    }
  };

  const handleDecline = async () => {
    // Only allow action if status is still PENDING
    if (status !== "PENDING") return;
    
    try {
      await updateBorrowRequestStatusById(id, "DECLINED");
      setStatus("DECLINED");
    } catch (error) {
      console.error("Failed to update status:", error);
    }
  };

  // Get correct styling based on status
  const getStatusClass = () => {
    switch (status) {
      case "APPROVED":
        return "bg-green-100 text-green-800";
      case "DECLINED":
        return "bg-red-100 text-red-800";
      case "PENDING":
      default:
        return "bg-yellow-100 text-yellow-800";
    }
  };

  // Only show action buttons if status is still PENDING
  const isPending = status === "PENDING";

  return (
    <Card>
      <CardContent className="p-4">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="md:w-1/4">
            <div className="aspect-square bg-muted rounded-lg flex items-center justify-center"></div>
          </div>
          <div className="flex-1">
            <div className="flex justify-between">
              <h3 className="text-xl font-semibold">Request for "{name}"</h3>
              <div
                className={`px-4 py-1 mr-4 rounded-full text-xs ${getStatusClass()}`}
              >
                {status}
              </div>
            </div>
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
            {isPending && (
              <div className="flex gap-2 mt-4">
                <Button variant="positive" size="sm" onClick={handleAccept}>
                  Accept
                </Button>
                <Button variant="destructive" size="sm" onClick={handleDecline}>
                  Decline
                </Button>
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}