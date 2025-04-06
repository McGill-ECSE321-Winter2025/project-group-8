import { useState, useEffect, useCallback } from "react";
import { TabsContent } from "@/components/ui/tabs.jsx";
import { Button } from "@/components/ui/button.jsx";
import Event from "./Event.jsx"; // Assuming this component displays event details
import CreateEventDialog from "../events-page/CreateEventDialog.jsx"; // Import dialog
import { getEventsByHostEmail, getEventById } from "../../service/event-api.js"; // Import event fetchers
import { getRegistrationsByEmail } from "../../service/registration-api.js"; // Import attended events fetcher
import { UnauthorizedError } from "@/service/apiClient"; // Import UnauthorizedError
import { useAuth } from "@/context/AuthContext"; // Import useAuth
import { Loader2 } from "lucide-react"; // Import loader

export default function DashboardEvents({ userType }) { // Accept userType prop
  const [hostedEvents, setHostedEvents] = useState([]);
  const [attendedEvents, setAttendedEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const { user, isAuthenticated, authReady } = useAuth(); // Get auth context with authReady
  const [apiCallAttempted, setApiCallAttempted] = useState(false);

  // Function to fetch both hosted and attended events - memoized to prevent infinite loops
  const fetchDashboardEvents = useCallback(async () => {
    // Don't try to fetch if we're not authenticated or auth isn't ready
    if (!user?.email || !isAuthenticated || !authReady) {
      if (!isLoading) return; // Don't update state if not loading
      setIsLoading(false);
      return;
    }

    // Don't refetch if we already tried and no auth state has changed
    if (apiCallAttempted && !isLoading) return;

    console.log("[DashboardEvents] Fetching events for user:", user?.email, "Auth ready:", authReady);
    setIsLoading(true);
    setError(null);
    setApiCallAttempted(true);
    const userEmail = user?.email;

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
      const response = await getRegistrationsByEmail(userEmail);
      
      // Ensure registrations is an array
      const registrations = Array.isArray(response) ? response : [];
      console.log("[DashboardEvents] Registrations response:", response);

      // Get event IDs from registrations
      const eventIds = registrations.map(reg => reg.eventId).filter(Boolean);
      
      // If no event IDs, set empty attendedEvents and return early
      if (eventIds.length === 0) {
        setAttendedEvents([]);
        setIsLoading(false);
        return;
      }

      // Fetch full event details for each attended event ID
      const attendedEventPromises = eventIds.map(id => getEventById(id));
      const attendedEventResults = await Promise.allSettled(attendedEventPromises);

      const attended = attendedEventResults
        .filter(result => result.status === 'fulfilled' && result.value)
        .map(result => result.value);
      setAttendedEvents(attended || []);

      // Log any errors from fetching individual attended events
      attendedEventResults
         .filter(result => result.status === 'rejected')
         .forEach(result => console.error("Failed to fetch attended event details:", result.reason));

    } catch (err) {
      // This will catch errors from fetching hosted events or registrations list
      if (err instanceof UnauthorizedError) {
        console.error("Unauthorized error fetching dashboard events:", err);
        setError("Authentication error. Please try logging in again.");
      } else {
        console.error("Failed to fetch dashboard events data:", err);
        setError(err.message || "Could not load events.");
      }
      setHostedEvents([]);
      setAttendedEvents([]);
    } finally {
      setIsLoading(false);
    }
  }, [userType, user, isAuthenticated, authReady, isLoading, apiCallAttempted]);

  // Reset API call attempted when auth state changes
  useEffect(() => {
    if (authReady && isAuthenticated && user?.email) {
      setApiCallAttempted(false);
    }
  }, [authReady, isAuthenticated, user]);

  // Fetch events on component mount and when userType or user changes
  useEffect(() => {
    console.log("[DashboardEvents] useEffect triggered, authReady:", authReady, "isAuthenticated:", isAuthenticated);
    // Add a small delay to ensure auth state is fully updated
    const timer = setTimeout(() => {
      if (authReady && isAuthenticated && user?.email) {
        fetchDashboardEvents();
      }
    }, 300);
    
    return () => clearTimeout(timer);
  }, [userType, user, fetchDashboardEvents, authReady, isAuthenticated]);

  // Function to handle event creation success (passed to dialog)
  const handleEventAdded = useCallback(() => {
    fetchDashboardEvents(); // Re-fetch events after adding a new one
  }, [fetchDashboardEvents]);

  // Helper to adapt backend event DTO to what the child Event component expects
  // TODO: Verify props expected by the ./Event.jsx component
  const adaptEventData = (event) => {
     if (!event) return null;
     return {
       id: event.eventId, // Assuming EventResponse DTO has eventId
       name: event.title,
       date: event.dateTime ? new Date(event.dateTime).toLocaleDateString() : 'N/A', // Format date
       time: event.dateTime ? new Date(event.dateTime).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' }) : 'N/A', // Format time
       location: event.location || 'N/A',
       game: event.featuredGame?.name || 'N/A', // Safely access nested name
       participants: {
         current: event.currentNumberParticipants ?? 0, // Use nullish coalescing
         capacity: event.maxParticipants ?? 0,
       },
       // Add other props if Event component needs them (e.g., description, host name)
       hostName: event.host?.name || 'Unknown',
       description: event.description || '',
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
                         // Pass the refresh function down to the Event component
                        return adapted ? <Event key={`attended-${adapted.id}`} {...adapted} onRegistrationUpdate={fetchDashboardEvents} /> : null;
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
