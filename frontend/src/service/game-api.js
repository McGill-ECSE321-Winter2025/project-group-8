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
 * @param {string} [gameData.condition] - Physical condition of the game copy (optional).
 * @param {string} [gameData.location] - Location where the game is stored (optional).
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
    // Include instance-specific fields
    condition: gameData.condition || "Excellent", // Default value
    location: gameData.location || "Home", // Default value
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
     console.warn("getGamesByOwner: Owner email is missing");
     return []; // Return empty array instead of throwing
  }

  console.log("getGamesByOwner: Fetching games for owner:", ownerEmail);
  
  // Handle if email is accidentally passed as an email object or with unnecessary formats
  const cleanEmail = ownerEmail.toString().trim();
  if (!cleanEmail) {
    console.warn("getGamesByOwner: Owner email is empty after cleaning");
    return []; // Return empty array instead of throwing
  }

  // Using the proper endpoint for fetching games by owner
  const endpoint = `/games?ownerId=${encodeURIComponent(cleanEmail)}`;
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
      skipPrefix: false,
      timeout: 8000 // Set a reasonable timeout to prevent hanging
    });
    
    // Ensure we return an array
    if (!Array.isArray(games)) {
      console.warn(`getGamesByOwner: Response is not an array for ${cleanEmail}:`, games);
      return [];
    }
    
    console.log(`getGamesByOwner: Successfully fetched ${games.length} games for owner ${cleanEmail}:`, games);
    return games;
  } catch (error) {
    console.error(`getGamesByOwner: Failed to fetch games for owner ${cleanEmail}:`, error);
    // Check specific error types
    if (error.name === 'UnauthorizedError') {
      console.error("getGamesByOwner: Authentication error - user not logged in or session expired");
    } else if (error.name === 'ForbiddenError') {
      console.error("getGamesByOwner: Permission error accessing games");
    } else if (error.name === 'TimeoutError') {
      console.error("getGamesByOwner: Request timed out, possible server issue");
    }
    
    // Return empty array instead of throwing to prevent UI from breaking
    return [];
  }
};

/**
 * Fetches a single game by its ID.
 * @param {number} id - Game ID
 * @returns {Promise<object>} Game object
 */
