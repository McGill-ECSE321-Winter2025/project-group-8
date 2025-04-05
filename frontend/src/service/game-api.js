import apiClient from './apiClient'; // Import the centralized API client

/**
 * Searches for games based on the provided criteria.
 * Authentication may or may not be required depending on backend implementation.
 * @param {object} criteria - The search criteria.
 * @param {string} [criteria.name] - Part of the game name to search for.
 * @param {string} [criteria.category] - The category to filter by.
 * @param {string|number} [criteria.minPlayers] - Minimum number of players.
 * @param {string|number} [criteria.maxPlayers] - Maximum number of players.
 * @returns {Promise<Array>} A promise that resolves to an array of game objects.
 * @throws {ApiError} For API-related errors.
 */
export const searchGames = async (criteria) => {
  const queryParams = new URLSearchParams();

  // Map frontend criteria names to backend parameter names
  if (criteria.name) queryParams.append('name', criteria.name);
  if (criteria.category) queryParams.append('category', criteria.category);
  if (criteria.minPlayers) queryParams.append('minPlayers', criteria.minPlayers);
  if (criteria.maxPlayers) queryParams.append('maxPlayers', criteria.maxPlayers);
  // Add other potential criteria here if needed

  const endpoint = `/games/search?${queryParams.toString()}`;

  try {
    // Use apiClient - it handles credentials automatically if needed
    const games = await apiClient(endpoint, { method: "GET" });
    return games;
  } catch (error) {
    console.error("Failed to fetch games:", error);
    // Re-throw the error (could be ApiError, UnauthorizedError, etc.)
    throw error;
  }
};

/**
 * Creates a new game. Requires authentication (via HttpOnly cookie).
 * The backend should identify the owner based on the authenticated user session.
 * @param {object} gameData - The game data.
 * @param {string} gameData.name - Game name.
 * @param {number} gameData.minPlayers - Min players.
 * @param {number} gameData.maxPlayers - Max players.
 * @param {string} [gameData.image] - Image URL (optional).
 * @param {string} [gameData.category] - Category (optional).
 * @returns {Promise<object>} A promise that resolves to the created game object.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ForbiddenError} If the user is not allowed (e.g., not a game owner account type).
 * @throws {ApiError} For other API-related errors.
 */
export const createGame = async (gameData) => {
  // Remove ownerId/ownerEmail from payload - backend identifies owner via cookie/session
  const payload = {
    ...gameData,
    minPlayers: parseInt(gameData.minPlayers, 10), // Ensure numbers are integers
    maxPlayers: parseInt(gameData.maxPlayers, 10),
  };

  try {
    // Use apiClient for the POST request
    const createdGame = await apiClient("/games", {
      method: "POST",
      body: payload,
    });
    return createdGame; // Return the created game object from backend
  } catch (error) {
    console.error("Failed to create game:", error);
    // Re-throw the specific error from apiClient
    throw error;
  }
};

/**
 * Fetches all games owned by a specific user (identified by email).
 * Requires authentication (via HttpOnly cookie).
 * @param {string} ownerEmail - The email of the owner whose games are to be fetched.
 * @returns {Promise<Array>} A promise that resolves to an array of game objects owned by the user.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ForbiddenError} If the user is not allowed to view these games.
 * @throws {ApiError} For other API-related errors.
 */
export const getGamesByOwner = async (ownerEmail) => {
  if (!ownerEmail) {
     throw new Error("Owner email is required to fetch games.");
  }

  // The backend endpoint uses email as the identifier in the path
  const endpoint = `/users/${encodeURIComponent(ownerEmail)}/games`;

  try {
    // Use apiClient for the GET request
    const games = await apiClient(endpoint, { method: "GET" });
    return games;
  } catch (error) {
    console.error(`Failed to fetch games for owner ${ownerEmail}:`, error);
    // Re-throw the specific error from apiClient
    throw error;
  }
};


// Add other game-related API functions here as needed, using apiClient
// e.g., getGameById, updateGame, deleteGame, getGameReviews, submitReview etc.
