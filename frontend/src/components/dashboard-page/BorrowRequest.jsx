import { useState } from 'react';
import { Button } from "@/components/ui/button.jsx";
import { Card, CardContent } from "@/components/ui/card.jsx";
import { Badge } from "@/components/ui/badge.jsx"; // Import Badge
import { actOnBorrowRequest } from '@/service/dashboard-api.js'; // Assuming toast is available globally or via context
import { useAuth } from "@/context/AuthContext"; // Import useAuth to check user type
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog.jsx";
import { getGameById } from '@/service/game-api.js';
import { getLendingRecordByRequestId } from '@/service/dashboard-api.js';
import ReviewForm from '../game-search-page/ReviewForm.jsx';
// import { toast } from 'react-toastify'; // Example: if using react-toastify

export default function BorrowRequest({ id, name, requester, date, endDate, status, refreshRequests, imageSrc, gameId, requestedGameId }) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const { user } = useAuth(); // Get user from auth context
  const [showDetails, setShowDetails] = useState(false);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [gameDetails, setGameDetails] = useState(null);
  const [lendingRecord, setLendingRecord] = useState(null);
  const [showReviewForm, setShowReviewForm] = useState(false);
  
  // Check if user is a game owner
  const isGameOwner = user?.gameOwner === true;

  // Determine if the game has been returned and can be reviewed by the requester
  const canReview = status === 'APPROVED' && 
                    !isGameOwner &&
                    lendingRecord?.status === 'CLOSED'; // Only allow reviews for returned games

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

  // Handle image error by falling back to placeholder
  const handleImageError = (e) => {
    e.target.src = "https://placehold.co/200x200/e9e9e9/1d1d1d?text=No+Image";
  };

  const handleViewDetails = async () => {
    setDetailsLoading(true);
    setError(null);
    try {
      // Get the actual game ID from the request
      const targetGameId = requestedGameId || gameId;
      if (targetGameId) {
        const gameResponse = await getGameById(targetGameId);
        setGameDetails(gameResponse);
      }

      // Check if there's a lending record for this request (to see if it's been returned)
      try {
        const lendingResponse = await getLendingRecordByRequestId(id);
        setLendingRecord(lendingResponse);
      } catch (lendingErr) {
        console.log("No lending record found or not yet approved", lendingErr);
      }
      
      setShowDetails(true);
    } catch (err) {
      console.error("Failed to load game details:", err);
      setError("Failed to load game details.");
    } finally {
      setDetailsLoading(false);
    }
  };

  const handleSubmitReview = (review) => {
    console.log("Review submitted:", review);
    setShowReviewForm(false);
    // Could add additional handling here, like showing a success message
  };

  return (
    <>
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-col md:flex-row gap-4">
            <div className="md:w-1/4">
              <img
                src={imageSrc || "https://placehold.co/200x200/e9e9e9/1d1d1d?text=No+Image"}
                alt={`Cover art for ${name}`}
                className="w-full h-full object-cover rounded-lg aspect-square"
                onError={handleImageError}
              />
            </div>
            <div className="flex-1">
              <div className="flex justify-between">
                <h3 className="text-xl font-semibold">Request for "{name}"</h3>
                <Badge
                  variant={
                    status === 'APPROVED' ? 'positive' :
                    status === 'DECLINED' ? 'destructive' :
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
                {/* Only show action buttons for game owners and if status is Pending */}
                {isGameOwner && status === 'PENDING' && (
                  <>
                    <Button 
                      variant="outline" 
                      disabled={isLoading} 
                      onClick={() => handleAction('DECLINED')}
                    >
                      {isLoading ? 'Processing...' : 'Decline'}
                    </Button>
                    <Button 
                      variant="positive" 
                      disabled={isLoading} 
                      onClick={() => handleAction('APPROVED')}
                    >
                      {isLoading ? 'Processing...' : 'Approve'}
                    </Button>
                  </>
                )}
                
                {/* View Details button for anyone */}
                <Button
                  variant="outline"
                  disabled={detailsLoading}
                  onClick={handleViewDetails}
                >
                  {detailsLoading ? 'Loading...' : 'View Details'}
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Details Dialog */}
      <Dialog open={showDetails} onOpenChange={setShowDetails}>
        <DialogContent className="max-w-[600px] max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Borrow Request Details</DialogTitle>
          </DialogHeader>
          
          <div className="space-y-4">
            {gameDetails && (
              <div className="space-y-2">
                <h3 className="font-medium text-lg">{gameDetails.name}</h3>
                <div className="flex gap-4">
                  <img
                    src={gameDetails.image || imageSrc || "https://placehold.co/200x200/e9e9e9/1d1d1d?text=No+Image"}
                    alt={gameDetails.name}
                    className="w-24 h-24 object-cover rounded"
                    onError={handleImageError}
                  />
                  <div className="space-y-1 text-sm">
                    <p><span className="font-medium">Category:</span> {gameDetails.category || 'Unknown'}</p>
                    <p><span className="font-medium">Players:</span> {gameDetails.minPlayers}-{gameDetails.maxPlayers}</p>
                    <p><span className="font-medium">Status:</span> {status}</p>
                    {lendingRecord && (
                      <p>
                        <span className="font-medium">Return Status:</span> {lendingRecord.status === 'CLOSED' ? 'Returned' : 'Not Returned'}
                      </p>
                    )}
                  </div>
                </div>
                {gameDetails.description && (
                  <p className="text-sm text-gray-600">{gameDetails.description}</p>
                )}
              </div>
            )}

            {/* Review section - only show for borrowers if game is returned */}
            {canReview && !showReviewForm && (
              <div className="pt-4 border-t">
                <Button 
                  onClick={() => setShowReviewForm(true)}
                  variant="outline"
                  className="w-full"
                >
                  Review This Game
                </Button>
              </div>
            )}

            {/* Review form */}
            {showReviewForm && (
              <div className="pt-4 border-t">
                <ReviewForm 
                  gameId={requestedGameId || gameId}
                  onReviewSubmitted={handleSubmitReview}
                  onCancel={() => setShowReviewForm(false)}
                />
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}