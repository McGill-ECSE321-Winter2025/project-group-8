import { ChevronRight, Users, Calendar, User, Copy } from "lucide-react";
import { Card, CardContent, CardFooter, CardHeader, CardTitle, CardDescription } from "../../ui/card";
import { Button } from "../ui/button";
import { Badge } from "../../ui/badge";

export function GameCard({ game, showInstanceCount = false }) {
  // Format date for display
  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <Card className="cursor-pointer hover:shadow-md transition-shadow h-full flex flex-col overflow-hidden">
      <div className="h-64 overflow-hidden">
        <img 
          src={game.image} 
          alt={game.name} 
          className="w-full h-full object-cover"
        />
      </div>
      <CardHeader className="pb-0">
        <CardTitle>{game.name}</CardTitle>
        <CardDescription>
          <Badge variant="secondary" className="mt-1">{game.category}</Badge>
        </CardDescription>
      </CardHeader>
      <CardContent className="flex-grow pt-4">
        <div className="space-y-3">
          <div className="flex items-center bg-muted/50 p-2 rounded-md">
            <Users size={16} className="mr-2 text-muted-foreground" />
            <span className="text-sm">{game.minPlayers}-{game.maxPlayers} players</span>
          </div>
          
          {showInstanceCount && game.instances && (
            <div className="flex items-center bg-muted/50 p-2 rounded-md">
              <Copy size={16} className="mr-2 text-muted-foreground" />
              <span className="text-sm">Available copies: {game.instances.length}</span>
            </div>
          )}
          
          {!showInstanceCount && game.dateAdded && (
            <>
              <div className="flex items-center bg-muted/50 p-2 rounded-md">
                <Calendar size={16} className="mr-2 text-muted-foreground" />
                <span className="text-sm">Added: {formatDate(game.dateAdded)}</span>
              </div>
              <div className="flex items-center bg-muted/50 p-2 rounded-md">
                <User size={16} className="mr-2 text-muted-foreground" />
                <span className="text-sm">Owner: {game.owner.name}</span>
              </div>
            </>
          )}
        </div>
      </CardContent>
      <CardFooter className="mt-auto border-t pt-4">
        <Button variant="ghost" className="text-sm w-full justify-between hover:bg-muted/70">
          {showInstanceCount ? "View Available Copies" : "View Details"} <ChevronRight size={16} />
        </Button>
      </CardFooter>
    </Card>
  );
} 