export const getGameById = async (id) => {
  if (!id) {
    throw new Error("Game ID is required.");
  }
  
  const endpoint = `/games/${id}`;
  console.log(`getGameById: Fetching game with ID ${id}`);
  
  try {
    const game = await apiClient(endpoint, { 
      method: "GET",
      skipPrefix: false // Use /api prefix
    });
    console.log(`getGameById: Successfully fetched game ${id}:`, game);
    return game;
  } catch (error) {
    console.error(`getGameById: Failed to fetch game ${id}:`, error);
    throw error;
  }
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
 * Submits a new review for a game.
 * @param {Object} reviewData - The review data to submit
 * @param {number} reviewData.rating - Rating from 1-5
 * @param {string} reviewData.comment - Review comment text
 * @param {number} reviewData.gameId - ID of the game being reviewed
 * @param {string} [reviewData.reviewerId] - Email of the reviewer (optional, uses current user if not provided)
 * @returns {Promise<Object>} A promise that resolves to the created review
 * @throws {ApiError} For API-related errors
 */
export const submitReview = async (reviewData) => {
  if (!reviewData.gameId) {
    throw new Error("Game ID is required to submit a review.");
  }

  if (!reviewData.rating) {
    throw new Error("Rating is required to submit a review.");
  }

  console.log("submitReview: Submitting review:", reviewData);

  try {
    const response = await apiClient('/reviews', {
      method: "POST",
      skipPrefix: false,
      body: reviewData
    });
    console.log("submitReview: Successfully submitted review:", response);
    return response;
  } catch (error) {
    console.error("submitReview: Failed to submit review:", error);
    throw error;
  }
};

/**
 * Updates an existing review.
 * @param {number} reviewId - ID of the review to update
 * @param {Object} reviewData - Updated review data
 * @returns {Promise<Object>} A promise that resolves to the updated review
 * @throws {ApiError} For API-related errors
 */
export const updateReview = async (reviewId, reviewData) => {
  if (!reviewId) {
    throw new Error("Review ID is required to update a review.");
  }

  console.log(`updateReview: Updating review ${reviewId}:`, reviewData);

  try {
    const response = await apiClient(`/reviews/${reviewId}`, {
      method: "PUT",
      skipPrefix: false,
      body: reviewData
    });
    console.log(`updateReview: Successfully updated review ${reviewId}:`, response);
    return response;
  } catch (error) {
    console.error(`updateReview: Failed to update review ${reviewId}:`, error);
    throw error;
  }
};

/**
 * Deletes a review by its ID.
 * @param {number} reviewId - ID of the review to delete
 * @returns {Promise<Object>} A promise that resolves when the review is deleted
 * @throws {ApiError} For API-related errors
 */
export const deleteReview = async (reviewId) => {
  if (!reviewId) {
    throw new Error("Review ID is required to delete a review.");
  }

  console.log(`deleteReview: Deleting review ${reviewId}`);

  try {
    const response = await apiClient(`/reviews/${reviewId}`, {
      method: "DELETE",
      skipPrefix: false
    });
    console.log(`deleteReview: Successfully deleted review ${reviewId}`);
    return response;
  } catch (error) {
    console.error(`deleteReview: Failed to delete review ${reviewId}:`, error);
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

/**
 * Updates a game instance
 * @param {number} instanceId - ID of the instance to update
 * @param {object} data - Updated instance data
 * @returns {Promise<Object>} - Updated instance data
 */
export const updateGameInstance = async (instanceId, data) => {
  if (!instanceId) {
    throw new Error("Instance ID is required to update game instance.");
  }
  
  if (!data.gameId) {
    throw new Error("Game ID is required to update game instance.");
  }
  
  const endpoint = `/games/${data.gameId}/instances/${instanceId}`;
  console.log(`updateGameInstance: Updating instance ${instanceId}:`, data);
  
  try {
    const response = await apiClient(endpoint, { 
      method: "PUT",
      skipPrefix: false,
      body: data
    });
    console.log(`updateGameInstance: Successfully updated instance ${instanceId}:`, response);
    return response;
  } catch (error) {
    console.error(`updateGameInstance: Failed to update instance ${instanceId}:`, error);
    throw error;
  }
};

/**
 * Creates a new game instance
 * @param {number} gameId - ID of the game to create an instance for
 * @param {object} data - Instance data (condition, location, ownerId)
 * @returns {Promise<Object>} - Created instance data
 */
export const createGameInstance = async (gameId, data) => {
  if (!gameId) {
    throw new Error("Game ID is required to create game instance.");
  }
  
  // Use the pattern consistent with getGameInstances
  const endpoint = `/games/${gameId}/instances`;
  console.log(`createGameInstance: Creating instance for game ${gameId}:`, data);
  
  try {
    const response = await apiClient(endpoint, { 
      method: "POST",
      skipPrefix: false,
      body: {
        ...data,
        gameId: gameId
      }
    });
    console.log(`createGameInstance: Successfully created instance for game ${gameId}:`, response);
    return response;
  } catch (error) {
    console.error(`createGameInstance: Failed to create instance for game ${gameId}:`, error);
    throw error;
  }
};

/**
 * Updates an existing game by ID. Requires authentication.
 * @param {number} gameId - The ID of the game to update
 * @param {object} gameData - The updated game data
 * @param {string} gameData.name - Game name
 * @param {number} gameData.minPlayers - Min players
 * @param {number} gameData.maxPlayers - Max players
 * @param {string} [gameData.image] - Image URL (optional)
 * @param {string} [gameData.category] - Category (optional)
 * @returns {Promise<object>} A promise that resolves to the updated game object
 * @throws {UnauthorizedError} If the user is not authenticated
 * @throws {ForbiddenError} If the user is not allowed to update this game
 * @throws {ApiError} For other API-related errors
 */
export const updateGame = async (gameId, gameData) => {
  if (!gameId) {
    throw new Error("Game ID is required for updating a game");
  }
  
  const payload = {
    ...gameData,
    minPlayers: parseInt(gameData.minPlayers, 10),
    maxPlayers: parseInt(gameData.maxPlayers, 10)
  };
  
  console.log(`updateGame: Attempting to update game ${gameId}:`, payload);
  
  try {
    const endpoint = `/games/${gameId}`;
    const updatedGame = await apiClient(endpoint, {
      method: "PUT",
      body: payload,
      skipPrefix: false
    });
    
    console.log(`updateGame: Successfully updated game ${gameId}:`, updatedGame);
    return updatedGame;
  } catch (error) {
    console.error(`updateGame: Failed to update game ${gameId}:`, error);
    throw error;
  }
};

/**
 * Deletes a game instance by its ID. Requires authentication.
 * @param {number} gameId - The ID of the game.
 * @param {number} instanceId - The ID of the instance to delete.
 * @returns {Promise<void>} A promise that resolves when the instance is deleted.
 * @throws {UnauthorizedError} If the user is not authenticated.
 * @throws {ForbiddenError} If the user is not allowed to delete the instance (e.g., not the owner).
 * @throws {ApiError} For other API-related errors.
 */
export const deleteGameInstance = async (gameId, instanceId) => {
  if (!gameId || !instanceId) {
    throw new Error("Game ID and Instance ID are required to delete the instance.");
  }
  const endpoint = `/games/${gameId}/instances/${instanceId}`;
  console.log("deleteGameInstance: Attempting to delete instance:", instanceId);

  try {
    await apiClient(endpoint, {
      method: "DELETE",
      skipPrefix: false
    });
    console.log(`deleteGameInstance: Successfully deleted instance ${instanceId}`);
  } catch (error) {
    console.error(`deleteGameInstance: Failed to delete instance ${instanceId}:`, error);
    throw error;
  }
};

/**
 * Checks if a game is available for a specific date range.
 * @param {number} gameId - The ID of the game to check.
 * @param {Date} startDate - The start date of the borrowing period.
 * @param {Date} endDate - The end date of the borrowing period.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the game is available.
 * @throws {ApiError} For API-related errors.
 */
export const checkGameAvailability = async (gameId, startDate, endDate) => {
  if (!gameId) {
    throw new Error("Game ID is required to check availability.");
  }
  
  if (!startDate || !endDate) {
    throw new Error("Start date and end date are required to check availability.");
  }
  
  // Convert dates to milliseconds for API call
  const startTimestamp = startDate.getTime();
  const endTimestamp = endDate.getTime();
  
  const endpoint = `/games/${gameId}/availability?startDate=${startTimestamp}&endDate=${endTimestamp}`;
  console.log(`checkGameAvailability: Checking availability for game ${gameId} from ${startDate} to ${endDate}`);

  try {
    const isAvailable = await apiClient(endpoint, { 
      method: "GET",
      skipPrefix: false
    });
    console.log(`checkGameAvailability: Game ${gameId} is ${isAvailable ? 'available' : 'not available'} for the requested period`);
    return isAvailable;
  } catch (error) {
    console.error(`checkGameAvailability: Failed to check availability for game ${gameId}:`, error);
    // Default to false (unavailable) on error to be safe
    return false;
  }
};

// Add other game-related API functions here as needed, using apiClient
// e.g., getGameById, updateGame, deleteGame, getGameReviews, submitReview etc.
