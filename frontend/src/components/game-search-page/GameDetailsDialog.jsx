import React from 'react';
import {
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '../ui/dialog';
import { Button } from '../ui/button';
import { Users, Clock, Gauge, Calendar, User } from 'lucide-react';
import Tag from '../common/Tag.jsx';
import GameOwnerTag from '../common/GameOwnerTag.jsx';
import { useSearchParams } from 'react-router-dom';

export const GameDetailsDialog = ({ game, onRequestGame }) => {
  const [searchParams] = useSearchParams();
  const fromUserId = searchParams.get('fromUser');
  
  if (!game) return null;

  return (
    <DialogContent className="sm:max-w-md md:max-w-lg">
      <DialogHeader>
        <div className="flex flex-col sm:flex-row sm:items-start gap-4">
          <div className="w-full sm:w-1/3 flex-shrink-0">
            <img 
              src={game.imageUrl} 
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
              {game.description}
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
              <div className="flex items-center gap-2">
                <Clock className="h-4 w-4 text-muted-foreground" />
                <span>{game.playTime}</span>
              </div>
              <div className="flex items-center gap-2">
                <Gauge className="h-4 w-4 text-muted-foreground" />
                <span>Complexity: {game.complexity}</span>
              </div>
              <div className="flex items-center gap-2">
                <User className="h-4 w-4 text-muted-foreground" />
                <span>Owner: GameMaster123</span>
              </div>
            </div>
          </div>
          
          {/* Availability */}
          <div className="space-y-2">
            <h4 className="text-sm font-medium">Availability</h4>
            <div className="space-y-1 text-sm">
              <div className="flex items-center gap-2">
                <Calendar className="h-4 w-4 text-muted-foreground" />
                <span>Available for events</span>
              </div>
              <div className="text-green-600 font-medium">
                3 instances available
              </div>
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
        <Button className="sm:flex-1" onClick={() => onRequestGame(game)}>
          Request to Play
        </Button>
        <Button variant="outline" className="sm:flex-1">
          See All Instances
        </Button>
      </DialogFooter>
    </DialogContent>
  );
}; 