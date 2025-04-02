/**
 * Event Service
 * 
 * This file provides mock API functions for event-related operations.
 */

import { events, eventLocations } from './mockData';
import {
  simulateApiCall,
  paginateItems,
  clone,
  generateId,
  searchInObject,
  errorMessages
} from './apiUtils';

// In-memory event storage (cloned to avoid mutating the original)
let eventStore = clone(events);

// Get all events with optional filtering and pagination
export const getEvents = async (options = {}) => {
  const { search, location, host, game, attendee, status, past, page, pageSize } = options;
  
  return simulateApiCall(async () => {
    // Apply filters
    let filteredEvents = [...eventStore];
    
    // Text search
    if (search) {
      filteredEvents = filteredEvents.filter(event => 
        searchInObject(event, search, ['title', 'description', 'location'])
      );
    }
    
    // Filter by location
    if (location) {
      filteredEvents = filteredEvents.filter(event => 
        event.location && event.location.toLowerCase().includes(location.toLowerCase())
      );
    }
    
    // Filter by host
    if (host) {
      filteredEvents = filteredEvents.filter(event => event.host === host);
    }
    
    // Filter by game
    if (game) {
      filteredEvents = filteredEvents.filter(event => 
        event.games && event.games.includes(game)
      );
    }
    
    // Filter by attendee
    if (attendee) {
      filteredEvents = filteredEvents.filter(event => 
        event.attendees && event.attendees.includes(attendee)
      );
    }
    
    // Filter by status
    if (status) {
      filteredEvents = filteredEvents.filter(event => event.status === status);
    }
    
    // Filter by past/upcoming
    if (past !== undefined) {
      const now = new Date();
      filteredEvents = filteredEvents.filter(event => {
        const eventDate = new Date(event.date);
        return past ? eventDate < now : eventDate >= now;
      });
    }
    
    // Apply pagination if requested
    if (page && pageSize) {
      return paginateItems(filteredEvents, page, pageSize);
    }
    
    return filteredEvents;
  });
};

// Get an event by ID
export const getEventById = async (eventId) => {
  return simulateApiCall(async () => {
    const event = eventStore.find(e => e.id === eventId);
    
    if (!event) {
      throw new Error(errorMessages.notFound);
    }
    
    return event;
  });
};

// Create a new event
export const createEvent = async (eventData, hostId) => {
  return simulateApiCall(async () => {
    // Validate required fields
    if (!eventData.title || !hostId || !eventData.date || !eventData.location) {
      throw new Error('Event title, host, date, and location are required');
    }
    
    // Create new event with defaults for missing fields
    const newEvent = {
      id: generateId('event'),
      title: eventData.title,
      description: eventData.description || `A ${eventData.title} event`,
      date: eventData.date,
      location: eventData.location,
      host: hostId,
      games: eventData.games || [],
      attendees: eventData.attendees || [hostId], // Host is automatically an attendee
      maxAttendees: eventData.maxAttendees || 10,
      status: eventData.status || 'scheduled'
    };
    
    // Add to store
    eventStore.push(newEvent);
    
    return newEvent;
  });
};

// Update an event
export const updateEvent = async (eventId, updates, hostId) => {
  return simulateApiCall(async () => {
    const eventIndex = eventStore.findIndex(e => e.id === eventId);
    
    if (eventIndex === -1) {
      throw new Error(errorMessages.notFound);
    }
    
    // Check if user is the host if hostId is provided
    if (hostId && eventStore[eventIndex].host !== hostId) {
      throw new Error(errorMessages.unauthorized);
    }
    
    // Update event (merging with existing data)
    eventStore[eventIndex] = {
      ...eventStore[eventIndex],
      ...updates,
      // Preserve ID and host
      id: eventId,
      host: eventStore[eventIndex].host
    };
    
    return eventStore[eventIndex];
  });
};

// Delete an event
export const deleteEvent = async (eventId, hostId) => {
  return simulateApiCall(async () => {
    const event = eventStore.find(e => e.id === eventId);
    
    if (!event) {
      throw new Error(errorMessages.notFound);
    }
    
    // Check if user is the host if hostId is provided
    if (hostId && event.host !== hostId) {
      throw new Error(errorMessages.unauthorized);
    }
    
    // Remove from store
    eventStore = eventStore.filter(e => e.id !== eventId);
    
    return { id: eventId, deleted: true };
  });
};

// Search events
export const searchEvents = async (query, options = {}) => {
  return simulateApiCall(async () => {
    if (!query && Object.keys(options).length === 0) {
      return [];
    }
    
    let matchedEvents = [...eventStore];
    
    // Text search
    if (query) {
      matchedEvents = matchedEvents.filter(event => 
        searchInObject(event, query, ['title', 'description', 'location'])
      );
    }
    
    // Apply additional filters from options
    if (options.location) {
      matchedEvents = matchedEvents.filter(event => 
        event.location && event.location.toLowerCase().includes(options.location.toLowerCase())
      );
    }
    
    if (options.game) {
      matchedEvents = matchedEvents.filter(event => 
        event.games && event.games.includes(options.game)
      );
    }
    
    // Filter by date range
    if (options.startDate) {
      const startDate = new Date(options.startDate);
      matchedEvents = matchedEvents.filter(event => 
        new Date(event.date) >= startDate
      );
    }
    
    if (options.endDate) {
      const endDate = new Date(options.endDate);
      matchedEvents = matchedEvents.filter(event => 
        new Date(event.date) <= endDate
      );
    }
    
    return matchedEvents;
  });
};

// Get events a user is attending
export const getUserEvents = async (userId) => {
  return simulateApiCall(async () => {
    const hostedEvents = eventStore.filter(event => event.host === userId);
    const attendingEvents = eventStore.filter(event => 
      event.attendees && event.attendees.includes(userId) && event.host !== userId
    );
    
    return { 
      hosted: hostedEvents,
      attending: attendingEvents
    };
  });
};

// RSVP to an event
export const rsvpToEvent = async (eventId, userId, attending = true) => {
  return simulateApiCall(async () => {
    const eventIndex = eventStore.findIndex(e => e.id === eventId);
    
    if (eventIndex === -1) {
      throw new Error(errorMessages.notFound);
    }
    
    const event = eventStore[eventIndex];
    
    // Check if event is already full
    if (attending && event.attendees.length >= event.maxAttendees) {
      throw new Error('Event is at maximum capacity');
    }
    
    // Update attendees list
    if (attending) {
      // Add user if not already attending
      if (!event.attendees.includes(userId)) {
        eventStore[eventIndex].attendees.push(userId);
      }
    } else {
      // Remove user if attending
      eventStore[eventIndex].attendees = event.attendees.filter(id => id !== userId);
    }
    
    return eventStore[eventIndex];
  });
};

// Get popular event locations
export const getEventLocations = async () => {
  return simulateApiCall(async () => {
    // Get unique locations from events
    const usedLocations = eventStore.map(event => event.location)
      .filter(location => location)
      .reduce((unique, location) => {
        if (!unique.includes(location)) {
          unique.push(location);
        }
        return unique;
      }, []);
    
    // Combine with predefined locations
    const allLocations = [...new Set([...usedLocations, ...eventLocations])];
    
    return allLocations;
  });
};

// Reset the event store (for testing purposes)
export const resetEventStore = () => {
  eventStore = clone(events);
};

// Export functions
export default {
  getEvents,
  getEventById,
  createEvent,
  updateEvent,
  deleteEvent,
  searchEvents,
  getUserEvents,
  rsvpToEvent,
  getEventLocations,
  resetEventStore
}; 