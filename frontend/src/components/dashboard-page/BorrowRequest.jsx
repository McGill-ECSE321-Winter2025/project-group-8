import { useState, useEffect } from 'react';
import { Button } from "@/components/ui/button.jsx";
import { Card, CardContent } from "@/components/ui/card.jsx";
import { Badge } from "@/components/ui/badge.jsx"; // Import Badge
import { actOnBorrowRequest } from '@/service/dashboard-api.js'; // Assuming toast is available globally or via context
import { useAuth } from "@/context/AuthContext"; // Import useAuth to check user type
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog.jsx";
import { getGameById } from '@/service/game-api.js';
import { getLendingRecordByRequestId } from '@/service/dashboard-api.js';
import ReviewForm from '../game-search-page/ReviewForm.jsx';
import { useNavigate } from 'react-router-dom'; // Import useNavigate for navigation

import { toast } from 'sonner';

export default function BorrowRequest({ id, name, requester, date, endDate, status, refreshRequests, imageSrc, gameId, requestedGameId }) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const { user } = useAuth(); // Get user from auth context
  const [showDetails, setShowDetails] = useState(false);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [gameDetails, setGameDetails] = useState(null);
  const [lendingRecord, setLendingRecord] = useState(null);
  const [showReviewForm, setShowReviewForm] = useState(false);

  const navigate = useNavigate(); // Initialize useNavigate

  const [isReturned, setIsReturned] = useState(false);

  
  // Check if user is a game owner
  const isGameOwner = user?.gameOwner === true;

  // Determine if this is a received request (not a sent request)
  // For received requests, the current user is the game owner, not the requester
  const isReceivedRequest = isGameOwner && requester !== user?.name;
  
  // Determine if the game has been returned and can be reviewed by the requester
  const canReview = status === 'APPROVED' && 
                    !isGameOwner &&
                    lendingRecord?.status === 'CLOSED'; // Only allow reviews for returned games

  // Load game details when component mounts to get owner information
  useEffect(() => {
    const loadGameDetails = async () => {
      try {
        const targetGameId = requestedGameId || gameId;
        if (targetGameId) {
          const gameResponse = await getGameById(targetGameId);
          
          // For sent requests, try to find the owner of the instance
          if (!isReceivedRequest && gameResponse.instances && gameResponse.instances.length > 0) {
            // Look for an instance with matching ID or use the first one available
            const instance = gameResponse.instances.find(inst => inst.id === id) || gameResponse.instances[0];
            if (instance && instance.owner) {
              console.log("Initial load: Found instance owner:", instance.owner);
              // Add instance owner to game details
              gameResponse.instanceOwner = instance.owner;
            }
          }
          
          setGameDetails(gameResponse);
        }
      } catch (err) {
        console.error("Failed to load initial game details:", err);
      }
    };
    
    loadGameDetails();
  }, [requestedGameId, gameId, isReceivedRequest, id]);

  // Update isReturned state when lendingRecord changes
  useEffect(() => {
    if (lendingRecord) {
      setIsReturned(lendingRecord.status === 'CLOSED' || lendingRecord.status === 'Returned');
    }
  }, [lendingRecord]);

  const handleAction = async (status) => {
    if (!id) {
      console.error("Borrow request ID is missing!");
      setError("Cannot process request: ID missing.");
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      // Create a request object with only the status
      // The backend expects just the status for updating
      const requestBody = {
        status: status.toUpperCase()
      };
      
      console.log("[API Request] Updating borrow request:", {
        requestId: id,
        requestBody: requestBody,
        userId: localStorage.getItem('userId'),
        user: user
      });
      
      const response = await actOnBorrowRequest(id, requestBody);
      
      console.log("[API Response] Borrow request update response:", response);
      
      if (refreshRequests) {
        console.log("[UI] Refreshing borrow requests list");
        refreshRequests(); // Refresh the list in the parent component
      }
    } catch (err) {
      console.error("[API Error] Failed to update borrow request:", {
        error: err,
        errorMessage: err.message,
        errorStack: err.stack,
        userId: localStorage.getItem('userId'),
        user: user
      });
      setError(`Failed to ${status.toLowerCase()} request. Please try again.`);
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
        
        // When this is a sent request, try to find the owner of the instance
        // from the game's instances array
        if (!isReceivedRequest && gameResponse.instances && gameResponse.instances.length > 0) {
          // Look for an instance with matching ID (if we have it in the request)
          // Otherwise, just take the first one
          const instance = gameResponse.instances.find(inst => inst.id === id) || gameResponse.instances[0];
          if (instance && instance.owner) {
            console.log("Found instance owner:", instance.owner);
            // Update gameDetails with the owner info 
            setGameDetails(prev => ({
              ...prev,
              instanceOwner: instance.owner
            }));
          }
        }
      }

      // Only check for lending record if the request is approved
      // This avoids 404 errors for pending requests
      if (status === 'APPROVED' || status === 'ACTIVE') {
        try {
          const lendingResponse = await getLendingRecordByRequestId(id);
          console.log("Lending record response:", lendingResponse);
          setLendingRecord(lendingResponse);
          setIsReturned(lendingResponse.status === 'CLOSED' || lendingResponse.status === 'Returned');
        } catch (lendingErr) {
          console.log("No lending record found or not yet processed", lendingErr);
          setLendingRecord(null);
          setIsReturned(false);
        }
      } else {
        // For pending or declined requests, we know there's no lending record yet
        setLendingRecord(null);
        setIsReturned(false);
      }
      
      setShowDetails(true);
    } catch (err) {
      console.error("Failed to load game details:", err);
      setError("Failed to load game details.");
      toast.error("Failed to load game details. Please try again.");
    } finally {
      setDetailsLoading(false);
    }
  };

  const handleSubmitReview = (review) => {
    console.log("Review submitted:", review);
    setShowReviewForm(false);
    toast.success("Review submitted successfully!");
  };
  
  // Handler for navigating to game search page
  const handleGoToGame = () => {
    if (gameDetails?.name) {
      navigate(`/games?q=${encodeURIComponent(gameDetails.name)}`);
    }
  };

  // Helper function to get the display name for the game instance owner
  const getGameInstanceOwnerName = () => {
    if (isReceivedRequest) {
      return requester; // For received requests, show the requester
    } else {
      // For sent requests, show the instance owner if available
      return gameDetails?.instanceOwner?.name || 
             gameDetails?.owner?.name || 
             "Game Owner";
    }
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
                  <span className="font-medium">{isReceivedRequest ? "From:" : "To:"}</span> {getGameInstanceOwnerName()}
                </div>
                <div className="text-sm">
                  <span className="font-medium">Requested on:</span> {date}
                </div>
                <div className="text-sm">
                  <span className="font-medium">End Date:</span> {endDate}
                </div>
              </div>
              
              <div className="flex gap-2 mt-4">
                {/* Only show action buttons for game owners and if it's a received request and status is Pending */}
                {isGameOwner && isReceivedRequest && status === 'PENDING' && (
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
            <DialogDescription>
              View details about your borrow request and its current status.
            </DialogDescription>
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
                    <p><span className="font-medium">Request Status:</span> {status}</p>
                    <p>
                      <span className="font-medium">Return Status:</span>{" "}
                      {isReturned ? (
                        <Badge variant="positive" className="ml-2">Returned</Badge>
                      ) : lendingRecord ? (
                        <Badge variant="outline" className="ml-2">Not Returned</Badge>
                      ) : (
                        <Badge variant="outline" className="ml-2">Not Yet Lent</Badge>
                      )}
                    </p>
                  </div>
                </div>
                {gameDetails.description && (
                  <p className="text-sm text-gray-600">{gameDetails.description}</p>
                )}
                
                {/* Add a Go to Game button */}
                <div className="flex justify-end mt-2">
                  <Button 
                    onClick={handleGoToGame}
                    variant="outline"
                    size="sm"
                    className="flex items-center gap-1"
                  >
                    Go to Game
                  </Button>
                </div>
              </div>
            )}

            {/* Review section - only show for borrowers if game is returned */}
            {canReview && !showReviewForm && (
              <div className="pt-4 border-t">
                <p className="text-sm text-muted-foreground mb-2 italic">
                  You can only review games that you have borrowed and returned.
                </p>
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