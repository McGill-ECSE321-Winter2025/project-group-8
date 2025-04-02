// Game data for the Game Search Page
// This would normally come from an API, but is mocked here for demo purposes

// List of all possible game names
const games = [
  {
    id: 'game-1',
    name: 'Monopoly',
    description: 'The classic property trading game where players buy, sell, and trade properties to win.',
    minPlayers: 2,
    maxPlayers: 8,
    playTime: '60-180 minutes',
    complexity: 'Easy',
    owner: 'user-1',
    category: 'Family',
    imageUrl: 'https://placehold.co/400x300/e9e9e9/1d1d1d?text=Monopoly'
  },
  {
    id: 'game-2',
    name: 'Settlers of Catan',
    description: 'Players collect resources to build settlements, cities, and roads to reach 10 victory points.',
    minPlayers: 3,
    maxPlayers: 4,
    playTime: '60-120 minutes',
    complexity: 'Medium',
    owner: 'user-2',
    category: 'Strategy',
    imageUrl: 'https://placehold.co/400x300/e9e9e9/1d1d1d?text=Catan'
  },
  {
    id: 'game-3',
    name: 'Ticket to Ride',
    description: 'Players collect train cards to claim railway routes connecting cities across North America.',
    minPlayers: 2,
    maxPlayers: 5,
    playTime: '30-60 minutes',
    complexity: 'Easy',
    owner: 'user-6',
    category: 'Family',
    imageUrl: 'https://placehold.co/400x300/e9e9e9/1d1d1d?text=Ticket+to+Ride'
  },
  {
    id: 'game-4',
    name: 'Chess',
    description: 'The classic two-player strategy game of tactical warfare.',
    minPlayers: 2,
    maxPlayers: 2,
    playTime: '30-180 minutes',
    complexity: 'Medium',
    owner: 'user-5',
    category: 'Abstract',
    imageUrl: 'https://placehold.co/400x300/e9e9e9/1d1d1d?text=Chess'
  },
  {
    id: 'game-5',
    name: 'Pandemic',
    description: 'Cooperative game where players work together to treat infections and find cures for diseases.',
    minPlayers: 2,
    maxPlayers: 4,
    playTime: '45-60 minutes',
    complexity: 'Medium',
    owner: 'user-10',
    category: 'Cooperative',
    imageUrl: 'https://placehold.co/400x300/e9e9e9/1d1d1d?text=Pandemic'
  },
  {
    id: 'game-6',
    name: 'Terraforming Mars',
    description: 'Players take on the role of corporations working to terraform the planet Mars.',
    minPlayers: 1,
    maxPlayers: 5,
    playTime: '120-180 minutes',
    complexity: 'High',
    owner: 'user-4',
    category: 'Strategy',
    imageUrl: 'https://placehold.co/400x300/e9e9e9/1d1d1d?text=Terraforming+Mars'
  },
  {
    id: 'game-7',
    name: '7 Wonders',
    description: 'Card drafting game spanning three ages of ancient civilization building.',
    minPlayers: 3,
    maxPlayers: 7,
    playTime: '30-45 minutes',
    complexity: 'Medium',
    owner: 'user-10',
    category: 'Card',
    imageUrl: 'https://placehold.co/400x300/e9e9e9/1d1d1d?text=7+Wonders'
  },
  {
    id: 'game-8',
    name: 'Gloomhaven',
    description: 'Campaign-based dungeon crawl game with persistent and evolving world.',
    minPlayers: 1,
    maxPlayers: 4,
    playTime: '90-150 minutes',
    complexity: 'High',
    owner: 'user-4',
    category: 'RPG',
    imageUrl: 'https://placehold.co/400x300/e9e9e9/1d1d1d?text=Gloomhaven'
  }
];

// Function to get unique games grouped by name
export const getUniqueGameNames = () => {
  // In a real application, this might group multiple instances of the same game
  // For this mock data, we'll just return the list of games
  return games;
};

export default {
  games,
  getUniqueGameNames
}; 