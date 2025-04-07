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
import { Users, Calendar, Star, MessageSquare, User, Loader2, PenSquare } from 'lucide-react';
import Tag from '../common/Tag.jsx';
import GameOwnerTag from '../common/GameOwnerTag.jsx';
import { useSearchParams } from 'react-router-dom';
import { getGameReviews } from '../../service/game-api.js';
import { useAuth } from '../../context/AuthContext.jsx';
import ReviewForm from './ReviewForm.jsx';
import { toast } from 'sonner';
import { checkUserCanReviewGame } from '../../service/dashboard-api.js';
import { Tooltip, TooltipTrigger, TopTooltipContent } from '../ui/tooltip.jsx';

export const GameDetailsDialog = ({ game, onRequestGame }) => {
  const [searchParams] = useSearchParams();
  const { user, isAuthenticated } = useAuth();
  const fromUserId = searchParams.get('fromUser');
  const [selectedInstance, setSelectedInstance] = useState(null);
  const [instances, setInstances] = useState([]);
  const [isLoadingInstances, setIsLoadingInstances] = useState(false);
  const [instancesError, setInstancesError] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [isLoadingReviews, setIsLoadingReviews] = useState(false);
  const [reviewsError, setReviewsError] = useState(null);
  const [averageRating, setAverageRating] = useState(0);
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [editingReview, setEditingReview] = useState(null);
  const [userCanReview, setUserCanReview] = useState(false);
  const [isCheckingReviewEligibility, setIsCheckingReviewEligibility] = useState(false);
  
  if (!game) return null;
  
  // Check if the current user has already reviewed this game
  const userReview = isAuthenticated && user ? reviews.find(review => 
    review.reviewer && review.reviewer.email === user.email
  ) : null;
  
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

  // Check if the user can review this game (has borrowed and returned it)
  useEffect(() => {
    if (isAuthenticated && user && game?.id) {
      const checkReviewEligibility = async () => {
        setIsCheckingReviewEligibility(true);
        try {
          const canReview = await checkUserCanReviewGame(game.id);
          setUserCanReview(canReview);
        } catch (error) {
          console.error("Failed to check if user can review game:", error);
          setUserCanReview(false);
        } finally {
          setIsCheckingReviewEligibility(false);
        }
      };
      
      checkReviewEligibility();
    } else {
      setUserCanReview(false);
    }
  }, [isAuthenticated, user, game?.id]);

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
  
  // Handle adding a new review or editing an existing review
  const handleAddReview = () => {
    setEditingReview(null);
    setShowReviewForm(true);
  };
  
  // Handle editing the user's existing review
  const handleEditReview = (review) => {
    setEditingReview(review);
    setShowReviewForm(true);
  };
  
  // Handle submission of a review (new or edited)
  const handleReviewSubmitted = (newReview, isDeleted = false) => {
    setShowReviewForm(false);
    setEditingReview(null);
    
    if (isDeleted) {
      // Remove the deleted review from the list
      setReviews(reviews.filter(review => review.id !== editingReview.id));
      
      // Recalculate average rating
      if (reviews.length > 1) {
        const remainingReviews = reviews.filter(review => review.id !== editingReview.id);
        const ratingSum = remainingReviews.reduce((sum, review) => sum + review.rating, 0);
        setAverageRating((ratingSum / remainingReviews.length).toFixed(1));
      } else {
        setAverageRating(0);
      }
    } else if (newReview) {
      if (editingReview) {
        // Update existing review in the list
        setReviews(reviews.map(review => 
          review.id === newReview.id ? newReview : review
        ));
      } else {
        // Add new review to the list
        setReviews([...reviews, newReview]);
      }
      
      // Recalculate average rating
      const updatedReviews = editingReview 
        ? reviews.map(review => review.id === newReview.id ? newReview : review)
        : [...reviews, newReview];
      
      const ratingSum = updatedReviews.reduce((sum, review) => sum + review.rating, 0);
      setAverageRating((ratingSum / updatedReviews.length).toFixed(1));
    }
  };
  
  // Cancel review form
  const handleCancelReview = () => {
    setShowReviewForm(false);
    setEditingReview(null);
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
            <div className="flex justify-between items-center">
              <h4 className="text-sm font-medium flex items-center gap-2">
                <MessageSquare className="h-4 w-4" /> Reviews
                {isLoadingReviews && <Loader2 className="h-4 w-4 animate-spin ml-2" />}
              </h4>
              
              {isAuthenticated && !showReviewForm && (
                userReview || userCanReview ? (
                  <Button 
                    variant="outline" 
                    size="sm" 
                    onClick={userReview ? () => handleEditReview(userReview) : handleAddReview}
                    className="flex items-center gap-1"
                    disabled={isCheckingReviewEligibility}
                  >
                    <PenSquare className="h-3 w-3" />
                    {userReview ? "Edit Review" : "Add Review"}
                    {isCheckingReviewEligibility && <Loader2 className="h-3 w-3 animate-spin ml-1" />}
                  </Button>
                ) : (
                  <div className="relative group">
                    <Button
                      variant="outline"
                      size="sm"
                      disabled
                      className="flex items-center gap-1 cursor-not-allowed"
                    >
                      <PenSquare className="h-3 w-3" />
                      Add Review
                    </Button>
                    <div 
                      className="absolute -left-5 -top-18 w-38 p-2 bg-popover border rounded-md shadow-md text-xs text-foreground text-center
                      opacity-0 group-hover:opacity-100 transition-opacity duration-200"
                    >
                      You can only review games that you have borrowed and returned
                    </div>
                  </div>
                )
              )}
            </div>
            
            {/* Review Form */}
            {showReviewForm && (
              <ReviewForm
                gameId={game.id}
                existingReview={editingReview}
                onReviewSubmitted={handleReviewSubmitted}
                onCancel={handleCancelReview}
              />
            )}
            
            {/* Reviews List */}
            {!showReviewForm && (
              isLoadingReviews ? (
                <div className="flex justify-center items-center h-20">
                  <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
                </div>
              ) : reviewsError ? (
                <p className="text-sm text-red-500">{reviewsError}</p>
              ) : reviews.length > 0 ? (
                <div className="space-y-4 max-h-60 overflow-y-auto pr-2">
                  {reviews.map(review => {
                    const isUserReview = isAuthenticated && user && review.reviewer?.email === user.email;
                    
                    return (
                      <div key={review.id} className={`border rounded-md p-3 ${isUserReview ? 'border-primary/50 bg-primary/5' : ''}`}>
                        <div className="flex justify-between items-start">
                          <div>
                            <div className="flex items-center gap-2">
                              <div className="font-medium flex items-center gap-1">
                                <User className="h-4 w-4 text-muted-foreground" />
                                {review.reviewer?.name || 'Anonymous'}
                                {isUserReview && <span className="text-xs bg-primary/20 text-primary px-1.5 py-0.5 rounded-full">You</span>}
                              </div>
                              <div className="text-xs text-muted-foreground">
                                {formatDate(review.dateSubmitted)}
                              </div>
                            </div>
                            <div className="mt-1">
                              {renderStars(review.rating)}
                            </div>
                          </div>
                          
                          {isUserReview && !showReviewForm && (
                            <Button 
                              variant="ghost" 
                              size="sm" 
                              onClick={() => handleEditReview(review)}
                              className="h-8 w-8 p-0"
                            >
                              <PenSquare className="h-4 w-4" />
                              <span className="sr-only">Edit</span>
                            </Button>
                          )}
                        </div>
                        {review.comment && (
                          <p className="text-sm mt-2">{review.comment}</p>
                        )}
                      </div>
                    );
                  })}
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">No reviews yet. Be the first to review this game!</p>
              )
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
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path>
                        </svg>
                      )}
                    </div>
                    <div className="mt-2 text-sm flex flex-col gap-1">
                      {instance.condition && (
                        <div>
                          <span className="text-muted-foreground">Condition:</span> {instance.condition}
                        </div>
                      )}
                      {instance.location && (
                        <div>
                          <span className="text-muted-foreground">Location:</span> {instance.location}
                        </div>
                      )}
                      <div>
                        <span className="text-muted-foreground">Status:</span>{' '}
                        <span className={instance.available ? 'text-green-600' : 'text-red-500'}>
                          {instance.available ? 'Available' : 'Not Available'}
                        </span>
                      </div>
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-sm text-muted-foreground">No copies available.</p>
              )}
            </div>
            
            {isAuthenticated && instances.some(instance => instance.available) && (
              <Button 
                onClick={handleRequestWithInstance} 
                disabled={!selectedInstance}
                className="w-full"
              >
                Request Selected Copy
              </Button>
            )}
            
            {!isAuthenticated && (
              <p className="text-sm text-muted-foreground text-center">
                Please log in to request a copy of this game.
              </p>
            )}
          </div>
        </div>
      </div>
    </DialogContent>
  );
}; 