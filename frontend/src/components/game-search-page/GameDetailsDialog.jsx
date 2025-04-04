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
import { Users, Calendar, Star, MessageSquare, User } from 'lucide-react';
import Tag from '../common/Tag.jsx';
import GameOwnerTag from '../common/GameOwnerTag.jsx';
import { useSearchParams } from 'react-router-dom';

// Demo game instances data - representing physical copies owned by different people
const generateGameInstances = (gameId) => [
  {
    id: `${gameId}-copy-1`,
    name: gameId === 'game-1' ? "Monopoly" : gameId === 'game-2' ? "Settlers of Catan" : "Game Copy",
    available: true,
    dateAdded: "2023-04-15",
    owner: {
      id: "user-1",
      name: "GameMaster123",
      email: "gamemaster@example.com"
    }
  },
  {
    id: `${gameId}-copy-2`,
    name: gameId === 'game-1' ? "Monopoly" : gameId === 'game-2' ? "Settlers of Catan" : "Game Copy",
    available: true,
    dateAdded: "2023-06-22",
    owner: {
      id: "user-2",
      name: "StrategyQueen",
      email: "strategyqueen@example.com"
    }
  },
  {
    id: `${gameId}-copy-3`,
    name: gameId === 'game-1' ? "Monopoly" : gameId === 'game-2' ? "Settlers of Catan" : "Game Copy",
    available: true,
    dateAdded: "2023-09-08",
    owner: {
      id: "user-3",
      name: "BoardGameFan42",
      email: "boardgamefan@example.com"
    }
  }
];

// Demo reviews data based on ReviewResponseDto
const generateGameReviews = (gameId) => [
  {
    id: 1,
    rating: 5,
    comment: "Absolutely fantastic game! Easy to learn but offers deep strategy. Perfect for both casual and serious gamers.",
    dateSubmitted: "2023-05-15",
    gameId: gameId,
    gameTitle: gameId === 'game-1' ? "Monopoly" : gameId === 'game-2' ? "Settlers of Catan" : "Game",
    reviewer: {
      id: "user-4",
      name: "StrategyMaster",
      email: "strategymaster@example.com"
    }
  },
  {
    id: 2,
    rating: 4,
    comment: "Really enjoyable game with friends. The only downside is the setup time, but the gameplay is worth it.",
    dateSubmitted: "2023-06-20",
    gameId: gameId,
    gameTitle: gameId === 'game-1' ? "Monopoly" : gameId === 'game-2' ? "Settlers of Catan" : "Game",
    reviewer: {
      id: "user-5",
      name: "CasualGamer42",
      email: "casualgamer@example.com"
    }
  },
  {
    id: 3,
    rating: 3,
    comment: "Good game overall. Rules can be a bit confusing at first, but gets better with repeat plays.",
    dateSubmitted: "2023-07-12",
    gameId: gameId,
    gameTitle: gameId === 'game-1' ? "Monopoly" : gameId === 'game-2' ? "Settlers of Catan" : "Game",
    reviewer: {
      id: "user-6",
      name: "BoardGameEnthusiast",
      email: "boardgameenthusiast@example.com"
    }
  }
];

