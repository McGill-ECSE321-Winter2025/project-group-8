import { useState, useEffect, useCallback } from "react";
import { TabsContent } from "@/components/ui/tabs.jsx";
import { Button } from "@/components/ui/button.jsx";
import { EventCard } from "../events-page/EventCard.jsx"; // Use EventCard for consistency
import CreateEventDialog from "../events-page/CreateEventDialog.jsx"; // Import dialog
import { getEventsByHostEmail } from "../../service/event-api.js"; // Removed getEventById
import { getRegistrationsByEmail } from "../../service/registration-api.js"; // Import attended events fetcher
import { UnauthorizedError } from "@/service/apiClient"; // Import UnauthorizedError
import { useAuth } from "@/context/AuthContext"; // Import useAuth
import { Loader2 } from "lucide-react"; // Import loader

export default function DashboardEvents({ userType }) { // Accept userType prop
  const [hostedEvents, setHostedEvents] = useState([]);
  const [attendedRegistrations, setAttendedRegistrations] = useState([]); // Store full registration objects
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
      console.log("[DashboardEvents] Full Registrations:", registrations); // Log the full data
      // Store the full registration objects, filtering out any potentially invalid ones
      setAttendedRegistrations(registrations.filter(reg => reg && reg.event) || []);

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
      setAttendedRegistrations([]);
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
  // Helper to adapt backend event DTO to what EventCard expects
  const adaptEventData = (event) => {
     if (!event) return null;
     return {
      id: event.eventId, // Use eventId from backend DTO
      title: event.title,
      dateTime: event.dateTime, // Pass raw date/time; formatting done in EventCard
      location: event.location || 'N/A',
      hostName: event.host?.name || 'Unknown Host', // Use host object if available
      game: event.featuredGame?.name || 'Unknown Game', // Use featuredGame object
      currentNumberParticipants: event.currentNumberParticipants,
      maxParticipants: event.maxParticipants,
      featuredGameImage: event.featuredGame?.image || "https://placehold.co/400x300/e9e9e9/1d1d1d?text=No+Image",
      participants: {
        current: event.currentNumberParticipants ?? 0,
        capacity: event.maxParticipants ?? 0,
      },
      description: event.description || '',
    };
  };


  return (
    <>
      <TabsContent value="events" className="space-y-6">
        <div className="flex justify-between items-center">
          <h2 className="text-2xl font-bold">My Events</h2>
          {/* Only show Create Event button if user is owner (checked via prop and auth context) */}
          {userType === "owner" && user?.gameOwner && (
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
            {/* Events Hosting Section (only if owner - checked via prop and auth context) */}
            {userType === "owner" && user?.gameOwner && (
              <div>
                <h3 className="text-xl font-semibold mb-4">Hosting</h3>
                 {hostedEvents.length > 0 ? (
                   <div className="space-y-4">
                     {hostedEvents.map(event => {
                        const adapted = adaptEventData(event);
                        // TODO: Adapt hosted events for EventCard if needed, or keep using Event.jsx?
                        // For now, assuming Event.jsx is still used for hosted events, but needs adapting
                        // This part is outside the scope of unregistering attended events.
                        // Let's adapt hosted events too for consistency, assuming Event.jsx is removed/replaced.
                        return adapted ? <EventCard key={`hosted-${adapted.id}`} event={adapted} onRegistrationUpdate={fetchDashboardEvents} isCurrentUserRegistered={false} /> : null;
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
                 {attendedRegistrations.length > 0 ? (
                   <div className="space-y-4">
                     {attendedRegistrations.map(registration => {
                       const event = registration.event; // Get the event object from registration
                       const registrationId = registration.id; // Get the registration ID
                       const adaptedEvent = adaptEventData(event); // Adapt the event data
                       if (!adaptedEvent) return null; // Skip if event data is invalid

                       // Render EventCard for attended events
                       return (
                         <EventCard
                           key={`attended-${adaptedEvent.id}`}
                           event={adaptedEvent}
                           onRegistrationUpdate={fetchDashboardEvents} // Pass refresh function
                           isCurrentUserRegistered={true} // Always true for this list
                           registrationId={registrationId} // Pass the specific registration ID
                         />
                       );
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
