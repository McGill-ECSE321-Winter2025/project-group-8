// Mock data based on the API structure
// Each game name can have multiple instances owned by different people
export const SAMPLE_GAMES = [
  {
    id: 1,
    name: "Catan",
    minPlayers: 3,
    maxPlayers: 4,
    image: "https://images.unsplash.com/photo-1585504198199-20277593b94f?q=80&w=300&auto=format&fit=crop",
    dateAdded: "2024-01-15T10:30:00",
    category: "Strategy",
    owner: {
      id: 101,
      name: "Game Library",
      email: "library@example.com"
    }
  },
  {
    id: 2,
    name: "Catan", // Same game, different owner
    minPlayers: 3,
    maxPlayers: 4,
    image: "https://images.unsplash.com/photo-1585504198199-20277593b94f?q=80&w=300&auto=format&fit=crop",
    dateAdded: "2024-02-05T14:20:00",
    category: "Strategy",
    owner: {
      id: 102,
      name: "Community Center",
      email: "community@example.com"
    }
  },
  {
    id: 3,
    name: "Ticket to Ride",
    minPlayers: 2,
    maxPlayers: 5,
    image: "https://images.unsplash.com/photo-1627986510562-1f06416f9298?q=80&w=300&auto=format&fit=crop",
    dateAdded: "2024-02-10T14:15:00",
    category: "Family",
    owner: {
      id: 102,
      name: "Community Center",
      email: "community@example.com"
    }
  },
  {
    id: 4,
    name: "Pandemic",
    minPlayers: 2,
    maxPlayers: 4,
    image: "https://images.unsplash.com/photo-1611032093218-5efe3b373c3e?q=80&w=300&auto=format&fit=crop",
    dateAdded: "2024-03-05T09:45:00",
    category: "Cooperative",
    owner: {
      id: 103,
      name: "Board Game Club",
      email: "bgclub@example.com"
    }
  },
  {
    id: 5,
    name: "Pandemic", // Same game, different owner
    minPlayers: 2,
    maxPlayers: 4,
    image: "https://images.unsplash.com/photo-1611032093218-5efe3b373c3e?q=80&w=300&auto=format&fit=crop",
    dateAdded: "2024-01-20T11:30:00",
    category: "Cooperative",
    owner: {
      id: 102,
      name: "Community Center",
      email: "community@example.com"
    }
  },
  {
    id: 6,
    name: "Scythe",
    minPlayers: 1,
    maxPlayers: 5,
    image: "https://images.unsplash.com/photo-1606503153255-59d8b2e757c8?q=80&w=300&auto=format&fit=crop",
    dateAdded: "2024-03-12T16:20:00",
    category: "Strategy",
    owner: {
      id: 104,
      name: "Strategy Games Club",
      email: "strategyclub@example.com"
    }
  },
  {
    id: 7,
    name: "Codenames",
    minPlayers: 2,
    maxPlayers: 8,
    image: "https://images.unsplash.com/photo-1606167668584-78701c57f13d?q=80&w=300&auto=format&fit=crop",
    dateAdded: "2024-02-28T11:10:00",
    category: "Party",
    owner: {
      id: 105,
      name: "Community Center",
      email: "community@example.com"
    }
  },
  {
    id: 8,
    name: "Wingspan",
    minPlayers: 1,
    maxPlayers: 5,
    image: "https://images.unsplash.com/photo-1610890716171-6b1bb98ffd09?q=80&w=300&auto=format&fit=crop",
    dateAdded: "2024-01-25T13:40:00",
    category: "Strategy",
    owner: {
      id: 106,
      name: "Nature Club",
      email: "nature@example.com"
    }
  }
];

// Helper function to group games by name
export function getUniqueGameNames() {
  const uniqueGames = [];
  const gameMap = {};
  
  SAMPLE_GAMES.forEach(game => {
    if (!gameMap[game.name]) {
      // Create a new entry with the first instance of this game
      const newEntry = {
        name: game.name,
        minPlayers: game.minPlayers,
        maxPlayers: game.maxPlayers,
        image: game.image,
        category: game.category,
        instances: [game]
      };
      uniqueGames.push(newEntry);
      gameMap[game.name] = newEntry;
    } else {
      // Add this instance to the existing entry
      gameMap[game.name].instances.push(game);
    }
  });
  
  return uniqueGames;
}

// Get instances of a specific game by name
export function getGameInstancesByName(name) {
  return SAMPLE_GAMES.filter(game => game.name === name);
}

