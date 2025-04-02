/**
 * User Service
 * 
 * This file provides mock API functions for user-related operations.
 */

import { users } from './mockData';
import {
  simulateApiCall,
  filterItems,
  paginateItems,
  clone,
  generateId,
  searchInObject,
  errorMessages
} from './apiUtils';

// In-memory user storage (cloned to avoid mutating the original)
let userStore = clone(users);

// Get all users with optional filtering and pagination
export const getUsers = async (options = {}) => {
  const { search, isGameOwner, page, pageSize } = options;
  
  return simulateApiCall(async () => {
    // Apply filters
    let filteredUsers = [...userStore];
    
    // Text search
    if (search) {
      filteredUsers = filteredUsers.filter(user => 
        searchInObject(user, search, ['username', 'email', 'bio', 'gamesPlayed'])
      );
    }
    
    // Game owner filter
    if (isGameOwner !== undefined) {
      filteredUsers = filteredUsers.filter(user => user.isGameOwner === isGameOwner);
    }
    
    // Apply pagination if requested
    if (page && pageSize) {
      return paginateItems(filteredUsers, page, pageSize);
    }
    
    return filteredUsers;
  });
};

// Get a user by ID
export const getUserById = async (userId) => {
  return simulateApiCall(async () => {
    const user = userStore.find(u => u.id === userId);
    
    if (!user) {
      throw new Error(errorMessages.notFound);
    }
    
    return user;
  });
};

// Create a new user
export const createUser = async (userData) => {
  return simulateApiCall(async () => {
    // Check if email already exists
    const emailExists = userStore.some(u => u.email === userData.email);
    if (emailExists) {
      throw new Error('Email already in use');
    }
    
    // Check if username already exists
    const usernameExists = userStore.some(u => u.username === userData.username);
    if (usernameExists) {
      throw new Error('Username already taken');
    }
    
    // Create new user with defaults for missing fields
    const newUser = {
      id: generateId('user'),
      username: userData.username,
      email: userData.email,
      password: userData.password,
      isGameOwner: userData.isGameOwner || false,
      bio: userData.bio || '',
      avatarUrl: userData.avatarUrl || null,
      gamesPlayed: userData.gamesPlayed || [],
      friends: userData.friends || [],
      joinDate: new Date().toISOString(),
      preferences: userData.preferences || {}
    };
    
    // Add to store
    userStore.push(newUser);
    
    // Return the created user (without password)
    const { password, ...userWithoutPassword } = newUser;
    return userWithoutPassword;
  });
};

// Update a user
export const updateUser = async (userId, updates) => {
  return simulateApiCall(async () => {
    const userIndex = userStore.findIndex(u => u.id === userId);
    
    if (userIndex === -1) {
      throw new Error(errorMessages.notFound);
    }
    
    // Check username uniqueness if being updated
    if (updates.username && updates.username !== userStore[userIndex].username) {
      const usernameExists = userStore.some(
        u => u.id !== userId && u.username === updates.username
      );
      
      if (usernameExists) {
        throw new Error('Username already taken');
      }
    }
    
    // Check email uniqueness if being updated
    if (updates.email && updates.email !== userStore[userIndex].email) {
      const emailExists = userStore.some(
        u => u.id !== userId && u.email === updates.email
      );
      
      if (emailExists) {
        throw new Error('Email already in use');
      }
    }
    
    // Update user (merging with existing data)
    userStore[userIndex] = {
      ...userStore[userIndex],
      ...updates,
      // Preserve ID
      id: userId
    };
    
    // Return the updated user (without password)
    const { password, ...userWithoutPassword } = userStore[userIndex];
    return userWithoutPassword;
  });
};

// Delete a user
export const deleteUser = async (userId) => {
  return simulateApiCall(async () => {
    const initialLength = userStore.length;
    userStore = userStore.filter(u => u.id !== userId);
    
    if (userStore.length === initialLength) {
      throw new Error(errorMessages.notFound);
    }
    
    return { id: userId, deleted: true };
  });
};

// Search users by username or other criteria
export const searchUsers = async (query, options = {}) => {
  return simulateApiCall(async () => {
    if (!query && Object.keys(options).length === 0) {
      return [];
    }
    
    let matchedUsers = [...userStore];
    
    // Text search
    if (query) {
      const searchFields = ['username', 'email', 'bio', 'gamesPlayed'];
      matchedUsers = matchedUsers.filter(user => 
        searchInObject(user, query, searchFields)
      );
    }
    
    // Apply additional filters
    if (options.isGameOwner !== undefined) {
      matchedUsers = matchedUsers.filter(user => 
        user.isGameOwner === options.isGameOwner
      );
    }
    
    if (options.hasGames === true) {
      matchedUsers = matchedUsers.filter(user => 
        user.gamesPlayed && user.gamesPlayed.length > 0
      );
    }
    
    // Return basic info for results (no passwords or other sensitive data)
    return matchedUsers.map(user => {
      const { password, ...safeUser } = user;
      return safeUser;
    });
  });
};

// Get recommended users based on game preferences
export const getRecommendedUsers = async (userId, limit = 5) => {
  return simulateApiCall(async () => {
    // Find the current user
    const currentUser = userStore.find(u => u.id === userId);
    
    if (!currentUser) {
      throw new Error(errorMessages.notFound);
    }
    
    // Get all users except current user
    const otherUsers = userStore.filter(u => u.id !== userId);
    
    // Calculate recommendations based on common games
    const recommendations = otherUsers.map(user => {
      // Find common games
      const userGames = user.gamesPlayed || [];
      const currentUserGames = currentUser.gamesPlayed || [];
      
      const commonGames = userGames.filter(game => 
        currentUserGames.includes(game)
      );
      
      // Compute similarity score based on number of common games
      const similarityScore = commonGames.length;
      
      // Return user with similarity data
      return {
        ...user,
        commonGames,
        similarityScore
      };
    });
    
    // Sort by similarity score and take the top 'limit' users
    const topRecommendations = recommendations
      .sort((a, b) => b.similarityScore - a.similarityScore)
      .filter(user => user.similarityScore > 0)
      .slice(0, limit);
    
    // Return without passwords
    return topRecommendations.map(user => {
      const { password, ...safeUser } = user;
      return safeUser;
    });
  });
};

// Add a friend connection
export const addFriend = async (userId, friendId) => {
  return simulateApiCall(async () => {
    const userIndex = userStore.findIndex(u => u.id === userId);
    const friendIndex = userStore.findIndex(u => u.id === friendId);
    
    if (userIndex === -1 || friendIndex === -1) {
      throw new Error(errorMessages.notFound);
    }
    
    // Add friend to user's friend list if not already there
    if (!userStore[userIndex].friends.includes(friendId)) {
      userStore[userIndex].friends.push(friendId);
    }
    
    // Add user to friend's friend list if not already there
    if (!userStore[friendIndex].friends.includes(userId)) {
      userStore[friendIndex].friends.push(userId);
    }
    
    return {
      success: true,
      userId,
      friendId
    };
  });
};

// Reset the user store (for testing purposes)
export const resetUserStore = () => {
  userStore = clone(users);
};

// Export functions
export default {
  getUsers,
  getUserById,
  createUser,
  updateUser,
  deleteUser,
  searchUsers,
  getRecommendedUsers,
  addFriend,
  resetUserStore
}; 