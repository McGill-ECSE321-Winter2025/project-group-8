/**
 * Mock API Index
 * 
 * This file exports all mock API services for easy imports.
 */

import userService from './userService';
import gameService from './gameService';
import eventService from './eventService';
import * as mockData from './mockData';
import * as apiUtils from './apiUtils';

// Main mock API object with all services
const mockApi = {
  users: userService,
  games: gameService,
  events: eventService,
  // Expose utility functions for advanced usage
  utils: apiUtils,
  // Expose raw data for direct access
  data: mockData,
  
  // Helper method to reset all stores to initial state
  resetAll: () => {
    userService.resetUserStore();
    gameService.resetGameStore();
    eventService.resetEventStore();
  }
};

export {
  userService,
  gameService,
  eventService,
  mockData,
  apiUtils
};

export default mockApi; 