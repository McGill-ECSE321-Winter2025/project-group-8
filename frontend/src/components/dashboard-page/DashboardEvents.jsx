import { useState, useEffect } from "react";
import { TabsContent } from "@/components/ui/tabs.jsx";
import { Button } from "@/components/ui/button.jsx";
import Event from "./Event.jsx";
import EventRegistrated from "./EventRegistered.jsx"; 
import CreateEventDialog from "../events-page/CreateEventDialog.jsx";
import { getEventsByHostEmail, getEventById } from "../../service/event-api.js";
import { getRegistrationsByEmail } from "../../service/registration-api.js";
import { Loader2 } from "lucide-react";

export default function DashboardEvents({ userType }) {
  const [hostedEvents, setHostedEvents] = useState([]);
  const [attendedEvents, setAttendedEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);

  // Function to fetch both hosted and attended events
  const fetchDashboardEvents = async () => {
    setIsLoading(true);
    setError(null);
    const userEmail = localStorage.getItem("userEmail");

    if (!userEmail) {
      setError("User email not found. Please log in again.");
      setIsLoading(false);
      return;
    }

    try {
      // Fetch hosted events only if the user is an owner
      let hosted = [];
      if (userType === "owner") {
        hosted = await getEventsByHostEmail(userEmail);
        setHostedEvents(hosted || []);
      } else {
        setHostedEvents([]); // Clear if not owner
      }

      // Fetch registrations (attended events)
      const registrations = await getRegistrationsByEmail(userEmail);
      
      // Create a mapping of eventId to registrationId
      const registrationMap = {};
      registrations.forEach(reg => {
        registrationMap[reg.eventId] = reg.id; // Store registration ID by event ID
      });

      // Get event IDs from registrations
      const eventIds = registrations.map(reg => reg.eventId).filter(Boolean);

      // Fetch full event details for each attended event ID
      const attendedEventPromises = eventIds.map(id => getEventById(id));
      const attendedEventResults = await Promise.allSettled(attendedEventPromises);

      const attended = attendedEventResults
        .filter(result => result.status === 'fulfilled' && result.value)
        .map(result => {
          const event = result.value;
          // Attach the registrationId to each event
          return {
            ...event,
            registrationId: registrationMap[event.eventId]
          };
        });
      setAttendedEvents(attended || []);

      // Log any errors from fetching individual attended events
      attendedEventResults
         .filter(result => result.status === 'rejected')
         .forEach(result => console.error("Failed to fetch attended event details:", result.reason));

    } catch (err) {
      // This will catch errors from fetching hosted events or registrations list
      console.error("Failed to fetch dashboard events data:", err);
      setError(err.message || "Could not load events.");
      setHostedEvents([]);
      setAttendedEvents([]);
    } finally {
      setIsLoading(false);
    }
  };

  // Fetch events on component mount and when userType changes
  useEffect(() => {
    fetchDashboardEvents();
  }, [userType]);

  // Function to handle event creation success (passed to dialog)
  const handleEventAdded = () => {
    fetchDashboardEvents(); // Re-fetch events after adding a new one
  };

  // Helper to adapt backend event DTO to what the child Event component expects
  const adaptEventData = (event) => {
    if (!event) return null;
    return {
      id: event.eventId,
      name: event.title,
      date: event.dateTime ? new Date(event.dateTime).toLocaleDateString() : 'N/A',
      time: event.dateTime ? new Date(event.dateTime).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' }) : 'N/A',
      location: event.location || 'N/A',
      game: event.featuredGame?.name || 'N/A',
      gameImage: event.featuredGame?.image || "https://placehold.co/400x300/e9e9e9/1d1d1d?text=No+Image",
      participants: {
        current: event.currentNumberParticipants ?? 0,
        capacity: event.maxParticipants ?? 0,
      },
      hostName: event.host?.name || 'Unknown',
      description: event.description || '',
      // Include the registrationId directly from the event object
      registrationId: event.registrationId || null,
    };
  };

  return (
    <>
      <TabsContent value="events" className="space-y-6">
        <div className="flex justify-between items-center">
          <h2 className="text-2xl font-bold">My Events</h2>
          {/* Only show Create Event button if user is owner */}
          {userType === "owner" && (
             <Button onClick={() => setIsCreateDialogOpen(true)}>Create Event</Button>
          )}
        </div>

        {isLoading ? (
          <div className="flex justify-center items-center py-10">
            <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
          </div>
        ) : error ? (
          <div className="text-center py-10 text-destructive">
            <p>Error loading events: {error}</p>
          </div>
        ) : (
          <div className="space-y-8">
            {/* Events Hosting Section (only if owner) */}
            {userType === "owner" && (
              <div>
                <h3 className="text-xl font-semibold mb-4">Hosting</h3>
                 {hostedEvents.length > 0 ? (
                   <div className="space-y-4">
                     {hostedEvents.map(event => {
                        const adapted = adaptEventData(event);
                        // Pass the refresh function down to the Event component
                        return adapted ? <Event key={`hosted-${adapted.id}`} {...adapted} onRegistrationUpdate={fetchDashboardEvents} /> : null;
                     })}
                   </div>
                ) : (
                  <p className="text-muted-foreground">You are not hosting any events.</p>
                )}
              </div>
            )}

            {/* Events Attending Section */}
            <div>
              <h3 className="text-xl font-semibold mb-4">Attending</h3>
                 {attendedEvents.length > 0 ? (
                   <div className="space-y-4">
                     {attendedEvents.map(event => {
                        const adapted = adaptEventData(event);
                        // Pass the refresh function down to the EventRegistrated component
                        return adapted ? (
                          <EventRegistrated 
                            key={`attended-${adapted.id}`} 
                            {...adapted} 
                            onRegistrationUpdate={fetchDashboardEvents} 
                          />
                        ) : null;
                     })}
                   </div>
              ) : (
                <p className="text-muted-foreground">You are not attending any events.</p>
              )}
            </div>
          </div>
        )}
      </TabsContent>

      {/* Render the Create Event Dialog */}
      <CreateEventDialog
        open={isCreateDialogOpen}
        onOpenChange={setIsCreateDialogOpen}
        onEventAdded={handleEventAdded} // Pass the refresh function
      />
    </>
  );
}