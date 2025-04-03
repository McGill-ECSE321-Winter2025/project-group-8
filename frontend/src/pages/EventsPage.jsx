import { useState } from "react";
import { Button } from "@/components/ui/button";
import { EventSearchBar } from "../components/events-page/EventSearchBar";
import { EventCard } from "../components/events-page/EventCard";
import { DateFilterComponent } from "../components/events-page/DateFilterComponent";
import CreateEventDialog from "../components/events-page/CreateEventDialog";

// Mock data is used here while waiting to really implement API calls
const upcomingEvents = [
  {
    id: 4,
    title: "Sahria Chkoba",
    dateTime: "2025-04-05T18:00:00",
    location: "Yessine's House, 789 Pine St",
    featuredGame: "Cards",
    featuredGameImage: "https://play-lh.googleusercontent.com/JQt2sr9XF-5JPXZVJ8fV3vGsOZTm-R6RrsNpwZL1x0f-W9Kis9U2FegyT-yVl0PCfA",
    maxParticipants: 16,
    participantCount: 8
  },
  {
    id: 1,
    title: "Friday Night Strategy Games",
    dateTime: "2025-03-15T19:00:00",
    location: "Board Game Cafe, 123 Main St",
    featuredGame: "Settlers of Catan",
    featuredGameImage: "https://www.asdesjeux.com/cdn/shop/files/qzfkij6xaovkewqw1kht.png?v=1725899582",
    maxParticipants: 6,
    participantCount: 4
  },
  {
    id: 2,
    title: "Weekend Board Game Marathon",
    dateTime: "2025-03-22T13:00:00",
    location: "Community Center, 456 Oak Ave",
    featuredGame: "Monopoly",
    featuredGameImage: "https://i5.walmartimages.com/seo/Monopoly-The-Mega-Edition-Board-Game_71fb2957-622e-45ac-9f2e-9871836991c7.13d6146918d3670bb3661408d2ab6d89.jpeg",
    maxParticipants: 20,
    participantCount: 12
  },
  {
    id: 3,
    title: "Werewolf Game Night",
    dateTime: "2025-04-05T18:00:00",
    location: "Game Store, 789 Pine St",
    featuredGame: "Werewolf",
    featuredGameImage: "https://www.zygomatic-games.com/wp-content/uploads/2020/04/lmelg01en_face_20200616-802x1024.jpg",
    maxParticipants: 16,
    participantCount: 8
  }
];

export default function EventsPage() {
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [filteredEvents, setFilteredEvents] = useState(upcomingEvents);
  const [isSearchActive, setIsSearchActive] = useState(false);

  // Filter events by date range
  const handleDateFilter = (filterValue) => {
    if (!filterValue) {
      setFilteredEvents(upcomingEvents);
      return;
    }
    
    const now = new Date();
    const filtered = upcomingEvents.filter(event => {
      const eventDate = new Date(event.dateTime);
      
      if (filterValue === "this-week") {
        const weekFromNow = new Date();
        weekFromNow.setDate(now.getDate() + 7);
        return eventDate >= now && eventDate <= weekFromNow;
      } else if (filterValue === "this-month") {
        const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);
        return eventDate >= now && eventDate <= endOfMonth;
      } else if (filterValue === "next-month") {
        const startOfNextMonth = new Date(now.getFullYear(), now.getMonth() + 1, 1);
        const endOfNextMonth = new Date(now.getFullYear(), now.getMonth() + 2, 0);
        return eventDate >= startOfNextMonth && eventDate <= endOfNextMonth;
      }
      return true;
    });
    
    setFilteredEvents(filtered);
  };

  // Handle search activity state
  const handleSearchStateChange = (isActive) => {
    setIsSearchActive(isActive);
  };

  return (
    <div className="bg-background text-foreground p-6">
      <div className="flex items-start justify-between mb-6">
        <div>
          <h1 className="text-4xl font-bold">Upcoming Events</h1>
          <h2 className="text-gray-600 mt-2">
            Join board game events in your area.
          </h2>
        </div>
        <Button
          className="bg-black hover:bg-gray-800 text-white"
          onClick={() => setCreateDialogOpen(true)}
        >
          Create Event
        </Button>
      </div>

      <div className="flex gap-4 mb-8">
        <div className="flex-grow">
          <EventSearchBar onSearchStateChange={handleSearchStateChange} />
        </div>
        <div className="w-48">
          <DateFilterComponent onFilterChange={handleDateFilter} />
        </div>
      </div>

      {/* Display filtered events only when no search is active */}
      {!isSearchActive && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredEvents.map((event) => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      )}

      <CreateEventDialog
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
      />
    </div>
  );
}