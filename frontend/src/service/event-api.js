/**
 * API Service
 *
 * This file provides API functions for the application.
 * Includes both mock data and real API endpoints.
 */

// Define API_BASE_URL centrally (or import if moved)
const API_BASE_URL = "http://localhost:8080/api/v1";

// === EVENT API FUNCTIONS ===

// Get all events (real API implementation)
export const getAllEvents = async () => {
  const token = localStorage.getItem("token");
  const headers = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/events`, { // Use API_BASE_URL
      method: "GET",
      headers: headers,
    });

    if (!response.ok) {
      const errorBody = await response.text();
      console.error("Backend error fetching events:", errorBody);
      throw new Error(`HTTP error ${response.status}: ${response.statusText}`);
    }
    return await response.json();
  } catch (error) {
    console.error("Failed to fetch events:", error);
    throw error;
  }
};

// Register for an event (real API implementation)
export const registerForEvent = async (eventId) => {
  const token = localStorage.getItem("token");
  const attendeeId = localStorage.getItem("userId"); // Get logged-in user's ID

  if (!token) throw new Error("Authentication token not found.");
  if (!attendeeId) throw new Error("User ID not found.");
  if (!eventId) throw new Error("Event ID is required.");

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  const payload = {
    attendeeId: parseInt(attendeeId, 10), // Ensure it's a number
    eventId: eventId // Should be the UUID string
    // registrationDate can be omitted, backend likely sets it
  };

  try {
    const response = await fetch(`${API_BASE_URL}/registrations`, { // Use API_BASE_URL
      method: "POST",
      headers: headers,
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
      try {
          const errorBody = await response.json();
          errorMsg = errorBody.message || errorMsg;
      } catch (e) { /* Ignore parsing error */ }
      console.error("Backend error registering for event:", errorMsg);
      throw new Error(errorMsg);
    }
    return await response.json(); // Return the created registration object
  } catch (error) {
    console.error("Failed to register for event:", error);
    throw error;
  }
};

// Create an event
export const createEvent = async (eventData) => {
  // Expects eventData to contain featuredGameId now
  if (!eventData.featuredGameId) {
      throw new Error("Featured Game ID is missing in event data for createEvent");
  }

  // Construct the payload according to backend DTO expectations
  const payload = {
    title: eventData.title,
    // Extract only the date part (YYYY-MM-DD) from the datetime-local input string
    dateTime: eventData.dateTime.substring(0, 10),
    location: eventData.location,
    description: eventData.description,
    maxParticipants: parseInt(eventData.maxParticipants, 10),
    featuredGame: { id: parseInt(eventData.featuredGameId, 10) } // Send game ID nested
    // Host is determined by the backend via token, not sent from frontend
  };

  const token = localStorage.getItem("token"); // Get token
  const headers = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`; // Add token if exists
  }

  const response = await fetch(`${API_BASE_URL}/events`, { // Use API_BASE_URL
    method: "POST",
    headers: headers, // Use headers object
    body: JSON.stringify(payload), // Send the structured payload
  });

  if (!response.ok) {
    // Attempt to parse JSON error first
    let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
     try {
         const errorData = await response.json();
         errorMsg = errorData.message || errorMsg;
     } catch (e) {
         // Fallback to text if JSON parsing fails
         try {
             const errorText = await response.text();
             errorMsg = errorText || errorMsg;
         } catch (e2) { /* Ignore further errors */ }
     }
    throw new Error(errorMsg);
  }

  return response.json();
};

// getAccountInfo moved to user-api.js

// Search events by title (actual API implementation)
// TODO: Consider renaming or consolidating with a more general event search function
export const searchEventsByTitle = async (title) => {
  const token = localStorage.getItem("token"); // Get token
  const headers = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`; // Add token if exists
  }

  const response = await fetch(`${API_BASE_URL}/events/by-title?title=${encodeURIComponent(title)}`, { // Use API_BASE_URL
    method: "GET",
    headers: headers, // Use headers object
  });

  if (!response.ok) {
    throw new Error("Failed to search events");
  }

  return response.json();
};

/**
 * Fetches all events hosted by a specific user email.
 * Requires authentication.
 * @param {string} hostEmail - The email of the host user.
 * @returns {Promise<Array>} A promise that resolves to an array of event objects.
 */
export const getEventsByHostEmail = async (hostEmail) => {
  const token = localStorage.getItem("token");
  if (!token) {
    throw new Error("Authentication token not found. Please log in.");
  }
  if (!hostEmail) {
     throw new Error("Host email is required to fetch hosted events.");
  }

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  // Use the updated endpoint path and parameter name
  const url = `${API_BASE_URL}/events/by-host-email?hostEmail=${encodeURIComponent(hostEmail)}`;

  try {
    const response = await fetch(url, {
      method: "GET",
      headers: headers,
    });

    if (!response.ok) {
      let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
       try {
           const errorBody = await response.json();
           errorMsg = errorBody.message || errorMsg;
       } catch (e) { /* Ignore parsing error */ }
      console.error("Backend error fetching hosted events:", errorMsg);
      throw new Error(errorMsg);
    }
    return await response.json();
  } catch (error) {
    console.error("Failed to fetch hosted events:", error);
    throw error;
  }
};

/**
 * Fetches details for a single event by its ID.
 * Requires authentication.
 * @param {string} eventId - The UUID of the event.
 * @returns {Promise<object>} A promise that resolves to the event object.
 */
export const getEventById = async (eventId) => {
  const token = localStorage.getItem("token");
  if (!token) {
    throw new Error("Authentication token not found. Please log in.");
  }
  if (!eventId) {
     throw new Error("Event ID is required.");
  }

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  const url = `${API_BASE_URL}/events/${eventId}`; // Use eventId directly in path

  try {
    const response = await fetch(url, {
      method: "GET",
      headers: headers,
    });

    if (!response.ok) {
      let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
       try {
           const errorBody = await response.json();
           errorMsg = errorBody.message || errorMsg;
       } catch (e) { /* Ignore parsing error */ }
      console.error("Backend error fetching event by ID:", errorMsg);
      throw new Error(errorMsg);
    }
    return await response.json(); // Returns EventResponse DTO
  } catch (error) {
    console.error("Failed to fetch event by ID:", error);
    throw error;
  }
};


// Unregister from an event
export async function unregisterFromEvent(registrationId) {
  const token = localStorage.getItem("token"); // Get token
  const headers = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`; // Add token if exists
  }

  // TODO: Verify the actual endpoint URL structure for unregistering - Assuming it's correct
  const response = await fetch(`${API_BASE_URL}/registrations/${registrationId}`, { // Use API_BASE_URL
    method: "DELETE",
    headers: headers, // Use headers object
  });

  if (!response.ok) {
    // Backend returns plain text error messages for this endpoint
    const errorText = await response.text();
    console.error("Backend error unregistering:", errorText);
    throw new Error(errorText || `HTTP error ${response.status}: ${response.statusText}`);
  }

  // Backend returns plain text success message
  return response.text();
}
