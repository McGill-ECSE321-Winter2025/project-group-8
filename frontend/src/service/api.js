const dummyResults = [
  { id: 'search1', username: 'Searched User One', email: 'search1@example.com', isGameOwner: true, attendedEventIds: [1, 3], gamesPlayed: ['Chess', 'Monopoly'] },
  { id: 'search2', username: 'Searched User Two', email: 'search2@example.com', isGameOwner: false, attendedEventIds: [3, 5], gamesPlayed: ['Monopoly', 'Catan'] },
  { id: 'search3', username: 'Another Searched User', email: 'search3@example.com', isGameOwner: true, attendedEventIds: [1, 5], gamesPlayed: ['Chess', 'Risk'] },
  { id: 'search4', username: 'TestUser', email: 'testuser@example.com', isGameOwner: false, attendedEventIds: [2, 4], gamesPlayed: ['Catan', 'Risk'] },
  { id: 'search5', username: 'tester', email: 'tester@example.com', isGameOwner: true, attendedEventIds: [1, 2], gamesPlayed: ['Monopoly', 'Scrabble'] },
  { id: 'search6', username: 'Another Test', email: 'anothertest@example.com', isGameOwner: false, attendedEventIds: [4, 5], gamesPlayed: ['Risk', 'Scrabble'] },
  { id: 'search7', username: 'SearchMaster', email: 'searchmaster@example.com', isGameOwner: true, attendedEventIds: [1, 4], gamesPlayed: ['Chess', 'Catan'] },
  { id: 'search8', username: 'search_pro', email: 'searchpro@example.com', isGameOwner: false, attendedEventIds: [2, 3], gamesPlayed: ['Monopoly', 'Risk'] },
  { id: 'search9', username: 'UserSearcher', email: 'usersearcher@example.com', isGameOwner: true, attendedEventIds: [3, 4], gamesPlayed: ['Catan', 'Scrabble'] },
];

const dummyRecs = [
  { id: 'rec1', username: 'Recommended Friend A', email: 'recA@example.com', isGameOwner: false, attendedEventIds: [10, 12], gamesPlayed: ['Settlers of Catan', 'Ticket to Ride'] },
  { id: 'rec2', username: 'Recommended Friend B', email: 'recB@example.com', isGameOwner: true, attendedEventIds: [12, 14], gamesPlayed: ['Ticket to Ride', 'Dominion'] },
  { id: 'rec3', username: 'Recommended Friend C', email: 'recC@example.com', isGameOwner: false, attendedEventIds: [10, 14], gamesPlayed: ['Settlers of Catan', '7 Wonders'] },
  { id: 'rec4', username: 'Friendly Neighbor', email: 'neighbor@example.com', isGameOwner: true, attendedEventIds: [11, 13], gamesPlayed: ['Dominion', '7 Wonders'] },
  { id: 'rec5', username: 'Gamer Pal', email: 'gamerpal@example.com', isGameOwner: false, attendedEventIds: [10, 11], gamesPlayed: ['Ticket to Ride', 'Pandemic'] },
  { id: 'rec6', username: 'Community Member', email: 'community@example.com', isGameOwner: true, attendedEventIds: [13, 14], gamesPlayed: ['7 Wonders', 'Pandemic'] },
  { id: 'rec7', username: 'Helpful User', email: 'helpful@example.com', isGameOwner: false, attendedEventIds: [10, 13], gamesPlayed: ['Settlers of Catan', 'Dominion'] },
  { id: 'rec8', username: 'New Connection', email: 'connection@example.com', isGameOwner: true, attendedEventIds: [11, 12], gamesPlayed: ['Ticket to Ride', '7 Wonders'] },
  { id: 'rec9', username: 'Potential Teammate', email: 'teammate@example.com', isGameOwner: false, attendedEventIds: [12, 13], gamesPlayed: ['Dominion', 'Pandemic'] },
];

const searchUsers = async (query) => {
  // Removed fetch call and replaced with dummy data

  console.log(`Searching users with query: "${query}"`); // Added for debugging

  // Exact Matches
  const exactMatches = dummyResults.filter(user => user.username.includes(query));

  // Case-Insensitive Matches
  const lowerCaseQuery = query.toLowerCase();
  const lowerCaseMatches = dummyResults.filter(user =>
    user.username.toLowerCase().includes(lowerCaseQuery)
  );

  // Combine Results
  const combinedResults = [...exactMatches];
  const addedIds = new Set(exactMatches.map(user => user.id));

  lowerCaseMatches.forEach(user => {
    if (!addedIds.has(user.id)) {
      combinedResults.push(user);
      addedIds.add(user.id);
    }
  });

  // Return combined results
  // Add artificial delay

  return Promise.resolve(combinedResults);
};

const fetchUserRecommendations = async () => {
  // Removed fetch call and replaced with dummy data

  console.log("Fetching dummy user recommendations based on common events"); // Updated log

  const currentUserEventIds = new Set([1, 5, 7]); // Example IDs

  const recommendedUsers = dummyRecs
    .map(user => {
      // Ensure attendedEventIds exists and is an array before filtering
      const attendedEvents = Array.isArray(user.attendedEventIds) ? user.attendedEventIds : [];
      const commonEvents = attendedEvents.filter(id => currentUserEventIds.has(id));
      return { ...user, commonEventCount: commonEvents.length };
    })
    .filter(user => user.commonEventCount > 0) // Only recommend if there's at least one common event
    .sort((a, b) => b.commonEventCount - a.commonEventCount) // Sort descending by common count
    .slice(0, 5); // Limit to top 5 recommendations

  console.log("Recommended Users:", recommendedUsers); // Debugging output

  return Promise.resolve(recommendedUsers);
};

// Export the functions and the dummy data arrays
export { searchUsers, fetchUserRecommendations, dummyResults, dummyRecs };