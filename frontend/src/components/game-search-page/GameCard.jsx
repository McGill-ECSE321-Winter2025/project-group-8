import React from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../ui/card';
import { motion } from 'framer-motion';
import { cn } from '../../lib/utils';
import { Users, Clock, Gauge } from 'lucide-react';
import Tag from '../common/Tag.jsx';

export const GameCard = ({ game, showInstanceCount = false }) => {
  // Provide default values or placeholders if game data might be missing
  const name = game?.name || 'Unknown Game';
  const category = game?.category || 'Uncategorized';
  const description = game?.description || 'No description available';
  const minPlayers = game?.minPlayers || 1;
  const maxPlayers = game?.maxPlayers || 4;
  const playTime = game?.playTime || 'Unknown';
  const complexity = game?.complexity || 'Medium';
  const imageUrl = game?.imageUrl || 'https://placehold.co/400x300/e9e9e9/1d1d1d?text=Board+Game';

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
      > 
        <div className="relative h-48 overflow-hidden">
          <img 
            src={imageUrl} 
            alt={name} 
            className="w-full h-full object-cover"
          />
          <div className="absolute top-3 left-3">
            <Tag text={category} variant="secondary" />
          </div>
          {showInstanceCount && (
            <div className="absolute top-3 right-3 bg-black/60 text-white text-xs px-2 py-1 rounded-full">
              3 instances available
            </div>
          )}
        </div>
        
        <CardHeader className="pb-2">
          <CardTitle className="text-xl">{name}</CardTitle>
        </CardHeader>
        
        <CardContent>
          <CardDescription className="line-clamp-2 mb-4 text-sm">
            {description}
          </CardDescription>
          
          <div className="flex flex-wrap gap-3 text-sm text-muted-foreground">
            <div className="flex items-center gap-1">
              <Users className="h-4 w-4" />
              <span>{minPlayers === maxPlayers ? minPlayers : `${minPlayers}-${maxPlayers}`} players</span>
            </div>
            
            <div className="flex items-center gap-1">
              <Clock className="h-4 w-4" />
              <span>{playTime}</span>
            </div>
            
            <div className="flex items-center gap-1">
              <Gauge className="h-4 w-4" />
              <span>{complexity}</span>
            </div>
          </div>
        </CardContent>
      </Card>
    </motion.div>
  );
}; 