// Sample reviews data - would be fetched from /games/{id}/reviews
export const SAMPLE_REVIEWS = {
  1: [
    {
      id: 1,
      rating: 4.5,
      comment: "Great game for family gatherings!",
      dateSubmitted: "2024-02-10T14:30:00",
      gameId: 1,
      gameTitle: "Catan",
      reviewer: {
        id: 201,
        name: "BoardGameFan",
        email: "fan@example.com"
      }
    },
    {
      id: 2,
      rating: 5,
      comment: "My all-time favorite strategy game.",
      dateSubmitted: "2024-03-05T09:15:00",
      gameId: 1,
      gameTitle: "Catan",
      reviewer: {
        id: 202,
        name: "StrategyLover",
        email: "strategy@example.com"
      }
    }
  ],
  2: [
    {
      id: 3,
      rating: 5,
      comment: "Perfect game for beginners and veterans alike!",
      dateSubmitted: "2024-03-15T16:45:00",
      gameId: 2,
      gameTitle: "Ticket to Ride",
      reviewer: {
        id: 203,
        name: "TrainEnthusiast",
        email: "trains@example.com"
      }
    }
  ],
  3: [
    {
      id: 4,
      rating: 4,
      comment: "Great team building experience!",
      dateSubmitted: "2024-02-25T10:20:00",
      gameId: 3,
      gameTitle: "Pandemic",
      reviewer: {
        id: 204,
        name: "CoopGamer",
        email: "coop@example.com"
      }
    }
  ],
  4: [
    {
      id: 5,
      rating: 5,
      comment: "Amazing artwork and deep gameplay!",
      dateSubmitted: "2024-03-20T13:10:00",
      gameId: 4,
      gameTitle: "Scythe",
      reviewer: {
        id: 205,
        name: "HistoryBuff",
        email: "history@example.com"
      }
    }
  ],
  5: [
    {
      id: 6,
      rating: 4.5,
      comment: "Perfect for game nights with friends!",
      dateSubmitted: "2024-02-18T19:30:00",
      gameId: 5,
      gameTitle: "Codenames",
      reviewer: {
        id: 206,
        name: "PartyGamer",
        email: "party@example.com"
      }
    },
    {
      id: 8,
      rating: 4,
      comment: "Pandemic is a must-play cooperative game!",
      dateSubmitted: "2024-02-08T16:30:00",
      gameId: 5,
      gameTitle: "Pandemic",
      reviewer: {
        id: 208,
        name: "CoopFan",
        email: "coopfan@example.com"
      }
    }
  ],
  6: [
    {
      id: 7,
      rating: 5,
      comment: "Beautiful artwork and surprisingly deep strategy!",
      dateSubmitted: "2024-03-01T15:25:00",
      gameId: 6,
      gameTitle: "Wingspan",
      reviewer: {
        id: 207,
        name: "BirdWatcher",
        email: "birds@example.com"
      }
    }
  ]
};

// Sample lending records - would be fetched through the lending record endpoints
export const SAMPLE_LENDING_RECORDS = {
  1: [
    {
      id: 301,
      startDate: "2024-04-01T09:00:00",
      endDate: "2024-04-10T18:00:00",
      status: "AVAILABLE",
      game: {
        id: 1,
        name: "Catan",
        category: "Strategy"
      },
      owner: {
        id: 101,
        name: "Game Library",
        email: "library@example.com"
      }
    }
  ],
  2: [
    {
      id: 302,
      startDate: "2024-04-05T10:00:00",
      endDate: "2024-04-12T19:00:00",
      status: "AVAILABLE",
      game: {
        id: 2,
        name: "Ticket to Ride",
        category: "Family"
      },
      owner: {
        id: 102,
        name: "Community Center",
        email: "community@example.com"
      }
    },
    {
      id: 304,
      startDate: "2024-05-01T09:00:00",
      endDate: "2024-05-10T18:00:00",
      status: "AVAILABLE",
      game: {
        id: 2,
        name: "Catan",
        category: "Strategy"
      },
      owner: {
        id: 102,
        name: "Community Center",
        email: "community@example.com"
      }
    }
  ],
  3: [
    {
      id: 303,
      startDate: "2024-04-03T14:00:00",
      endDate: "2024-04-08T17:00:00",
      status: "BORROWED",
      game: {
        id: 3,
        name: "Pandemic",
        category: "Cooperative"
      },
      owner: {
        id: 103,
        name: "Board Game Club",
        email: "bgclub@example.com"
      }
    }
  ],
  5: [
    {
      id: 305,
      startDate: "2024-04-10T10:00:00",
      endDate: "2024-04-20T18:00:00",
      status: "AVAILABLE",
      game: {
        id: 5,
        name: "Pandemic",
        category: "Cooperative"
      },
      owner: {
        id: 102,
        name: "Community Center",
        email: "community@example.com"
      }
    }
  ]
}; 