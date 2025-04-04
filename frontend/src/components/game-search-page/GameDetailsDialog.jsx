import React, { useState } from 'react';
import {
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  Dialog,
} from '../ui/dialog';
import { Button } from '../ui/button';
import { Users, Calendar } from 'lucide-react';
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

export const GameDetailsDialog = ({ game, onRequestGame }) => {
  const [searchParams] = useSearchParams();
  const fromUserId = searchParams.get('fromUser');
  const [showInstancesDialog, setShowInstancesDialog] = useState(false);
  const [selectedInstance, setSelectedInstance] = useState(null);
  
  if (!game) return null;
  
  // Generate instances for this game
  const gameInstances = generateGameInstances(game.id);
  
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

  return (
    <>
    <DialogContent className="sm:max-w-md md:max-w-lg">
      <DialogHeader>
        <div className="flex flex-col sm:flex-row sm:items-start gap-4">
          <div className="w-full sm:w-1/3 flex-shrink-0">
            <img 
              src={game.imageUrl || game.image} 
              alt={game.name} 
              className="w-full h-auto rounded-md aspect-[4/3] object-cover"
            />
          </div>
          <div className="flex-1">
            <DialogTitle className="text-2xl mb-2">{game.name}</DialogTitle>
            <div className="mb-2">
              <Tag text={game.category} variant="secondary" searchable fromUserId={fromUserId} />
            </div>
            <DialogDescription className="text-sm">
              {game.description || "No description available"}
            </DialogDescription>
          </div>
        </div>
      </DialogHeader>
      
      <div className="space-y-4 my-4">
        <div className="grid grid-cols-2 gap-4">
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
          
          {/* Availability */}
          <div className="space-y-2">
            <h4 className="text-sm font-medium">Availability</h4>
            <div className="space-y-1 text-sm">
              <div className="flex items-center gap-2">
                <Calendar className="h-4 w-4 text-muted-foreground" />
                  <span>Available for borrowing</span>
              </div>
              <div className="text-green-600 font-medium">
                  {gameInstances.length} copies available from different owners
                </div>
                {selectedInstance && (
                  <div className="flex items-center gap-2 mt-2 bg-muted/50 p-2 rounded">
                    <span className="font-medium">Selected: Copy from {selectedInstance.owner.name}</span>
                  </div>
                )}
            </div>
          </div>
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
      
      <DialogFooter className="sm:justify-start gap-2">
          <Button 
            className="sm:flex-1" 
            onClick={handleRequestWithInstance} 
            disabled={!selectedInstance}
          >
            {selectedInstance ? "Request to Borrow" : "Select a copy first"}
          </Button>
          <Button 
            variant="outline" 
            className="sm:flex-1"
            onClick={() => setShowInstancesDialog(true)}
          >
            View Available Copies
          </Button>
        </DialogFooter>
      </DialogContent>

      {/* Game Instances Dialog */}
      <Dialog open={showInstancesDialog} onOpenChange={setShowInstancesDialog}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Available Copies of {game.name}</DialogTitle>
            <DialogDescription>
              Select one of the available copies to request from an owner
            </DialogDescription>
          </DialogHeader>
          
          <div className="space-y-4 my-2">
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
          
          <DialogFooter className="gap-2 mt-2">
            <Button 
              onClick={() => {
                setShowInstancesDialog(false);
              }}
              disabled={!selectedInstance}
            >
              {selectedInstance ? "Continue with Selected" : "Select a Copy"}
        </Button>
            <Button 
              type="button" 
              variant="outline" 
              onClick={() => {
                setShowInstancesDialog(false);
              }}
            >
              Cancel
        </Button>
      </DialogFooter>
    </DialogContent>
      </Dialog>
    </>
  );
}; 