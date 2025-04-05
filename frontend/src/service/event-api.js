/**
 * API Service
 * 
 * This file provides API functions for the application.
 * Includes both mock data and real API endpoints.
 */

// Import mock API services
import mockApi from './mock-api';

// Helper function to add Authorization header
const fetchWithAuth = async (url, options = {}) => {
  const token = localStorage.getItem("authToken");
  const headers = { ...(options.headers || {}) }; // Clone headers, ensure headers object exists

  // Define public paths that don't need the token
  const publicPaths = [
    "/auth/login", // Login endpoint
    "/account",   // Registration endpoint (POST)
  ];

  // Check if the request is for a public path
  // Assumes full URLs are passed, checks if URL string *includes* the path
  const isPublicPath = publicPaths.some(path =>
    url.includes(path) && (path !== "/account" || (options.method || "GET").toUpperCase() === "POST")
  );

  if (token && !isPublicPath) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  // Ensure Content-Type is set if not already present for methods that might have a body
  // Only add if body exists and Content-Type is not already set
  if (options.body && !headers["Content-Type"] && (options.method === "POST" || options.method === "PUT" || options.method === "PATCH")) {
     headers["Content-Type"] = "application/json";
  }


  const fetchOptions = {
    ...options,
    headers,
  };

  // Make the actual fetch call
  return fetch(url, fetchOptions);
};


// === USER API FUNCTIONS ===

// Search users
export const searchUsers = async (query, options = {}) => {
  console.log(`Searching users with query: "${query}"`, options);
  
  // Use the mock API service
  const response = await mockApi.users.searchUsers(query, options);
  
  if (response.status === 'error') {
    console.error('Error searching users:', response.error);
    throw new Error(response.error.message);
  }
  
  return response.data;
};

// Fetch user recommendations
export const fetchUserRecommendations = async (userId) => {
  console.log("Fetching user recommendations for user:", userId);
  
  // If no userId is provided, use a default for development
  const currentUserId = userId || 'user-1';
  
  // Use the mock API service
  const response = await mockApi.users.getRecommendedUsers(currentUserId);
  
  if (response.status === 'error') {
    console.error('Error fetching recommendations:', response.error);
    throw new Error(response.error.message);
  }
  
  return response.data;
};

// Get user by ID
export const getUserById = async (userId) => {
  console.log("Fetching user details for:", userId);
  
  // Use the mock API service
  const response = await mockApi.users.getUserById(userId);
  
  if (response.status === 'error') {
    console.error('Error fetching user:', response.error);
    throw new Error(response.error.message);
  }
  
  return response.data;
};

// Search games
export const searchGames = async (query, options = {}) => {
  console.log(`Searching games with query: "${query}"`, options);
  
  // Use the mock API service
  const response = await mockApi.games.searchGames(query, options);
  
  if (response.status === 'error') {
    console.error('Error searching games:', response.error);
    throw new Error(response.error.message);
  }
  
  return response.data;
};

// Search events (mock version)
export const searchEvents = async (query, options = {}) => {
  console.log(`Searching events with query: "${query}"`, options);
  
  // Use the mock API service
  const response = await mockApi.events.searchEvents(query, options);
  
  if (response.status === 'error') {
    console.error('Error searching events:', response.error);
    throw new Error(response.error.message);
  }
  
  return response.data;
};

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

  const response = await fetchWithAuth("http://localhost:8080/events", {
    method: "POST",
    // Content-Type is handled by fetchWithAuth if body exists
    body: JSON.stringify(formattedData),
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || "Failed to create event");
  }

  return response.json();
};

export const getAccountInfo = async (email) => {
  // fetchWithAuth will automatically add the Authorization header if a token exists
  const response = await fetchWithAuth(`http://localhost:8080/account/${encodeURIComponent(email)}`, {
    method: "GET",
    // Headers are handled by fetchWithAuth
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
  const response = await fetchWithAuth(`http://localhost:8080/events/by-title?title=${encodeURIComponent(title)}`, {
    method: "GET",
    // Headers are handled by fetchWithAuth
  });

  if (!response.ok) {
    throw new Error("Failed to search events");
  }

  return response.json();
};

// Unregister from an event
export async function unregisterFromEvent(registrationId) {
  const response = await fetchWithAuth(`/registrations/${registrationId}`, {
    method: "DELETE",
    // Headers are handled by fetchWithAuth
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(errorData.message || "Failed to unregister");
  }

  return response.json(); // Assuming the backend returns some response data
}
