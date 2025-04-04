// Removed useState, useEffect imports
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent } from "@/components/ui/card"; // Re-added Card imports
import { EventCard } from "../components/events-page/EventCard";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
// Removed getAccountInfo import

// Reinstated mock data based on available backend fields
const userData = {
  username: "MockUser",
  isGameOwner: true,
  events: [ // Mock registered events
    {
      id: 2,
      title: "Mock Weekend Marathon",
      dateTime: "2025-03-22T13:00:00",
      location: "Mock Community Center",
      featuredGame: "Mockopoly",
      featuredGameImage: "https://via.placeholder.com/150/0000FF/808080?text=Mockopoly", // Placeholder image
      maxParticipants: 20,
      participantCount: 12
    },
    {
      id: 4,
      title: "Mock Sahria Chkoba",
      dateTime: "2025-04-05T18:00:00",
      location: "Mock House",
      featuredGame: "Mock Cards",
      featuredGameImage: "https://via.placeholder.com/150/FF0000/FFFFFF?text=Mock+Cards", // Placeholder image
      maxParticipants: 16,
      participantCount: 8
    }
  ]
};

// Re-added mock data for owned games
const ownedGames = [
  {
    id: 1,
    name: "Mock Catan",
    image: "https://via.placeholder.com/150/00FF00/808080?text=Mock+Catan", // Placeholder image
    playCount: 28,
    rating: 4.8
  },
  {
    id: 2,
    name: "Mockopoly",
    image: "https://via.placeholder.com/150/0000FF/808080?text=Mockopoly", // Placeholder image
    playCount: 15,
    rating: 3.5
  },
  {
    id: 3,
    name: "Mock Werewolf",
    image: "https://via.placeholder.com/150/FFFF00/808080?text=Mock+Werewolf", // Placeholder image
    playCount: 42,
    rating: 4.9
  }
];

// Re-added Game card component for displaying owned games
const GameCard = ({ game }) => {
  return (
    <Card className="overflow-hidden">
      <div className="h-40 overflow-hidden">
        <img
          src={game.image}
          alt={game.name}
          className="w-full h-full object-cover"
        />
      </div>
      <CardContent className="p-4">
        <h3 className="font-semibold text-lg">{game.name}</h3>
        <div className="flex justify-between mt-2">
          <span className="text-sm text-gray-600">Played {game.playCount} times</span>
          <span className="text-sm font-medium">
            ★ {game.rating.toFixed(1)}
          </span>
        </div>
      </CardContent>
    </Card>
  );
};


export default function UserProfilePage() {
  // Removed state and useEffect for API fetching

  // Removed loading/error/no data checks

  return (
    <div className="bg-background text-foreground p-6">
      {/* Profile Header - Now uses mock userData constant */}
      <div className="flex flex-col md:flex-row gap-6 mb-8">
        <div className="flex-shrink-0">
          {/* Using a default avatar, remove profileImage */}
          <Avatar className="h-32 w-32">
            {/* <AvatarImage src={userData.profileImage} alt={userData.username} /> */}
            <AvatarFallback className="text-3xl">{userData.username?.charAt(0).toUpperCase() || '?'}</AvatarFallback>
          </Avatar>
        </div>

        <div className="flex-grow">
          <div className="flex justify-between items-start">
            <div>
              {/* Display username from API */}
              <h1 className="text-4xl font-bold">{userData.username}</h1>
              {/* Optionally display Game Owner status */}
              {userData.isGameOwner && (
                <Badge variant="secondary" className="mt-2">Game Owner</Badge>
              )}
              {/* Removed @username display as it's redundant */}
            </div>
            {/* TODO: Implement Edit Profile functionality if needed */}
            <Button className="bg-black hover:bg-gray-800 text-white">
              Edit Profile
            </Button>
          </div>

          {/* Removed Bio, Joined Date, Location, Favorite Game Types */}

        </div>
      </div>

      {/* Tabs for different sections - Re-added "My Games" */}
      <Tabs defaultValue="games" className="w-full"> {/* Set default back to games */}
        <TabsList className="mb-6">
          <TabsTrigger value="games">My Games</TabsTrigger> {/* Re-added "My Games" trigger */}
          {/* Removed "Hosting" trigger */}
          <TabsTrigger value="registered">Registered Events</TabsTrigger>
        </TabsList>

        {/* Re-added Games Tab */}
        <TabsContent value="games">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl font-semibold">Games I Own</h2>
              {/* TODO: Implement Add Game functionality if needed */}
              <Button variant="outline">Add Game</Button>
            </div>
            {ownedGames && ownedGames.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                {ownedGames.map((game) => (
                  <GameCard key={game.id} game={game} />
                ))}
              </div>
            ) : (
              <p>No games owned.</p>
            )}
          </div>
        </TabsContent>

        {/* Removed Hosting Tab */}

        {/* Registered Tab - Populated with mock data */}
        <TabsContent value="registered">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-2xl font-semibold">Events I'm Attending</h2>
              {/* TODO: Link to event search page if needed */}
              <Button variant="outline">Find Events</Button>
            </div>
            {userData.events && userData.events.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {userData.events.map((event) => (
                  <EventCard key={event.id} event={event} />
                ))}
              </div>
            ) : (
              <p>Not registered for any events.</p>
            )}
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
}
