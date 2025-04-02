import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom'; // Import useSearchParams
import UserSearchBar from '../components/user-search-page/UserSearchBar.jsx';
import UserList from '../components/user-search-page/UserList.jsx';
import { searchUsers, fetchUserRecommendations, dummyResults, dummyRecs } from '../service/api.js';
import UserPreviewOverlay from '../components/ui/UserPreviewOverlay.jsx';

function UserSearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [isLoadingSearch, setIsLoadingSearch] = useState(false);
  const [isLoadingRecs, setIsLoadingRecs] = useState(false);
  const [searchError, setSearchError] = useState(null);
  const [recsError, setRecsError] = useState(null);
  const [filterGameOwnersOnly, setFilterGameOwnersOnly] = useState(false);

  const [selectedUser, setSelectedUser] = useState(null); // Stores the user object for the overlay
  const [isPreviewOpen, setIsPreviewOpen] = useState(false); // Controls overlay visibility
  // Effect to load recommendations on component mount
  // Effect for debounced search
  useEffect(() => {
    // Don't search if the query is empty
    if (searchQuery.trim() === '') {
      setSearchError(null); // Clear any previous errors
      return;
    }

    // Clear previous errors immediately
    setSearchError(null);

    // Debounce for API call
    const searchDebounceHandler = setTimeout(() => {
      searchUsers(searchQuery)
        .then(results => {
          setSearchResults(results);
        })
        .catch(error => {
          setSearchError(error.message);
        })
    }, 500); // Keep 500ms debounce for API call

    // Updated Cleanup Function
    return () => {
      clearTimeout(searchDebounceHandler);
    };
  }, [searchQuery, filterGameOwnersOnly]); // Trigger effect when searchQuery or filter changes


  useEffect(() => {
    const loadRecommendations = async () => {
      setIsLoadingRecs(true);
      setRecsError(null);
      try {
        const recs = await fetchUserRecommendations();
        setRecommendations(recs);
      } catch (error) {
        setRecsError(error.message || 'Failed to fetch recommendations');
      } finally {
        setIsLoadingRecs(false);
      }
    };

    loadRecommendations();
  }, []); // Empty dependency array ensures this runs only once on mount

  // Effect to read previewUser from URL
  useEffect(() => {
    const previewUserId = searchParams.get('previewUser');
    if (previewUserId) {
      // Find the user in dummy data (combine recommendations and search results for lookup)
      // NOTE: In a real app, you might need to fetch user details if not already loaded
      const allUsers = [...dummyRecs, ...dummyResults];
      const userToPreview = allUsers.find(u => u.id === previewUserId);
      
      if (userToPreview) {
        setSelectedUser(userToPreview);
        setIsPreviewOpen(true);
      } else {
        // User ID in URL not found in current data, clear the param
        setSearchParams({}); 
        setIsPreviewOpen(false);
        setSelectedUser(null);
      }
    } else {
      // No previewUser param, ensure overlay is closed
      setIsPreviewOpen(false);
      setSelectedUser(null);
    }
  }, [searchParams]); // Re-run only if searchParams change



  const handleUserCardClick = (user) => {
    setSelectedUser(user);
    setIsPreviewOpen(true);
    setSearchParams({ previewUser: user.id }); // Update URL
  };

  const handlePreviewClose = () => {
    setIsPreviewOpen(false);
    setSelectedUser(null);
    setSearchParams({}); // Clear URL param
  };

  // Apply filtering based on the checkbox state
  const filteredSearchResults = filterGameOwnersOnly 
    ? searchResults.filter(user => user.isGameOwner) 
    : searchResults;

  return (
    <div className="container p-8 space-y-6">
      {/* Search Bar Area - Centered and Width Adjusted */}
      <div className="w-full md:w-3/4 lg:w-2/3 mx-auto">
        <UserSearchBar
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          filterGameOwnersOnly={filterGameOwnersOnly}
          setFilterGameOwnersOnly={setFilterGameOwnersOnly}
        />
      </div>

      {/* Conditional Search Results Area */}
      {searchQuery.trim() !== '' && (
        <div className="mt-12"> {/* Increased margin-top */}
          <h2 className="text-2xl font-semibold tracking-tight mb-6">Search Results</h2>
          <UserList
            users={filteredSearchResults} // Use the filtered list
            isLoading={isLoadingSearch}
            error={searchError}
            emptyMessage="No users found." // Updated empty message
            onUserClick={handleUserCardClick}
          />
        </div>
      )}

      {/* Conditional Recommendations Area */}
      {searchQuery.trim() === '' && (
        <div className="mt-12"> {/* Increased margin-top */}
          <h2 className="text-2xl font-semibold tracking-tight mb-6">Recommended Friends</h2>
          <UserList
            users={recommendations}
            isLoading={isLoadingRecs}
            error={recsError}
            emptyMessage="No recommendations available."
            onUserClick={handleUserCardClick}
          />
        </div>
      )}
      <UserPreviewOverlay
        user={selectedUser}
        isOpen={isPreviewOpen}
        onClose={handlePreviewClose}
      />
    </div>
  );
}

export default UserSearchPage;