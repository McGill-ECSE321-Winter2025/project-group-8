import React, { useState, useEffect } from 'react';
import {
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  Dialog,
} from '../ui/dialog';
import { Button } from '../ui/button';
import { Users, Calendar, Star, MessageSquare, User, Loader2 } from 'lucide-react';
import Tag from '../common/Tag.jsx';
import GameOwnerTag from '../common/GameOwnerTag.jsx';
import { useSearchParams } from 'react-router-dom';
import { getGameReviews } from '../../service/game-api.js';

export const GameDetailsDialog = ({ game, onRequestGame }) => {
  const [searchParams] = useSearchParams();
  const fromUserId = searchParams.get('fromUser');
  const [selectedInstance, setSelectedInstance] = useState(null);
  const [instances, setInstances] = useState([]);
  const [isLoadingInstances, setIsLoadingInstances] = useState(false);
  const [instancesError, setInstancesError] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [isLoadingReviews, setIsLoadingReviews] = useState(false);
  const [reviewsError, setReviewsError] = useState(null);
  const [averageRating, setAverageRating] = useState(0);
  
  if (!game) return null;
  
  // Fetch reviews when game changes
  useEffect(() => {
    if (game?.id) {
      const fetchReviews = async () => {
        setIsLoadingReviews(true);
        setReviewsError(null);
        setReviews([]);
        setAverageRating(0);
        
        try {
          const fetchedReviews = await getGameReviews(game.id);
          setReviews(fetchedReviews);
          
          // Calculate average rating
          if (fetchedReviews.length > 0) {
            const ratingSum = fetchedReviews.reduce((sum, review) => sum + review.rating, 0);
            setAverageRating((ratingSum / fetchedReviews.length).toFixed(1));
          }
        } catch (error) {
          console.error("Failed to fetch game reviews:", error);
          setReviewsError("Could not load reviews.");
        } finally {
          setIsLoadingReviews(false);
        }
      };
      fetchReviews();
    }
  }, [game?.id]);

  // Use instances from the game object instead of fetching them again
  useEffect(() => {
    setIsLoadingInstances(true);
    setInstancesError(null);
    
    try {
      // Use the instances passed from the parent component
      if (game?.instances && Array.isArray(game.instances)) {
        setInstances(game.instances);
      } else {
        setInstances([]);
      }
      setIsLoadingInstances(false);
    } catch (error) {
      console.error("Error processing game instances:", error);
      setInstancesError("Could not load available copies.");
      setIsLoadingInstances(false);
    }
    
    // Clear selected instance when game changes
    setSelectedInstance(null);
  }, [game?.id, game?.instances]);
  
  const handleRequestWithInstance = () => {
    onRequestGame(game, selectedInstance);
  };

  // Format date to a more readable format
  const formatDate = (dateString) => {
    if (!dateString) return "Unknown";
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  // Render star rating
  const renderStars = (rating) => {
    return (
      <div className="flex items-center">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star 
            key={star} 
            className={`h-4 w-4 ${star <= rating ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'}`} 
          />
        ))}
      </div>
    );
  };

  return (
    <DialogContent className="sm:max-w-[95%] md:max-w-[90%] lg:max-w-[80%] xl:max-w-6xl overflow-y-auto max-h-[90vh]">
      <DialogHeader>
        <div className="flex flex-col sm:flex-row sm:items-start gap-4">
          <div className="w-full sm:w-1/4 flex-shrink-0">
            <img 
              src={game.image || 'https://placehold.co/400x300/e9e9e9/1d1d1d?text=No+Image'} 
              alt={game.name} 
              className="w-full h-auto rounded-md aspect-[4/3] object-cover"
            />
          </div>
          <div className="flex-1">
            <DialogTitle className="text-2xl mb-2">{game.name}</DialogTitle>
            <div className="mb-2 flex items-center gap-2">
              <Tag text={game.category} variant="primary" fromUserId={fromUserId} />
              {!isLoadingReviews && !reviewsError && reviews.length > 0 && (
                <div className="flex items-center gap-1 text-sm">
                  <span className="flex items-center">
                    <Star className="h-4 w-4 fill-yellow-400 text-yellow-400 mr-1" />
                    {averageRating}
                  </span>
                  <span className="text-muted-foreground">({reviews.length} reviews)</span>
                </div>
              )}
              {isLoadingReviews && <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />}
              {reviewsError && <span className="text-xs text-red-500">Error loading reviews</span>}
            </div>
            <DialogDescription className="text-sm">
              {game.description || "No description available"}
            </DialogDescription>
          </div>
        </div>
      </DialogHeader>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 my-4">
        {/* Left Column: Game Details and Reviews */}
        <div className="space-y-6">
          {/* Game Specs */}
          <div className="space-y-2">
            <h4 className="text-sm font-medium">Game Details</h4>
            <div className="space-y-1 text-sm">
              <div className="flex items-center gap-2">
                <Users className="h-4 w-4 text-muted-foreground" />
                <span>
                  {game.minPlayers === game.maxPlayers 
                    ? `${game.minPlayers} players` 
                    : `${game.minPlayers}-${game.maxPlayers} players`}
                </span>
              </div>
              {game.owner && (
                <div className="flex items-center gap-2 mt-2">
                  <User className="h-4 w-4 text-muted-foreground" />
                  <span>Owner: {game.owner.name}</span>
                </div>
              )}
              {game.dateAdded && (
                <div className="flex items-center gap-2 mt-2">
                  <Calendar className="h-4 w-4 text-muted-foreground" />
                  <span>Added: {formatDate(game.dateAdded)}</span>
                </div>
              )}
            </div>
          </div>
          
          {/* Reviews Section */}
          <div className="space-y-3">
            <h4 className="text-sm font-medium flex items-center gap-2">
              <MessageSquare className="h-4 w-4" /> Reviews
              {isLoadingReviews && <Loader2 className="h-4 w-4 animate-spin ml-2" />}
            </h4>
            
            {isLoadingReviews ? (
              <div className="flex justify-center items-center h-20">
                <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
              </div>
            ) : reviewsError ? (
              <p className="text-sm text-red-500">{reviewsError}</p>
            ) : reviews.length > 0 ? (
              <div className="space-y-4 max-h-60 overflow-y-auto pr-2">
                {reviews.map(review => (
                  <div key={review.id} className="border rounded-md p-3">
                    <div className="flex justify-between items-start">
                      <div>
                        <div className="flex items-center gap-2">
                          <div className="font-medium flex items-center gap-1">
                            <User className="h-4 w-4 text-muted-foreground" />
                            {review.reviewer?.name || 'Anonymous'}
                          </div>
                          <div className="text-xs text-muted-foreground">
                            {formatDate(review.dateSubmitted)}
                          </div>
                        </div>
                        <div className="mt-1">
                          {renderStars(review.rating)}
                        </div>
                      </div>
                    </div>
                    {review.comment && (
                      <p className="text-sm mt-2">{review.comment}</p>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">No reviews yet.</p>
            )}
          </div>
        </div>
        
        {/* Right Column: Game Instances and Borrowing */}
        <div className="border-t md:border-t-0 md:border-l pt-6 md:pt-0 md:pl-6 space-y-4">
          <div>
            <h4 className="text-sm font-medium mb-3">Available Copies</h4>
            
            <div className="space-y-4 mb-4 max-h-60 overflow-y-auto pr-2">
              {isLoadingInstances ? (
                <div className="flex justify-center items-center h-20">
                  <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                </div>
              ) : instancesError ? (
                <p className="text-sm text-red-500">{instancesError}</p>
              ) : instances.length > 0 ? (
                instances.map(instance => (
                  <div
                    key={instance.id}
                    onClick={() => instance.available && setSelectedInstance(instance)}
                    className={`
                      border rounded-md p-3 transition-colors
                      ${selectedInstance?.id === instance.id
                        ? 'border-primary bg-primary/5 cursor-pointer'
                        : instance.available 
                          ? 'hover:border-primary/50 cursor-pointer' 
                          : 'opacity-60 cursor-not-allowed'}
                    `}
                  >
                    <div className="flex justify-between items-center">
                      <h4 className="font-medium">Owner: {instance.owner?.name || 'Unknown Owner'}</h4>
                      {selectedInstance?.id === instance.id && (
                        <svg className="h-4 w-4 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                      )}
                    </div>
                    <div className="flex items-center gap-1 mt-1 text-sm">
                      <span className={`font-medium ${instance.available ? 'text-green-600' : 'text-orange-600'}`}>
                        {instance.available ? 'Available for borrowing' : 'Currently unavailable'}
                      </span>
                    </div>
                    {instance.condition && (
                      <div className="text-xs mt-1">
                        Condition: {instance.condition}
                      </div>
                    )}
                    {instance.acquiredDate && (
                      <div className="text-xs text-muted-foreground mt-1">
                        Added: {formatDate(instance.acquiredDate)}
                      </div>
                    )}
                  </div>
                ))
              ) : (
                <p className="text-sm text-muted-foreground">No copies found for this game.</p>
              )}
            </div>
            
            <div className="mt-6">
              <Button 
                className="w-full mb-2" 
                onClick={handleRequestWithInstance} 
                disabled={!selectedInstance || !selectedInstance.available || isLoadingInstances}
              >
                {selectedInstance ? "Request to Borrow" : "Select a copy first"}
              </Button>
            </div>
          </div>
          
          {/* Availability Summary */}
          <div className="bg-muted/50 p-4 rounded-md">
            <h4 className="text-sm font-medium mb-2">Availability Summary</h4>
            <div className="space-y-1 text-sm">
              <div className="flex items-center gap-2">
                <Calendar className="h-4 w-4 text-muted-foreground" />
                <span>Available for borrowing</span>
              </div>
              <div className="text-green-600 font-medium">
                {isLoadingInstances ? 'Loading...' 
                  : instancesError ? 'Error loading copies' 
                  : `${instances.filter(i => i.available).length} of ${instances.length} ${instances.length === 1 ? 'copy' : 'copies'} available`}
              </div>
              {selectedInstance && (
                <div className="flex items-center gap-2 mt-2 p-2 rounded bg-primary/10">
                  <span className="font-medium">Selected: Copy from {selectedInstance.owner?.name || 'Unknown'}</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </DialogContent>
  );
}; 