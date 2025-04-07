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
    const games = await apiClient(endpoint, { 
      method: "GET",
      skipPrefix: false // Should now use the /api prefix
    });
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

  console.log("createGame: Attempting to create game:", payload);
  
  try {
    // Use apiClient for the POST request
    const createdGame = await apiClient("/games", {
      method: "POST",
      body: payload,
      skipPrefix: false // Should now use the /api prefix
    });
    
    console.log("createGame: Successfully created game:", createdGame);
    return createdGame; // Return the created game object from backend
  } catch (error) {
    console.error("createGame: Failed to create game:", error);
    // Check if this is an authentication issue
    if (error.name === 'UnauthorizedError') {
      console.error("createGame: Authentication error - user not logged in or session expired");
    } else if (error.name === 'ForbiddenError') {
      console.error("createGame: Permission error - user does not have GAME_OWNER role");
    }
    
    // Log cookies state to help debug
    console.log("createGame: Cookie state at time of error:", {
      isAuthenticated: document.cookie.includes('isAuthenticated=true'),
      hasAccessToken: document.cookie.includes('accessToken='),
      allCookies: document.cookie
    });
    
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

  console.log("getGamesByOwner: Fetching games for owner:", ownerEmail);

  // Using the proper endpoint for fetching games by owner
  const endpoint = `/games?ownerId=${encodeURIComponent(ownerEmail)}`;
  console.log("getGamesByOwner: Using endpoint:", endpoint);

  try {
    // Log cookie state before making the request
    console.log("getGamesByOwner: Cookie state before fetch:", {
      isAuthenticated: document.cookie.includes('isAuthenticated=true'),
      hasAccessToken: document.cookie.includes('accessToken='),
      allCookies: document.cookie
    });
    
    // Use apiClient for the GET request with skipPrefix option
    const games = await apiClient(endpoint, { 
      method: "GET",
      skipPrefix: false
    });
    
    console.log(`getGamesByOwner: Successfully fetched ${games.length} games for owner ${ownerEmail}:`, games);
    return games;
  } catch (error) {
    console.error(`getGamesByOwner: Failed to fetch games for owner ${ownerEmail}:`, error);
    // Check specific error types
    if (error.name === 'UnauthorizedError') {
      console.error("getGamesByOwner: Authentication error - user not logged in or session expired");
    } else if (error.name === 'ForbiddenError') {
      console.error("getGamesByOwner: Permission error accessing games");
    }
    
    // Re-throw the specific error from apiClient
    throw error;
  }
};

// Removed duplicate/older deleteGame function
/**
 * Fetches a single game by its ID.
 * @param {number} id - Game ID
 * @returns {Promise<object>} Game object
 */
export const getGameById = async (id) => {
  const token = localStorage.getItem("token");

  const headers = {
    "Content-Type": "application/json",
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`http://localhost:8080/api/v1/games/${id}`, {
    method: "GET",
    headers,
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(
      `Failed to fetch game #${id}: ${response.status} ${response.statusText}\n${errorText}`
    );
  }

  return await response.json();
};



/**
 * Fetches all instances (physical copies) of a specific game.
 * Authentication might be required depending on backend setup.
 * @param {string|number} gameId - The ID of the game.
 * @returns {Promise<Array>} A promise that resolves to an array of game instance objects.
 * @throws {ApiError} For API-related errors.
 */
export const getGameInstances = async (gameId) => {
  if (!gameId) {
    throw new Error("Game ID is required to fetch instances.");
  }
  const endpoint = `/games/${gameId}/instances`;
  console.log("getGameInstances: Fetching instances for game:", gameId);

  try {
    const instances = await apiClient(endpoint, { 
      method: "GET",
      skipPrefix: false // Should now use the /api prefix
    });
    console.log(`getGameInstances: Successfully fetched ${instances.length} instances for game ${gameId}:`, instances);
    return instances;
  } catch (error) {
    console.error(`getGameInstances: Failed to fetch instances for game ${gameId}:`, error);
    throw error;
  }
};

/**
 * Fetches all reviews for a specific game.
 * Authentication might be required depending on backend setup.
 * @param {string|number} gameId - The ID of the game.
 * @returns {Promise<Array>} A promise that resolves to an array of review objects.
 * @throws {ApiError} For API-related errors.
 */
export const getGameReviews = async (gameId) => {
  if (!gameId) {
    throw new Error("Game ID is required to fetch reviews.");
  }
  const endpoint = `/games/${gameId}/reviews`;
  console.log("getGameReviews: Fetching reviews for game:", gameId);

  try {
    const reviews = await apiClient(endpoint, { 
      method: "GET",
      skipPrefix: false // Should now use the /api prefix
    });
    console.log(`getGameReviews: Successfully fetched ${reviews.length} reviews for game ${gameId}:`, reviews);
    return reviews;
  } catch (error) {
    console.error(`getGameReviews: Failed to fetch reviews for game ${gameId}:`, error);
    throw error;
  }
};



/**
 * Deletes a game by its ID. Requires authentication.
 * @param {string|number} gameId - The ID of the game to delete.
 * @returns {Promise<void>} A promise that resolves when the game is deleted.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ForbiddenError} If the user is not allowed to delete the game (e.g., not the owner).
 * @throws {ApiError} For other API-related errors.
 */
export const deleteGame = async (gameId) => {
  if (!gameId) {
    throw new Error("Game ID is required to delete the game.");
  }
  const endpoint = `/games/${gameId}`;
  console.log("deleteGame: Attempting to delete game:", gameId);

  try {
    await apiClient(endpoint, {
      method: "DELETE",
      skipPrefix: false // Should now use the /api prefix
    });
    console.log(`deleteGame: Successfully deleted game ${gameId}`);
  } catch (error) {
    console.error(`deleteGame: Failed to delete game ${gameId}:`, error);
    // Re-throw the specific error from apiClient
    throw error;
  }
};

// Add other game-related API functions here as needed, using apiClient
// e.g., getGameById, updateGame, deleteGame, getGameReviews, submitReview etc.
