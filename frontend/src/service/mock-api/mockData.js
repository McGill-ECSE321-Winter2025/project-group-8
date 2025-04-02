/**
 * Mock Data Store
 * This file contains all of the mock data for our application.
 * It is used by the mock API services to simulate a backend.
 */

// Generate a random date in the past year
const getRandomPastDate = () => {
  const today = new Date();
  const pastYear = new Date(today.getFullYear() - 1, today.getMonth(), today.getDate());
  return new Date(pastYear.getTime() + Math.random() * (today.getTime() - pastYear.getTime()));
};

// Format date to ISO string
const formatDate = (date) => date.toISOString();

// Generate a random future date for events
const getRandomFutureDate = () => {
  const today = new Date();
  const futureYear = new Date(today.getFullYear() + 1, today.getMonth(), today.getDate());
  return new Date(today.getTime() + Math.random() * (futureYear.getTime() - today.getTime()));
};

// Generate random avatar URL
const getRandomAvatar = (username) => {
  const seed = username.toLowerCase().replace(/[^a-z0-9]/g, '');
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${seed}`;
};

// List of possible game names
const gameNames = [
  'Monopoly', 'Settlers of Catan', 'Ticket to Ride', 'Scrabble', 'Risk',
  'Pandemic', 'Dominion', '7 Wonders', 'Codenames', 'Chess', 'Carcassonne',
  'Azul', 'Splendor', 'Terraforming Mars', 'Gloomhaven'
];

// List of possible event locations
const eventLocations = [
  'Community Center', 'Local Library', 'Board Game Cafe', 'University Campus',
  'Recreation Center', 'Downtown Pub', 'Gaming Convention', 'Private Residence'
];

/**
 * USERS
 */
const users = [
  {
    id: 'user-1',
    username: 'GameMaster',
    email: 'gamemaster@example.com',
    password: 'password123', // In a real app, this would be hashed
    isGameOwner: true,
    bio: 'Board game enthusiast with a growing collection of strategy games.',
    avatarUrl: getRandomAvatar('GameMaster'),
    gamesPlayed: ['Monopoly', 'Settlers of Catan', 'Risk', 'Chess'],
    friends: ['user-3', 'user-5', 'user-7'],
    joinDate: formatDate(getRandomPastDate()),
    preferences: {
      favoriteGameType: 'Strategy',
      preferredPlayTime: '1-3 hours',
      playerCount: '3-5'
    }
  },
  {
    id: 'user-2',
    username: 'StrategyQueen',
    email: 'strategyqueen@example.com',
    password: 'secure456',
    isGameOwner: true,
    bio: 'Love complex strategy games and teaching them to others!',
    avatarUrl: getRandomAvatar('StrategyQueen'),
    gamesPlayed: ['Settlers of Catan', 'Terraforming Mars', '7 Wonders', 'Dominion'],
    friends: ['user-1', 'user-4', 'user-6'],
    joinDate: formatDate(getRandomPastDate()),
    preferences: {
      favoriteGameType: 'Heavy Strategy',
      preferredPlayTime: '2-4 hours',
      playerCount: '2-4'
    }
  },
  {
    id: 'user-3',
    username: 'CasualGamer',
    email: 'casualgamer@example.com',
    password: 'simplepass',
    isGameOwner: false,
    bio: 'Just looking for fun games to play on weekends!',
    avatarUrl: getRandomAvatar('CasualGamer'),
    gamesPlayed: ['Monopoly', 'Scrabble', 'Codenames', 'Ticket to Ride'],
    friends: ['user-1', 'user-8'],
    joinDate: formatDate(getRandomPastDate()),
    preferences: {
      favoriteGameType: 'Party Games',
      preferredPlayTime: '30-60 minutes',
      playerCount: '4+'
    }
  },
  {
    id: 'user-4',
    username: 'RPGEnthusiast',
    email: 'rpgfan@example.com',
    password: 'dragon123',
    isGameOwner: true,
    bio: 'Dungeon master on weekends, board gamer all week!',
    avatarUrl: getRandomAvatar('RPGEnthusiast'),
    gamesPlayed: ['Gloomhaven', 'Pandemic', 'Risk'],
    friends: ['user-2', 'user-9'],
    joinDate: formatDate(getRandomPastDate()),
    preferences: {
      favoriteGameType: 'RPG',
      preferredPlayTime: '3+ hours',
      playerCount: '3-5'
    }
  },
  {
    id: 'user-5',
    username: 'ChessWizard',
    email: 'chesswizard@example.com',
    password: 'checkmate',
    isGameOwner: false,
    bio: 'Chess player since childhood, exploring more board games!',
    avatarUrl: getRandomAvatar('ChessWizard'),
    gamesPlayed: ['Chess', 'Scrabble', 'Azul'],
    friends: ['user-1', 'user-10'],
    joinDate: formatDate(getRandomPastDate()),
    preferences: {
      favoriteGameType: 'Abstract',
      preferredPlayTime: '30-60 minutes',
      playerCount: '2'
    }
  },
  {
    id: 'user-6',
    username: 'FamilyGameNight',
    email: 'familygames@example.com',
    password: 'familyfun',
    isGameOwner: true,
    bio: 'Looking for family-friendly games for our weekly game night!',
    avatarUrl: getRandomAvatar('FamilyGameNight'),
    gamesPlayed: ['Ticket to Ride', 'Codenames', 'Monopoly', 'Settlers of Catan'],
    friends: ['user-2', 'user-7'],
    joinDate: formatDate(getRandomPastDate()),
    preferences: {
      favoriteGameType: 'Family',
      preferredPlayTime: '30-90 minutes',
      playerCount: '3-6'
    }
  },
  {
    id: 'user-7',
    username: 'CardGamePro',
    email: 'cardgamer@example.com',
    password: 'fullhouse',
    isGameOwner: false,
    bio: 'Card game expert looking to expand into more complex board games.',
    avatarUrl: getRandomAvatar('CardGamePro'),
    gamesPlayed: ['Dominion', '7 Wonders', 'Splendor'],
    friends: ['user-1', 'user-6'],
    joinDate: formatDate(getRandomPastDate()),
    preferences: {
      favoriteGameType: 'Card',
      preferredPlayTime: '45-90 minutes',
      playerCount: '2-4'
    }
  },
  {
    id: 'user-8',
    username: 'NewToGaming',
    email: 'newgamer@example.com',
    password: 'newbie789',
    isGameOwner: false,
    bio: 'Recently discovered the world of board games and loving it!',
    avatarUrl: getRandomAvatar('NewToGaming'),
    gamesPlayed: ['Ticket to Ride', 'Azul', 'Codenames'],
    friends: ['user-3'],
    joinDate: formatDate(getRandomPastDate()),
    preferences: {
      favoriteGameType: 'Gateway Games',
      preferredPlayTime: '30-60 minutes',
      playerCount: '2-5'
    }
  },
  {
    id: 'user-9',
    username: 'CompetitivePlayer',
    email: 'competitive@example.com',
    password: 'wintowin',
    isGameOwner: true,
    bio: 'Looking for challenging games and worthy opponents!',
    avatarUrl: getRandomAvatar('CompetitivePlayer'),
    gamesPlayed: ['Chess', 'Terraforming Mars', 'Scrabble', 'Risk'],
    friends: ['user-4', 'user-10'],
    joinDate: formatDate(getRandomPastDate()),
    preferences: {
      favoriteGameType: 'Competitive',
      preferredPlayTime: '1-3 hours',
      playerCount: '2-4'
    }
  },
  {
    id: 'user-10',
    username: 'CooperativeTeam',
    email: 'cooperative@example.com',
    password: 'teamwork',
    isGameOwner: true,
    bio: 'Prefer cooperative games where we can win or lose together!',
    avatarUrl: getRandomAvatar('CooperativeTeam'),
    gamesPlayed: ['Pandemic', 'Gloomhaven', '7 Wonders'],
    friends: ['user-5', 'user-9'],
    joinDate: formatDate(getRandomPastDate()),
    preferences: {
      favoriteGameType: 'Cooperative',
      preferredPlayTime: '1-2 hours',
      playerCount: '3-5'
    }
  }
];

/**
 * GAMES
 */
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

/**
 * EVENTS
 */
const events = [
  {
    id: 'event-1',
    title: 'Weekend Strategy Game Night',
    description: 'Join us for an evening of strategy games! Beginners welcome.',
    date: formatDate(getRandomFutureDate()),
    location: 'Board Game Cafe',
    host: 'user-2',
    games: ['game-2', 'game-6'],
    attendees: ['user-1', 'user-3', 'user-4', 'user-7'],
    maxAttendees: 8,
    status: 'scheduled'
  },
  {
    id: 'event-2',
    title: 'Family Game Day',
    description: 'Bring the whole family for a day of fun board games suitable for all ages!',
    date: formatDate(getRandomFutureDate()),
    location: 'Community Center',
    host: 'user-6',
    games: ['game-1', 'game-3'],
    attendees: ['user-3', 'user-8'],
    maxAttendees: 12,
    status: 'scheduled'
  },
  {
    id: 'event-3',
    title: 'Chess Tournament',
    description: 'Test your skills in our monthly chess tournament. All levels welcome!',
    date: formatDate(getRandomFutureDate()),
    location: 'Local Library',
    host: 'user-5',
    games: ['game-4'],
    attendees: ['user-1', 'user-9'],
    maxAttendees: 16,
    status: 'scheduled'
  },
  {
    id: 'event-4',
    title: 'Cooperative Game Challenge',
    description: 'Can we beat the game together? Join our cooperative gaming session!',
    date: formatDate(getRandomFutureDate()),
    location: 'Gaming Convention',
    host: 'user-10',
    games: ['game-5', 'game-8'],
    attendees: ['user-4', 'user-6', 'user-7'],
    maxAttendees: 6,
    status: 'scheduled'
  },
  {
    id: 'event-5',
    title: 'Card Game Marathon',
    description: 'A full day dedicated to various card-based board games.',
    date: formatDate(getRandomFutureDate()),
    location: 'Downtown Pub',
    host: 'user-7',
    games: ['game-7'],
    attendees: ['user-2', 'user-8', 'user-10'],
    maxAttendees: 10,
    status: 'scheduled'
  }
];

// Export all mock data
export {
  users,
  games,
  events,
  gameNames,
  eventLocations,
  getRandomPastDate,
  getRandomFutureDate,
  getRandomAvatar,
  formatDate
}; 