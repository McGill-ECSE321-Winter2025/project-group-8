/**
 * API Service
 * 
 * This file provides API functions for the application.
 * Includes both mock data and real API endpoints.
 */

// Removed mock USER API FUNCTIONS
// Removed mock searchGames function
// Removed mock searchEvents function

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
    const response = await fetch("http://localhost:8080/api/v1/events", {
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
    const response = await fetch("http://localhost:8080/api/v1/registrations", {
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

  const response = await fetch("http://localhost:8080/api/v1/events", {
    method: "POST",
    headers: headers, // Use headers object
    body: JSON.stringify(payload), // Send the structured payload
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || "Failed to create event");
  }

  return response.json();
};

export const getAccountInfo = async (email) => {
  const token = localStorage.getItem("token");
  if (!token) {
    // If used on a protected page, this shouldn't happen, but good practice
    throw new Error("Authentication token not found. Please log in.");
  }
  if (!email) {
     throw new Error("Email is required to fetch account info.");
  }

  // Define API_BASE_URL locally within this function as a workaround
  const API_BASE_URL = "http://localhost:8080/api/v1";

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}` // Add the token
  };

  const response = await fetch(`${API_BASE_URL}/account/${encodeURIComponent(email)}`, {
    method: "GET",
    headers: headers,
  });

  if (!response.ok) {
    const errorText = await response.text(); // Get error text for better debugging
    throw new Error(`Failed to fetch account info: ${response.status} ${response.statusText} - ${errorText}`);
  }

  return response.json();
};

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

  const response = await fetch(`http://localhost:8080/api/v1/events/by-title?title=${encodeURIComponent(title)}`, {
    method: "GET",
    headers: headers, // Use headers object
  });

  if (!response.ok) {
    throw new Error("Failed to search events");
  }

  return response.json();
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
  const response = await fetch(`http://localhost:8080/api/v1/registrations/${registrationId}`, {
    method: "DELETE",
    headers: headers, // Use headers object
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || "Failed to unregister");
  }

  return response.json(); // Assuming the backend returns some response data
}
