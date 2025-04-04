/**
 * Game Service
 * 
 * This file provides mock API functions for game-related operations.
 */

import { games, gameNames } from './mockData';
import {
  simulateApiCall,
  paginateItems,
  clone,
  generateId,
  searchInObject,
  errorMessages
} from './apiUtils';

// In-memory game storage (cloned to avoid mutating the original)
let gameStore = clone(games);

// Get all games with optional filtering and pagination
export const getGames = async (options = {}) => {
  const { search, category, owner, minPlayers, maxPlayers, complexity, page, pageSize } = options;
  
  return simulateApiCall(async () => {
    // Apply filters
    let filteredGames = [...gameStore];
    
    // Text search
    if (search) {
      filteredGames = filteredGames.filter(game => 
        searchInObject(game, search, ['name', 'description', 'category'])
      );
    }
    
    // Filter by category
    if (category) {
      filteredGames = filteredGames.filter(game => 
        game.category && game.category.toLowerCase() === category.toLowerCase()
      );
    }
    
    // Filter by owner
    if (owner) {
      filteredGames = filteredGames.filter(game => game.owner === owner);
    }
    
    // Filter by player count
    if (minPlayers !== undefined) {
      filteredGames = filteredGames.filter(game => game.minPlayers <= minPlayers);
    }
    
    if (maxPlayers !== undefined) {
      filteredGames = filteredGames.filter(game => game.maxPlayers >= maxPlayers);
    }
    
    // Filter by complexity
    if (complexity) {
      filteredGames = filteredGames.filter(game => 
        game.complexity && game.complexity.toLowerCase() === complexity.toLowerCase()
      );
    }
    
    // Apply pagination if requested
    if (page && pageSize) {
      return paginateItems(filteredGames, page, pageSize);
    }
    
    return filteredGames;
  });
};

// Get a game by ID
export const getGameById = async (gameId) => {
  return simulateApiCall(async () => {
    const game = gameStore.find(g => g.id === gameId);
    
    if (!game) {
      throw new Error(errorMessages.notFound);
    }
    
    return game;
  });
};

// Create a new game
export const createGame = async (gameData, userId) => {
  return simulateApiCall(async () => {
    // Validate required fields
    if (!gameData.name || !userId) {
      throw new Error('Game name and owner are required');
    }
    
    // Check if game with same name by same owner already exists
    const gameExists = gameStore.some(
      g => g.name === gameData.name && g.owner === userId
    );
    
    if (gameExists) {
      throw new Error('You already have a game with this name');
    }
    
    // Create new game with defaults for missing fields
    const newGame = {
      id: generateId('game'),
      name: gameData.name,
      description: gameData.description || `A game of ${gameData.name}`,
      minPlayers: gameData.minPlayers || 2,
      maxPlayers: gameData.maxPlayers || 4,
      playTime: gameData.playTime || '30-60 minutes',
      complexity: gameData.complexity || 'Medium',
      owner: userId,
      category: gameData.category || 'Other',
      imageUrl: gameData.imageUrl || `https://placehold.co/400x300/e9e9e9/1d1d1d?text=${encodeURIComponent(gameData.name)}`
    };
    
    // Add to store
    gameStore.push(newGame);
    
    return newGame;
  });
};

// Update a game
export const updateGame = async (gameId, updates, userId) => {
  return simulateApiCall(async () => {
    const gameIndex = gameStore.findIndex(g => g.id === gameId);
    
    if (gameIndex === -1) {
      throw new Error(errorMessages.notFound);
    }
    
    // Check ownership if userId is provided
    if (userId && gameStore[gameIndex].owner !== userId) {
      throw new Error(errorMessages.unauthorized);
    }
    
    // Update game (merging with existing data)
    gameStore[gameIndex] = {
      ...gameStore[gameIndex],
      ...updates,
      // Preserve ID and owner
      id: gameId,
      owner: gameStore[gameIndex].owner
    };
    
    return gameStore[gameIndex];
  });
};

// Delete a game
export const deleteGame = async (gameId, userId) => {
  return simulateApiCall(async () => {
    const game = gameStore.find(g => g.id === gameId);
    
    if (!game) {
      throw new Error(errorMessages.notFound);
    }
    
    // Check ownership if userId is provided
    if (userId && game.owner !== userId) {
      throw new Error(errorMessages.unauthorized);
    }
    
    // Remove from store
    gameStore = gameStore.filter(g => g.id !== gameId);
    
    return { id: gameId, deleted: true };
  });
};

// Search games by name or description
export const searchGames = async (query, options = {}) => {
  return simulateApiCall(async () => {
    if (!query && Object.keys(options).length === 0) {
      return [];
    }
    
    let matchedGames = [...gameStore];
    
    // Text search
    if (query) {
      matchedGames = matchedGames.filter(game => 
        searchInObject(game, query, ['name', 'description', 'category'])
      );
    }
    
    // Apply additional filters
    if (options.category) {
      matchedGames = matchedGames.filter(game => 
        game.category && game.category.toLowerCase() === options.category.toLowerCase()
      );
    }
    
    if (options.minPlayers) {
      matchedGames = matchedGames.filter(game => 
        game.minPlayers <= options.minPlayers
      );
    }
    
    if (options.maxPlayers) {
      matchedGames = matchedGames.filter(game => 
        game.maxPlayers >= options.maxPlayers
      );
    }
    
    return matchedGames;
  });
};

// Get games by owner
export const getGamesByOwner = async (ownerId) => {
  return simulateApiCall(async () => {
    return gameStore.filter(game => game.owner === ownerId);
  });
};

// Get game suggestions based on user preferences
export const getGameSuggestions = async (userId, preferences = {}, limit = 5) => {
  return simulateApiCall(async () => {
    // Simple recommendation algorithm based on category or complexity
    let recommendedGames = [...gameStore];
    
    if (preferences.category) {
      recommendedGames = recommendedGames.filter(game => 
        game.category === preferences.category
      );
    }
    
    if (preferences.complexity) {
      recommendedGames = recommendedGames.filter(game => 
        game.complexity === preferences.complexity
      );
    }
    
    // If we don't have enough games after filtering, add some random ones
    if (recommendedGames.length < limit) {
      const remainingGames = gameStore.filter(game => 
        !recommendedGames.some(rg => rg.id === game.id)
      );
      
      // Shuffle array to get random games
      const shuffled = remainingGames.sort(() => 0.5 - Math.random());
      
      // Add enough to reach the limit
      recommendedGames = [
        ...recommendedGames,
        ...shuffled.slice(0, limit - recommendedGames.length)
      ];
    }
    
    // Take the top 'limit' games
    return recommendedGames.slice(0, limit);
  });
};

// Get popular game categories
export const getGameCategories = async () => {
  return simulateApiCall(async () => {
    const categories = gameStore.map(game => game.category)
      .filter(category => category) // Remove nulls/undefined
      .reduce((unique, category) => {
        if (!unique.includes(category)) {
          unique.push(category);
        }
        return unique;
      }, []);
    
    return categories;
  });
};

// Reset the game store (for testing purposes)
export const resetGameStore = () => {
  gameStore = clone(games);
};

// Export functions
export default {
  getGames,
  getGameById,
  createGame,
  updateGame,
  deleteGame,
  searchGames,
  getGamesByOwner,
  getGameSuggestions,
  getGameCategories,
  resetGameStore
}; 