import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent } from "@/components/ui/card";
import { EventCard } from "../components/events-page/EventCard"; // Assuming EventCard is reusable
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { getUserInfoByEmail } from "../service/user-api.js";
import { getGamesByOwner } from "../service/game-api.js";
import { Loader2 } from "lucide-react";

// Define GameCard locally if it's specific to this page, otherwise import it
const GameCard = ({ game }) => {
  return (
    <Card className="overflow-hidden">
      <div className="h-40 overflow-hidden bg-muted flex items-center justify-center">
        {game.image ? (
           <img src={game.image} alt={game.name} className="w-full h-full object-cover" />
        ) : (
           <span className="text-sm text-muted-foreground">No Image</span>
        )}
      </div>
      <CardContent className="p-4">
        <h3 className="font-semibold text-lg truncate">{game.name}</h3>
        {/* Add other game details if available/needed */}
      </CardContent>
    </Card>
  );
};

export default function UserProfilePage() {
  const [searchParams] = useSearchParams();
  const profileEmail = searchParams.get('email');

  // State for fetched data
  const [userInfo, setUserInfo] = useState(null); // Includes name, events, isGameOwner
  const [ownedGamesList, setOwnedGamesList] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      if (!profileEmail) {
        setError("No user email specified in URL.");
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError(null);
      setUserInfo(null);
      setOwnedGamesList([]);

      try {
        // Fetch basic account info (name, type, registered events)
        const accountData = await getUserInfoByEmail(profileEmail);
        setUserInfo(accountData);

        // If user is a game owner, fetch their games
        if (accountData && accountData.gameOwner) {
          try {
            const gamesData = await getGamesByOwner(profileEmail);
            setOwnedGamesList(gamesData || []);
          } catch (gamesError) {
             console.error("Failed to fetch owned games:", gamesError);
             setError("Could not load owned games. " + (gamesError.message || ''));
             // Continue loading profile even if games fail? Or set main error?
             // Let's set the main error for now.
          }
        }
      } catch (accountError) {
        console.error("Failed to fetch user info:", accountError);
        setError(accountError.message || "Could not load user profile.");
      } finally {
        setIsLoading(false);
      }
    };

    fetchData();
  }, [profileEmail]); // Re-fetch if email changes

  // Loading state
  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[calc(100vh-100px)]">
        <Loader2 className="h-16 w-16 animate-spin text-muted-foreground" />
      </div>
    );
  }

  // Error state
  if (error) {
     return (
       <div className="text-center py-10 text-destructive">
         <p>Error loading profile: {error}</p>
       </div>
     );
  }

   // No user found state (userInfo is null after loading without error)
   if (!userInfo) {
      return (
        <div className="text-center py-10 text-muted-foreground">
          User profile not found for email: {profileEmail || 'N/A'}
        </div>
      );
   }

  // --- Render actual profile ---
  const userType = userInfo.gameOwner ? "owner" : "player";

  return (
    <div className="bg-background text-foreground p-6">
      {/* Profile Header - Use fetched userInfo */}
      <div className="flex flex-col md:flex-row gap-6 mb-8">
        <div className="flex-shrink-0">
          <Avatar className="h-32 w-32">
            <AvatarImage src="/placeholder.svg?height=128&width=128" alt={userInfo.name || 'User'}/>
            <AvatarFallback className="text-3xl">{userInfo.name ? userInfo.name.substring(0, 2).toUpperCase() : 'U'}</AvatarFallback>
          </Avatar>
        </div>

        <div className="flex-grow">
          <div className="flex justify-between items-start">
            <div>
              <h1 className="text-4xl font-bold">{userInfo.name}</h1>
              {userInfo.gameOwner && (
                <Badge variant="secondary" className="mt-2">Game Owner</Badge>
              )}
              <p className="text-sm text-muted-foreground mt-1">{profileEmail}</p>
            </div>
            {/* TODO: Implement Edit Profile functionality only if viewing own profile */}
          </div>
        </div>
      </div>

      {/* Tabs for different sections */}
      <Tabs defaultValue={userType === 'owner' ? "games" : "registered"} className="w-full">
        <TabsList className="mb-6">
          {userInfo.gameOwner && (
             <TabsTrigger value="games">Owned Games</TabsTrigger>
          )}
          <TabsTrigger value="registered">Registered Events</TabsTrigger>
        </TabsList>

        {/* Games Tab - Only render if owner */}
        {userInfo.gameOwner && (
          <TabsContent value="games">
            <div>
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-2xl font-semibold">Games Owned</h2>
              </div>
              {ownedGamesList && ownedGamesList.length > 0 ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                  {ownedGamesList.map((game) => (
                    <GameCard key={game.id} game={game} />
                  ))}
                </div>
              ) : (
                <p className="text-muted-foreground">No games owned yet.</p>
              )}
            </div>
          </TabsContent>
        )}

        {/* Registered Tab - Use events from userInfo */}
        <TabsContent value="registered">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl font-semibold">Events Attending</h2>
            </div>
            {userInfo.events && userInfo.events.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {userInfo.events.map((event) => {
                   // Adapt event data for EventCard component
                   const adaptedEvent = {
                     id: event.eventId,
                     title: event.title,
                     dateTime: event.dateTime,
                     location: event.location,
                     host: event.host ? { name: event.host.name } : { name: 'Unknown' },
                     featuredGame: event.featuredGame ? { name: event.featuredGame.name } : { name: 'N/A' },
                     featuredGameImage: event.featuredGame?.image || "https://placehold.co/400x300/e9e9e9/1d1d1d?text=No+Image",
                     maxParticipants: event.maxParticipants,
                     participantCount: event.currentNumberParticipants,
                     description: event.description,
                   };
                   return <EventCard key={adaptedEvent.id} event={adaptedEvent} />;
                })}
              </div>
            ) : (
              <p className="text-muted-foreground">Not registered for any events.</p>
            )}
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
}
