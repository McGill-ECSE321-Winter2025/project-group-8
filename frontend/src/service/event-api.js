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

// Register for an event (mock implementation)
export async function registerForEvent(attendeeId, eventId) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const responses = [
        { status: 200, ok: true, json: () => Promise.resolve({ message: "Success" }) },
        { status: 400, ok: false, json: () => Promise.resolve({ message: "Event is at full capacity!" }) },
        { status: 403, ok: false, json: () => Promise.resolve({ message: "You are already registered for this event!" }) }
      ];

      // Simulate random API response
      const randomResponse = responses[Math.floor(Math.random() * responses.length)];

      if (randomResponse.ok) {
        resolve(randomResponse);
      } else {
        reject(randomResponse);
      }
    }, 500); 
  });
}

// Create an event
export const createEvent = async (eventData) => {
  const formattedData = {
    ...eventData,
    dateTime: new Date(eventData.dateTime).toISOString(),
    maxParticipants: parseInt(eventData.maxParticipants, 10),
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

// Export mock API for direct access when needed
export { mockApi };
