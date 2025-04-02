/**
 * API Service
 * 
 * This file provides API functions for the application.
 * Currently using mock data during development.
 */

// Import mock API services
import mockApi from './mock-api';

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

// Search events
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

// Export mock API for direct access when needed
export { mockApi };