export const GameDetailsDialog = ({ game, onRequestGame }) => {
  const [searchParams] = useSearchParams();
  const fromUserId = searchParams.get('fromUser');
  const [showInstancesDialog, setShowInstancesDialog] = useState(false);
  const [selectedInstance, setSelectedInstance] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [averageRating, setAverageRating] = useState(0);
  
  if (!game) return null;
  
  // Generate instances for this game
  const gameInstances = generateGameInstances(game.id);
  
  // Load reviews when game changes
  useEffect(() => {
    if (game?.id) {
      // In a real implementation, this would be an API call:
      // fetch(`/api/v1/games/${game.id}/reviews`)
      //  .then(response => response.json())
      //  .then(data => setReviews(data));
      
      const gameReviews = generateGameReviews(game.id);
      setReviews(gameReviews);
      
      // Calculate average rating
      if (gameReviews.length > 0) {
        const ratingSum = gameReviews.reduce((sum, review) => sum + review.rating, 0);
        setAverageRating((ratingSum / gameReviews.length).toFixed(1));
      }
    }
  }, [game]);
  
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
    <>
    <DialogContent className="sm:max-w-[95%] md:max-w-[90%] lg:max-w-[80%] xl:max-w-6xl overflow-y-auto max-h-[90vh]">
      <DialogHeader>
        <div className="flex flex-col sm:flex-row sm:items-start gap-4">
          <div className="w-full sm:w-1/4 flex-shrink-0">
            <img 
              src={game.imageUrl || game.image} 
              alt={game.name} 
              className="w-full h-auto rounded-md aspect-[4/3] object-cover"
            />
          </div>
          <div className="flex-1">
            <DialogTitle className="text-2xl mb-2">{game.name}</DialogTitle>
            <div className="mb-2 flex items-center gap-2">
              <Tag text={game.category} variant="primary" fromUserId={fromUserId} />
              {reviews.length > 0 && (
                <div className="flex items-center gap-1 text-sm">
                  <span className="flex items-center">
                    <Star className="h-4 w-4 fill-yellow-400 text-yellow-400 mr-1" />
                    {averageRating}
                  </span>
                  <span className="text-muted-foreground">({reviews.length} reviews)</span>
                </div>
              )}
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
            </div>
          </div>
          
          {/* Reviews Section */}
          <div className="space-y-3">
            <h4 className="text-sm font-medium flex items-center gap-2">
              <MessageSquare className="h-4 w-4" />
              Reviews
            </h4>
            
            {reviews.length > 0 ? (
              <div className="space-y-4">
                {reviews.map(review => (
                  <div key={review.id} className="border rounded-md p-3">
                    <div className="flex justify-between items-start">
                      <div>
                        <div className="flex items-center gap-2">
                          <div className="font-medium flex items-center gap-1">
                            <User className="h-4 w-4 text-muted-foreground" />
                            {review.reviewer.name}
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
          
          {/* Similar Games */}
          <div className="space-y-2">
            <h4 className="text-sm font-medium">You might also like</h4>
            <div className="flex flex-wrap gap-2">
              <Tag text="Pandemic" variant="primary" interactive searchable fromUserId={fromUserId} />
              <Tag text="Carcassonne" variant="primary" interactive searchable fromUserId={fromUserId} />
              <Tag text="Ticket to Ride" variant="primary" interactive searchable fromUserId={fromUserId} />
            </div>
          </div>
        </div>
        
        {/* Right Column: Game Instances and Borrowing */}
        <div className="border-t md:border-t-0 md:border-l pt-6 md:pt-0 md:pl-6 space-y-4">
          <div>
            <h4 className="text-sm font-medium mb-3">Available Copies</h4>
            
            <div className="space-y-4 mb-4">
              {gameInstances.map(instance => (
                <div 
                  key={instance.id}
                  onClick={() => setSelectedInstance(instance)}
                  className={`
                    border rounded-md p-3 cursor-pointer transition-colors
                    ${selectedInstance?.id === instance.id 
                      ? 'border-primary bg-primary/5' 
                      : 'hover:border-primary/50'}
                  `}
                >
                  <div className="flex justify-between items-center">
                    <h4 className="font-medium">Owner: {instance.owner.name}</h4>
                    {selectedInstance?.id === instance.id && (
                      <svg className="h-4 w-4 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                    )}
                  </div>
                  <div className="flex items-center gap-1 mt-1 text-sm">
                    <span className="text-muted-foreground">{instance.available ? 'Available for borrowing' : 'Currently unavailable'}</span>
                  </div>
                  <div className="text-xs text-muted-foreground mt-1">
                    Added: {formatDate(instance.dateAdded)}
                  </div>
                </div>
              ))}
            </div>
            
            <div className="mt-6">
              <Button 
                className="w-full mb-2" 
                onClick={handleRequestWithInstance} 
                disabled={!selectedInstance}
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
                {gameInstances.length} copies available from different owners
              </div>
              {selectedInstance && (
                <div className="flex items-center gap-2 mt-2 p-2 rounded bg-primary/10">
                  <span className="font-medium">Selected: Copy from {selectedInstance.owner.name}</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </DialogContent>
    </>
  );
}; 