import { useState, useEffect, useRef, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent } from "@/components/ui/card";
import { EventCard } from "../components/events-page/EventCard"; // Assuming EventCard is reusable
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { getUserInfoByEmail } from "../service/user-api.js";
import { getGamesByOwner } from "../service/game-api.js";
import { getEventsByHostEmail } from "../service/event-api.js";
import { Loader2 } from "lucide-react";
import { useAuth } from "@/context/AuthContext"; // Import useAuth

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
  const { user: currentUser } = useAuth(); // Get current user from AuthContext
  const [activeTab, setActiveTab] = useState(null);

  // State for fetched data
  const [userInfo, setUserInfo] = useState(null); // Includes name, events, isGameOwner
  const [ownedGamesList, setOwnedGamesList] = useState([]);
  const [hostedEventsList, setHostedEventsList] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Ref to track the last fetched email to prevent redundant API calls
  const lastFetchedEmailRef = useRef(null);

  // Check if this is the current user's own profile
  const isOwnProfile = currentUser && 
    ((!profileEmail && currentUser.email) || (profileEmail === currentUser.email));

  // Memoize fetchData to prevent recreation on each render
  const fetchData = useCallback(async (emailToFetch) => {
    // Skip if we're trying to fetch the same email again
    if (lastFetchedEmailRef.current === emailToFetch && userInfo) {
      return;
    }
    
    // Update the ref to the current email being fetched
    lastFetchedEmailRef.current = emailToFetch;
    
    if (!emailToFetch) {
      setError("No user email specified in URL and you are not logged in.");
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);
    setUserInfo(null);
    setOwnedGamesList([]);
    setHostedEventsList([]);

    try {
      // Fetch basic account info (name, type, registered events)
      const accountData = await getUserInfoByEmail(emailToFetch);
      setUserInfo(accountData);

      // If user is a game owner, fetch their games
      if (accountData && accountData.gameOwner) {
        try {
          const gamesData = await getGamesByOwner(emailToFetch);
          setOwnedGamesList(gamesData || []);
        } catch (gamesError) {
           console.error("Failed to fetch owned games:", gamesError);
           // Don't set error state here - just log it as we want to continue even if games can't be fetched
        }
      }
      
      // Fetch events that the user is hosting
      try {
        const hostedEvents = await getEventsByHostEmail(emailToFetch);
        setHostedEventsList(hostedEvents || []);
      } catch (eventsError) {
        console.error("Failed to fetch hosted events:", eventsError);
        // Don't set error, just log it to avoid blocking the UI
      }
    } catch (accountError) {
      console.error("Failed to fetch user info:", accountError);
      setError(accountError.message || "Could not load user profile.");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    // Use profile email from URL params, or fall back to current user's email
    const emailToFetch = profileEmail || currentUser?.email;
    
    // Only fetch if we have an email to fetch
    if (emailToFetch) {
      fetchData(emailToFetch);
    }
  }, [profileEmail, fetchData]); // Remove currentUser?.email from dependencies

  // Set the initial active tab once userInfo is loaded
  useEffect(() => {
    if (userInfo && !activeTab) {
      // If the user is hosting events, start on that tab, otherwise go to the default tab
      if (hostedEventsList.length > 0) {
        setActiveTab("hosting");
      } else {
        const userType = userInfo.gameOwner ? "owner" : "player";
        setActiveTab(userType === 'owner' ? "games" : "registered");
      }
    }
  }, [userInfo, activeTab, hostedEventsList.length]);

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

  // Handle tab change
  const handleTabChange = (value) => {
    setActiveTab(value);
  };

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
              <p className="text-sm text-muted-foreground mt-1">{profileEmail || currentUser?.email}</p>
            </div>
            {/* TODO: Implement Edit Profile functionality only if viewing own profile */}
          </div>
        </div>
      </div>

      {/* Tabs for different sections */}
      <Tabs value={activeTab} onValueChange={handleTabChange} className="w-full">
        <TabsList className="mb-6">
          {userInfo.gameOwner && (
             <TabsTrigger value="games">Owned Games</TabsTrigger>
          )}
          <TabsTrigger value="hosting">Hosting</TabsTrigger>
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

        {/* Hosting Tab - Events user is hosting */}
        <TabsContent value="hosting">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl font-semibold">Events Hosting</h2>
            </div>
            {hostedEventsList && hostedEventsList.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {hostedEventsList.map((event) => {
                  const adaptedEvent = {
                    id: event.id || event.eventId,
                    eventId: event.id || event.eventId,
                    title: event.title,
                    name: event.title,
                    dateTime: event.dateTime,
                    location: event.location,
                    host: event.host ? {
                      name: event.host.name,
                      email: event.host.email
                    } : { name: userInfo.name },
                    hostName: event.host ? event.host.name : userInfo.name,
                    hostEmail: event.host ? event.host.email : profileEmail || currentUser?.email,
                    featuredGame: event.featuredGame ? { name: event.featuredGame.name } : { name: 'Unknown Game' },
                    game: event.featuredGame ? event.featuredGame.name : 'Unknown Game',
                    featuredGameImage: event.featuredGame?.image || "https://placehold.co/400x300/e9e9e9/1d1d1d?text=No+Image",
                    maxParticipants: event.maxParticipants,
                    currentNumberParticipants: event.currentNumberParticipants,
                    participants: {
                      current: event.currentNumberParticipants,
                      capacity: event.maxParticipants
                    },
                    description: event.description,
                  };
                  
                  return <EventCard 
                    key={adaptedEvent.id} 
                    event={adaptedEvent}
                    isUserEventHost={isOwnProfile}
                    hideRegisterButtons={true}
                    onRegistrationUpdate={() => {
                      // Ensure we stay on the hosting tab
                      setActiveTab("hosting");
                      // Refresh the data
                      fetchData(profileEmail || currentUser?.email);
                    }}
                  />;
                })}
              </div>
            ) : (
              <p className="text-muted-foreground">Not hosting any events yet.</p>
            )}
          </div>
        </TabsContent>

        {/* Registered Tab - Use events from userInfo */}
        <TabsContent value="registered">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl font-semibold">Events Attending</h2>
            </div>
            {userInfo.events && userInfo.events.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {userInfo.events.map((registration) => {
                   // Adapt event data from registration.event for EventCard component
                   const event = registration.event;
                   if (!event) {
                     console.error("Missing event data in registration:", registration);
                     return null;
                   }
                   
                   const adaptedEvent = {
                     id: event.eventId || event.id,
                     eventId: event.eventId || event.id, // Add explicit eventId property as backup
                     title: event.title,
                     name: event.title, // add name as backup
                     dateTime: event.dateTime,
                     location: event.location,
                     // Properly map host information
                     host: event.host ? { 
                       name: event.host.name,
                       email: event.host.email
                     } : { name: 'Unknown Host' },
                     hostName: event.host ? event.host.name : 'Unknown Host',
                     hostEmail: event.host ? event.host.email : null,
                     // Properly map game information
                     featuredGame: event.featuredGame ? { name: event.featuredGame.name } : { name: 'Unknown Game' },
                     game: event.featuredGame ? event.featuredGame.name : 'Unknown Game',
                     featuredGameImage: event.featuredGame?.image || "https://placehold.co/400x300/e9e9e9/1d1d1d?text=No+Image",
                     // Participants info - multiple formats to ensure compatibility
                     maxParticipants: event.maxParticipants,
                     currentNumberParticipants: event.currentNumberParticipants,
                     participants: {
                       current: event.currentNumberParticipants,
                       capacity: event.maxParticipants
                     },
                     description: event.description,
                   };
                   
                   return <EventCard 
                     key={adaptedEvent.id} 
                     event={adaptedEvent}
                     isCurrentUserRegistered={isOwnProfile}
                     registrationId={isOwnProfile ? registration.id : null}
                     hideRegisterButtons={true}
                     onRegistrationUpdate={() => {
                       // Ensure we stay on the registered events tab
                       setActiveTab("registered"); 
                       // Refresh the data
                       fetchData(profileEmail || currentUser?.email);
                     }}
                   />;
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
