import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { EventSearchBar } from "../components/events-page/EventSearchBar";
import { EventCard } from "../components/events-page/EventCard";
import { DateFilterComponent } from "../components/events-page/DateFilterComponent";
import CreateEventDialog from "../components/events-page/CreateEventDialog";
import { AnimatePresence, motion } from "framer-motion";
import { getAllEvents } from "../service/event-api"; // Import API function
import { Loader2 } from "lucide-react"; // Import Loader icon

// Removed mock data
// const upcomingEvents = [...]

// Create card stagger animation variants (remains the same)
const container = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1
    }
  },
  exit: {
    opacity: 0,
    transition: {
      staggerChildren: 0.05,
      staggerDirection: -1,
      when: "afterChildren"
    }
  }
};

const item = {
  hidden: { opacity: 0, scale: 0.8, y: 20 },
  show: { 
    opacity: 1, 
    scale: 1, 
    y: 0,
    transition: { 
      type: "spring",
      stiffness: 300,
      damping: 24
    }
  },
  exit: { 
    opacity: 0, 
    scale: 0.8, 
    y: -20,
    transition: { 
      duration: 0.3,
      ease: "easeInOut"
    }
  }
};

export default function EventsPage() {
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [allEvents, setAllEvents] = useState([]); // Store all fetched events
  const [filteredEvents, setFilteredEvents] = useState([]); // Store currently filtered events
  const [isLoading, setIsLoading] = useState(true); // Loading state
  const [error, setError] = useState(null); // Error state
  const [isSearchActive, setIsSearchActive] = useState(false); // Search state remains
  const [displayedEvents, setDisplayedEvents] = useState([]); // Events to actually display (for animation)
  const [isSearchTransitioning, setIsSearchTransitioning] = useState(false); // Animation state remains

  // Fetch all events on component mount
  useEffect(() => {
    const fetchEvents = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const data = await getAllEvents();
        setAllEvents(data);
        setFilteredEvents(data); // Initially, show all events
        setDisplayedEvents(data); // Update display
      } catch (err) {
        console.error("Failed to fetch events:", err);
        setError(err.message || "Could not load events.");
      } finally {
        setIsLoading(false);
      }
    };
    fetchEvents();
  }, []); // Empty dependency array means run once on mount

  // Update displayed events with animation delay (remains similar)
  useEffect(() => {
    if (!isSearchActive) {
      setDisplayedEvents(filteredEvents);
    }
  }, [filteredEvents, isSearchActive]);

  // Filter events by date range (now filters 'allEvents')
  const handleDateFilter = (filterValue) => {
    if (filterValue === "all") {
      setFilteredEvents(allEvents); // Reset to all fetched events
      return;
    }

    const now = new Date();
    const filtered = allEvents.filter(event => {
      // Backend returns java.sql.Date which might be just a string or number timestamp
      // Safely create Date object
      const eventDate = event.dateTime ? new Date(event.dateTime) : null;
      if (!eventDate) return false; // Skip if date is invalid

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
      // Should not happen with current filters, but good practice
      return false;
    });

    setFilteredEvents(filtered);
  };

  // Handle search activity state with transition
  const handleSearchStateChange = (isActive) => {
    if (isActive && !isSearchActive) {
      // Start transition when search becomes active
      setIsSearchTransitioning(true);
      
      // Delay setting isSearchActive to true to allow exit animations to complete
      setTimeout(() => {
        setIsSearchActive(true);
        setIsSearchTransitioning(false);
      }, 400); // This should match the total duration of the exit animations
    } else if (!isActive) {
      setIsSearchActive(false);
    }
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

      {/* Event Card Grid - Conditional Rendering based on loading/error/data */}
      {isLoading ? (
        <div className="flex justify-center items-center py-10">
          <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        </div>
      ) : error ? (
        <div className="text-center py-10 text-destructive">
          <p>Error loading events: {error}</p>
        </div>
      ) : (
        <AnimatePresence mode="wait">
          {!isSearchActive && !isSearchTransitioning && displayedEvents.length > 0 ? (
            <motion.div
              className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
              key="grid"
              variants={container}
              initial="hidden"
              animate="show"
              exit="exit"
            >
              {displayedEvents.map((event, index) => {
                // Adapt event data for EventCard component
                const adaptedEvent = {
                  id: event.eventId,
                  title: event.title,
                  dateTime: event.dateTime, // Pass Date object or formatted string
                  location: event.location,
                  host: event.host ? { name: event.host.name } : { name: 'Unknown Host' }, // Handle potential null host
                  featuredGame: event.featuredGame ? { name: event.featuredGame.name } : { name: 'N/A' }, // Handle potential null game
                  featuredGameImage: event.featuredGame?.image || "https://placehold.co/400x300/e9e9e9/1d1d1d?text=No+Image", // Use game image or placeholder
                  maxParticipants: event.maxParticipants,
                  participantCount: event.currentNumberParticipants,
                  description: event.description,
                };
                return (
                  <motion.div
                    key={adaptedEvent.id} // Use unique eventId from backend
                    variants={item}
                    custom={index}
                    layout
                  >
                    <EventCard event={adaptedEvent} />
                  </motion.div>
                );
              })}
            </motion.div>
          ) : !isSearchActive && !isSearchTransitioning && displayedEvents.length === 0 ? (
             <motion.div key="no-events" initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} className="text-center py-10 text-muted-foreground">
               No events found matching your criteria.
             </motion.div>
          ) : null}
        </AnimatePresence>
      )}

      <CreateEventDialog
        open={createDialogOpen}
        onOpenChange={setCreateDialogOpen}
      />
    </div>
  );
}
