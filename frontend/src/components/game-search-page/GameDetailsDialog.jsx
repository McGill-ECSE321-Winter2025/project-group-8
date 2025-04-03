import { useState, useEffect } from "react";
import { DialogContent, DialogDescription, DialogHeader, DialogTitle } from "../../ui/dialog";
import { Button } from "../ui/button";
import { SAMPLE_REVIEWS } from "./data";
import { GameCard } from "./GameCard";

export function GameDetailsDialog({ game, onRequestGame }) {
  const [selectedInstanceIndex, setSelectedInstanceIndex] = useState(0); // Default to first instance
  const [reviews, setReviews] = useState([]);
  
  // Format date for display
  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  // Get all reviews for this game type (by name)
  useEffect(() => {
    if (game.instances) {
      // Get reviews for all instances of this game
      const allReviews = [];
      game.instances.forEach(instance => {
        const instanceReviews = SAMPLE_REVIEWS[instance.id] || [];
        allReviews.push(...instanceReviews);
      });
      
      setReviews(allReviews);
      // Set first instance as selected by default
      setSelectedInstanceIndex(0);
    } else {
      // Single game instance
      setReviews(SAMPLE_REVIEWS[game.id] || []);
    }
  }, [game]);

  // Handle requesting a game
  const handleRequestGame = (instanceIndex) => {
    const gameInstance = game.instances[instanceIndex];
    onRequestGame(gameInstance);
  };

  return (
    <DialogContent className="sm:max-w-3xl max-h-[90vh] overflow-y-auto">
      <DialogHeader>
        <DialogTitle className="text-2xl">{game.name}</DialogTitle>
        <DialogDescription>{game.category} • {game.minPlayers}-{game.maxPlayers} players</DialogDescription>
      </DialogHeader>
      
      <div className="mt-4 space-y-6">
        {/* Image and basic info shown for both grouped and single games */}
        <div>
          <h3 className="text-lg font-medium mb-3">Game Details</h3>
          <div className="border rounded-lg p-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <img src={game.image} alt={game.name} className="w-full h-64 rounded-lg object-cover" />
              </div>
              <div className="space-y-3">
                <div>
                  <span className="font-medium">Players:</span> {game.minPlayers} - {game.maxPlayers}
                </div>
                <div>
                  <span className="font-medium">Category:</span> {game.category}
                </div>
                {!game.instances && (
                  <>
                    <div>
                      <span className="font-medium">Owner:</span> {game.owner.name}
                    </div>
                    <div>
                      <span className="font-medium">Contact:</span> {game.owner.email}
                    </div>
                    <div>
                      <span className="font-medium">Date Added:</span> {formatDate(game.dateAdded)}
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>
        
        {/* Show available copies if this is a grouped game */}
        {game.instances && (
          <div>
            <h3 className="text-lg font-medium mb-3">Available Copies ({game.instances.length})</h3>
            <div className="space-y-4">
              {game.instances.map((instance, index) => (
                <div 
                  key={instance.id} 
                  className="border rounded-lg p-4"
                >
                  <div className="flex justify-between items-center">
                    <div>
                      <h4 className="font-medium">{instance.owner.name}</h4>
                      <p className="text-sm text-muted-foreground">Added: {formatDate(instance.dateAdded)}</p>
                      <p className="text-sm text-muted-foreground">Contact: {instance.owner.email}</p>
                    </div>
                    <Button 
                      onClick={() => handleRequestGame(index)}
                    >
                      Request
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
        
        {/* For a single game instance, show the request button */}
        {!game.instances && (
          <div className="flex justify-end">
            <Button onClick={() => onRequestGame(game)}>
              Request This Game
            </Button>
          </div>
        )}
        
        {/* Reviews section */}
        {reviews.length > 0 && (
          <div>
            <h3 className="text-lg font-medium mb-3">Reviews</h3>
            <div className="space-y-3">
              {reviews.map(review => (
                <div key={review.id} className="border rounded-lg p-4">
                  <div className="flex justify-between">
                    <span className="font-medium">{review.reviewer.name}</span>
                    <span>Rating: {review.rating}/5</span>
                  </div>
                  <p className="mt-2">{review.comment}</p>
                  <p className="text-sm text-muted-foreground mt-1">Posted: {formatDate(review.dateSubmitted)}</p>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </DialogContent>
  );
} 