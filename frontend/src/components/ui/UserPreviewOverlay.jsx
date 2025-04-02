import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DialogTrigger, // Keep for potential future use, even if not used now
} from '/src/components/ui/dialog.jsx'; // Adjusted path based on shadcn/ui setup
import { Button } from '/src/components/ui/button.jsx'; // Adjusted path
import { Avatar, AvatarFallback, AvatarImage } from './avatar.jsx'; // Adjusted path
import Tag from '../common/Tag.jsx'; // Assuming Tag is in the same directory

const UserPreviewOverlay = ({ user, isOpen, onClose }) => {
  const navigate = useNavigate();

  const handleGameClick = (gameName) => {
    // Navigate to /games and pass gameName as search query param
    navigate(`/games?search=${encodeURIComponent(gameName)}`);

  };

  if (!user) return null;
  const currentUserGames = ['Monopoly', 'Settlers of Catan', 'Ticket to Ride']; // Example games
  const commonGames = user?.gamesPlayed?.filter(game => currentUserGames.includes(game)) || [];

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <div className="flex items-center space-x-4">
            <Avatar className="h-16 w-16"> {/* Larger Avatar */}
              <AvatarImage src={user.avatarUrl} alt={user.username} />
              <AvatarFallback className="text-xl">{user.username ? user.username[0].toUpperCase() : 'U'}</AvatarFallback>
            </Avatar>
            <div>
              <DialogTitle className="text-lg">{user.username || 'User'}</DialogTitle>
              {user.isGameOwner && <Tag text="Game Owner" className="mt-1" />}
            </div>
          </div>
        </DialogHeader>
        <div className="py-4">
          {/* Placeholder for Bio/Tags */}
          <p className="text-sm text-muted-foreground mb-4">User bio goes here...</p>
          {commonGames.length > 0 && (
            <div>
              <h4 className="text-sm font-medium mb-2">Games Played in Common:</h4>
              <div className="flex flex-wrap gap-2"> {/* Replaced ul with div for wrapping */}
                {commonGames.map((game, index) => (
                  <button
                    key={index}
                    onClick={() => handleGameClick(game)}
                    className="focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 rounded-full" // Add focus styles
                  >
                    <Tag text={game} />
                  </button>
                ))}
              </div>
            </div>
          )}

        </div>
        <DialogFooter className="gap-2"> {/* Added gap for spacing */}
          <Button
            variant="outline"
            onClick={() => console.log('View Profile clicked for:', user.id)}
          >
            View Full Profile
          </Button>
          <Button onClick={() => console.log('Friend Request sent to:', user.id)}>
            Send Friend Request
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default UserPreviewOverlay;