/**
 * API Service
 *
 * This file provides API functions for the application.
 * Includes both mock data and real API endpoints.
 */

// Define API_BASE_URL centrally (or import if moved)
const API_BASE_URL = "http://localhost:8080/api/v1";

import apiClient from './apiClient'; // Import the centralized API client

// === EVENT API FUNCTIONS ===

/**
 * Fetches all events.
 * Requires authentication (via HttpOnly cookie).
 * @returns {Promise<Array>} A promise that resolves to an array of event objects.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ApiError} For other API-related errors.
 */
export const getAllEvents = async () => {
  try {
    const events = await apiClient("/events", { method: "GET" });
    return events;
  } catch (error) {
    console.error("Failed to fetch events:", error);
    throw error; // Re-throw the specific error from apiClient
  }
};

/**
 * Registers the current authenticated user for an event.
 * Requires authentication (via HttpOnly cookie). Backend identifies attendee from session.
 * @param {string} eventId - The UUID of the event to register for.
 * @returns {Promise<object>} A promise that resolves to the created registration object.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ForbiddenError} If registration is not allowed (e.g., event full, already registered).
 * @throws {ApiError} For other API-related errors.
 */
export const registerForEvent = async (eventId) => {
  if (!eventId) throw new Error("Event ID is required.");

  // Payload might only need the eventId if backend identifies user from cookie.
  // Adjust if backend still requires attendeeId explicitly.
  const payload = {
    eventId: eventId 
    // attendeeId might not be needed if derived from session on backend
  };

  try {
    const registration = await apiClient("/registrations", {
      method: "POST",
      body: JSON.stringify(payload), // Ensure body is stringified if apiClient doesn't do it implicitly
                                     // (Though our current apiClient does stringify)
    });
    return registration; // Return the created registration object
  } catch (error) {
    console.error(`Failed to register for event ${eventId}:`, error);
    throw error; // Re-throw the specific error from apiClient
  }
};

/**
 * Creates a new event. Requires authentication (via HttpOnly cookie).
 * Backend identifies the host from the session.
 * @param {object} eventData - Data for the new event.
 * @param {string} eventData.title
 * @param {string} eventData.dateTime - ISO 8601 format string or similar expected by backend.
 * @param {string} eventData.location
 * @param {string} eventData.description
 * @param {number} eventData.maxParticipants
 * @param {number} eventData.featuredGameId - ID of the featured game.
 * @returns {Promise<object>} A promise that resolves to the created event object.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ForbiddenError} If the user is not allowed to create events.
 * @throws {ApiError} For other API-related errors.
 */
export const createEvent = async (eventData) => {
  if (!eventData.featuredGameId) {
      throw new Error("Featured Game ID is missing in event data for createEvent");
  }

  // Construct the payload according to backend DTO expectations
  // Host is determined by the backend via token/cookie
  const payload = {
    title: eventData.title,
    // Assuming backend expects full dateTime string or handles parsing YYYY-MM-DD
    dateTime: eventData.dateTime, 
    location: eventData.location,
    description: eventData.description,
    maxParticipants: parseInt(eventData.maxParticipants, 10),
    featuredGame: { id: parseInt(eventData.featuredGameId, 10) } // Send game ID nested
  };

  try {
    const createdEvent = await apiClient("/events", {
      method: "POST",
      body: payload, 
    });
    return createdEvent;
  } catch (error) {
    console.error("Failed to create event:", error);
    throw error; // Re-throw the specific error from apiClient
  }
};

/**
 * Searches for events by title.
 * Requires authentication (via HttpOnly cookie).
 * @param {string} title - The title (or part of it) to search for.
 * @returns {Promise<Array>} A promise that resolves to an array of matching event objects.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ApiError} For other API-related errors.
 */
export const searchEventsByTitle = async (title) => {
  try {
    const events = await apiClient(`/events/by-title?title=${encodeURIComponent(title)}`, {
      method: "GET",
    });
    return events;
  } catch (error) {
    console.error(`Failed to search events by title "${title}":`, error);
    throw error; // Re-throw the specific error from apiClient
  }
};

/**
 * Fetches all events hosted by a specific user email.
 * Requires authentication.
 * @param {string} hostEmail - The email of the host user.
 * @returns {Promise<Array>} A promise that resolves to an array of event objects.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ApiError} For other API-related errors.
 */
export const getEventsByHostEmail = async (hostEmail) => {
  if (!hostEmail) {
     throw new Error("Host email is required to fetch hosted events.");
  }

  try {
    const events = await apiClient(`/events/by-host-email?hostEmail=${encodeURIComponent(hostEmail)}`, {
      method: "GET",
    });
    return events;
  } catch (error) {
    console.error(`Failed to fetch events for host ${hostEmail}:`, error);
    throw error; // Re-throw the specific error from apiClient
  }
};

/**
 * Unregisters the current authenticated user from an event using the registration ID.
 * Requires authentication (via HttpOnly cookie).
 * @param {string|number} registrationId - The ID of the registration record to delete.
 * @returns {Promise<object|null>} A promise that resolves (often with no content) on success.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ForbiddenError} If the user is not allowed to delete this registration.
 * @throws {ApiError} For other API-related errors (e.g., 404 Not Found).
 */
export const unregisterFromEvent = async (registrationId) => {
  if (!registrationId) {
    throw new Error("Registration ID is required to unregister from an event");
  }
  
  try {
    // DELETE request to remove the registration
    const result = await apiClient(`/registrations/${registrationId}`, {
      method: "DELETE"
    });
    return result; // Might be empty if server returns 204 No Content
  } catch (error) {
    console.error(`Failed to unregister from event (registration ${registrationId}):`, error);
    throw error; // Re-throw the specific error from apiClient
  }
};
