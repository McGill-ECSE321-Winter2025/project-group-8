const API_BASE_URL = "http://localhost:8080/api/v1";

/**
 * Searches for games based on the provided criteria.
 * @param {object} criteria - The search criteria.
 * @param {string} [criteria.name] - Part of the game name to search for.
 * @param {string} [criteria.category] - The category to filter by.
 * @param {string|number} [criteria.minPlayers] - Minimum number of players.
 * @param {string|number} [criteria.maxPlayers] - Maximum number of players.
 * @returns {Promise<Array>} A promise that resolves to an array of game objects.
 */
export const searchGames = async (criteria) => {
  const queryParams = new URLSearchParams();

  // Map frontend criteria names to backend parameter names
  if (criteria.name) queryParams.append('name', criteria.name);
  if (criteria.category) queryParams.append('category', criteria.category);
  if (criteria.minPlayers) queryParams.append('minPlayers', criteria.minPlayers);
  if (criteria.maxPlayers) queryParams.append('maxPlayers', criteria.maxPlayers);
  // Add other potential criteria here if needed (e.g., minRating, available, ownerId, sort, order)

  const url = `${API_BASE_URL}/games/search?${queryParams.toString()}`;

  // Retrieve the token from localStorage
  const token = localStorage.getItem("token");
  const headers = {
    "Content-Type": "application/json",
  };

  // Add the Authorization header if the token exists
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  try {
    const response = await fetch(url, {
      method: "GET",
      headers: headers, // Use the headers object
    });

    if (!response.ok) {
      // Attempt to read error details from the backend response
      const errorBody = await response.text();
      console.error("Backend error:", errorBody);
      throw new Error(`HTTP error ${response.status}: ${response.statusText}`);
    }

    return await response.json();
  } catch (error) {
    console.error("Failed to fetch games:", error);
    // Re-throw the error so the calling component can handle it
    throw error;
  }
};

/**
 * Creates a new game.
 * @param {object} gameData - The game data.
 * @param {string} gameData.name - Game name.
 * @param {number} gameData.minPlayers - Min players.
 * @param {number} gameData.maxPlayers - Max players.
 * @param {string} [gameData.image] - Image URL (optional).
 * @param {string} [gameData.category] - Category (optional).
 * @returns {Promise<object>} A promise that resolves to the created game object.
 */
export const createGame = async (gameData) => {
  const token = localStorage.getItem("token");
  const ownerEmail = localStorage.getItem("userEmail"); // Get owner's email

  if (!token) {
    throw new Error("Authentication token not found. Please log in.");
  }
  if (!ownerEmail) {
    // This shouldn't happen if login/registration worked, but good to check
    throw new Error("User email not found in storage. Please log in again.");
  }

  const payload = {
    ...gameData,
    minPlayers: parseInt(gameData.minPlayers, 10), // Ensure numbers are integers
    maxPlayers: parseInt(gameData.maxPlayers, 10),
    ownerId: ownerEmail, // Set ownerId from stored email
  };

  const headers = {
    "Content-Type": "application/json",
    'Authorization': `Bearer ${token}`
  };

  try {
    const response = await fetch("http://localhost:8080/api/v1/games", {
      method: "POST",
      headers: headers,
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      // Try to parse error message from backend
      let errorMsg = `HTTP error ${response.status}: ${response.statusText}`;
      try {
          const errorBody = await response.json(); // Or response.text() if not JSON
          errorMsg = errorBody.message || errorMsg;
      } catch (e) { /* Ignore parsing error */ }
      console.error("Backend error creating game:", errorMsg);
      throw new Error(errorMsg);
    }
    return await response.json(); // Return the created game object from backend
  } catch (error) {
    console.error("Failed to create game:", error);
    throw error; // Re-throw for the component to handle
  }
};


// Add other game-related API functions here as needed
// e.g., getGameById, updateGame, deleteGame, getGameReviews, submitReview etc.
