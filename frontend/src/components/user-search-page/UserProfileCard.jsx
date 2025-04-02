import React from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../../../src/components/ui/card.jsx';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '../../../src/components/ui/avatar.jsx';
import Tag from '../common/Tag.jsx';
import { motion } from 'framer-motion'; // Add framer-motion for animations
import { cn } from '../../../src/lib/utils';
import { formatJoinDate } from '../../../src/lib/dateUtils';
import { Calendar } from 'lucide-react';

const UserProfileCard = ({ user, onClick }) => {
  // Provide default values or placeholders if user data might be missing
  const username = user?.username || 'N/A';
  const isGameOwner = user?.isGameOwner || false;
  const gamesPlayed = user?.gamesPlayed || [];

  // Only show up to 3 games on the card
  const displayedGames = gamesPlayed.slice(0, 3);
  const hasMoreGames = gamesPlayed.length > 3;

  // Format join date if available
  const formattedJoinDate = user?.joinDate ? formatJoinDate(user.joinDate) : null;

  return (
    <motion.div
      whileHover={{ scale: 1.02 }}
      whileTap={{ scale: 0.98 }}
      transition={{ type: "spring", stiffness: 400, damping: 17 }}
      className="h-full"
    >
      <Card 
        className={cn(
          "h-full overflow-hidden cursor-pointer",
          "border border-border/50 hover:border-border",
          "transition-colors duration-200",
          "bg-card/50 hover:bg-card"
        )}
        onClick={onClick}
      > 
        <CardHeader className="flex flex-row items-center gap-4 pb-2">
          <Avatar className="h-12 w-12 ring-2 ring-primary/10 transition-all duration-200">
            <AvatarImage src={user?.avatarUrl} alt={username} />
            <AvatarFallback className="bg-primary/5 text-primary">
              {username ? username[0].toUpperCase() : 'U'}
            </AvatarFallback>
          </Avatar>
          <div className="space-y-1">
            <CardTitle className="text-base leading-tight">{username}</CardTitle>
            <div className="flex flex-wrap gap-1 items-center">
              {isGameOwner && <Tag text="Game Owner" variant="owner" className="mt-1" />}
              {formattedJoinDate && (
                <div className="flex items-center text-xs text-muted-foreground mt-1">
                  <Calendar className="h-3 w-3 mr-1" />
                  <span>Joined {formattedJoinDate}</span>
                </div>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {gamesPlayed.length > 0 && (
            <div className="mt-2">
              <CardDescription className="text-xs mb-2">Games Played</CardDescription>
              <div className="flex flex-wrap gap-2">
                {displayedGames.map((game, index) => (
                  <Tag
                    key={index}
                    text={game}
                    variant="primary"
                    interactive
                  />
                ))}
                {hasMoreGames && (
                  <Tag 
                    text={`+${gamesPlayed.length - 3} more`} 
                    variant="secondary"
                    className="opacity-70"
                  />
                )}
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </motion.div>
  );
};

export default UserProfileCard;