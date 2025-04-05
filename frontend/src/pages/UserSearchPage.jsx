import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import UserSearchBar from '../components/user-search-page/UserSearchBar.jsx';
import UserList from '../components/user-search-page/UserList.jsx';
// Commented out import from event-api.js as functions were removed/moved
// import { searchUsers, fetchUserRecommendations, getUserById } from '@/service/event-api.js';
import UserPreviewOverlay from '../components/ui/UserPreviewOverlay.jsx';

function UserSearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [searchQuery, setSearchQuery] = useState(searchParams.get('q') || '');
  const [searchResults, setSearchResults] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [isLoadingSearch, setIsLoadingSearch] = useState(false);
  const [isLoadingRecs, setIsLoadingRecs] = useState(false);
  const [searchError, setSearchError] = useState(null);
  const [recsError, setRecsError] = useState(null);
  const [filterGameOwnersOnly, setFilterGameOwnersOnly] = useState(
    searchParams.get('gameOwner') === 'true'
  );

  const [selectedUser, setSelectedUser] = useState(null);
  const [isPreviewOpen, setIsPreviewOpen] = useState(false);

  // Load initial search if query param exists
  useEffect(() => {
    if (searchParams.get('q')) {
      handleSearch(searchParams.get('q'));
    }
  }, []);

  // Effect for debounced search
  useEffect(() => {
    // Don't search if the query is empty
    if (searchQuery.trim() === '') {
      setSearchResults([]);
      setSearchError(null);
      return;
    }

    // Set loading state and clear previous errors
    setIsLoadingSearch(true);
    setSearchError(null);

    // Debounce for API call
    const searchDebounceHandler = setTimeout(() => {
      handleSearch(searchQuery);
    }, 500); // 500ms debounce for API call

    // Cleanup function
    return () => {
      clearTimeout(searchDebounceHandler);
    };
  }, [searchQuery]);

  // Update URL when filters change
  useEffect(() => {
    const params = new URLSearchParams();
    if (searchQuery) params.set('q', searchQuery);
    if (filterGameOwnersOnly) params.set('gameOwner', 'true');
    
    // Don't update URL if we're just viewing a user profile
    if (searchParams.has('previewUser')) {
      params.set('previewUser', searchParams.get('previewUser'));
    }
    
    setSearchParams(params);
  }, [searchQuery, filterGameOwnersOnly]);

  // Effect to load recommendations on component mount
  useEffect(() => {
    const loadRecommendations = async () => {
      // TODO: Re-implement recommendations with real API call from user-api.js
      setIsLoadingRecs(true);
      setRecsError(null);
      try {
        // Using 'user-1' as a default user ID for recommendations
        // const recs = await fetchUserRecommendations('user-1'); // Commented out usage
        console.warn("fetchUserRecommendations needs to be implemented with real API");
        setRecommendations([]); // Set empty for now
      } catch (error) {
        setRecsError(error.message || 'Failed to fetch recommendations');
        setRecommendations([]);
      } finally {
        setIsLoadingRecs(false);
      }
    };

    loadRecommendations();
  }, []);

  // Effect to handle user preview from URL
  useEffect(() => {
    const previewUserId = searchParams.get('previewUser');
    if (previewUserId) {
      loadUserPreview(previewUserId);
    } else {
      setIsPreviewOpen(false);
      setSelectedUser(null);
    }
  }, [searchParams.get('previewUser')]);

  // Function to load user preview
  const loadUserPreview = async (userId) => {
    // TODO: Re-implement user preview loading with real API call from user-api.js
    try {
      // const user = await getUserById(userId); // Commented out usage
      console.warn("getUserById needs to be implemented with real API");
      // For now, just close the preview if the API call is missing
      handlePreviewClose();
      // setSelectedUser(user);
      // setIsPreviewOpen(true);
    } catch (error) {
      console.error("Error loading user preview:", error);
      // User ID in URL not found, clear the param
      const params = new URLSearchParams(searchParams);
      params.delete('previewUser');
      setSearchParams(params);
      setIsPreviewOpen(false);
      setSelectedUser(null);
    }
  };

  // Function to handle search
  const handleSearch = async (query) => {
    // TODO: Re-implement search with real API call from user-api.js
    setIsLoadingSearch(true);
    try {
      // Pass filter options to the search API
      // const results = await searchUsers(query, { // Commented out usage
      //   isGameOwner: filterGameOwnersOnly || undefined
      // });
      console.warn("searchUsers needs to be implemented with real API");
      setSearchResults([]); // Set empty results for now
    } catch (error) {
      setSearchError(error.message || 'Search failed');
    } finally {
      setIsLoadingSearch(false);
    }
  };

  // Trigger search when filter changes
  useEffect(() => {
    if (searchQuery.trim() !== '') {
      handleSearch(searchQuery);
    }
  }, [filterGameOwnersOnly]);

  const handleUserCardClick = (user) => {
    setSelectedUser(user);
    setIsPreviewOpen(true);
    // Update URL with user ID for preview
    const params = new URLSearchParams(searchParams);
    params.set('previewUser', user.id);
    setSearchParams(params);
  };

  const handlePreviewClose = () => {
    setIsPreviewOpen(false);
    setSelectedUser(null);
    // Remove previewUser from URL
    const params = new URLSearchParams(searchParams);
    params.delete('previewUser');
    setSearchParams(params);
  };

  // Function to handle manual search submission
  const handleSearchSubmit = () => {
    if (searchQuery.trim() !== '') {
      handleSearch(searchQuery);
    }
  };

  return (
    <div className="container p-8 space-y-6">
      {/* Search Bar Area - Centered and Width Adjusted */}
      <div className="w-full md:w-3/4 lg:w-2/3 mx-auto">
        <UserSearchBar
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          onSearchSubmit={handleSearchSubmit}
          filterGameOwnersOnly={filterGameOwnersOnly}
          setFilterGameOwnersOnly={setFilterGameOwnersOnly}
        />
      </div>

      {/* Conditional Search Results Area */}
      {searchQuery.trim() !== '' && (
        <div className="mt-12">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-semibold tracking-tight">Search Results</h2>
            {!isLoadingSearch && searchResults.length > 0 && (
              <span className="text-sm text-muted-foreground">
                Found {searchResults.length} user{searchResults.length !== 1 ? 's' : ''}
              </span>
            )}
          </div>
          <UserList
            users={searchResults}
            isLoading={isLoadingSearch}
            error={searchError}
            emptyMessage="No users found matching your search."
            onUserClick={handleUserCardClick}
          />
        </div>
      )}

      {/* Conditional Recommendations Area */}
      {searchQuery.trim() === '' && (
        <div className="mt-12">
          <h2 className="text-2xl font-semibold tracking-tight mb-6">Recommended Friends</h2>
          <UserList
            users={recommendations}
            isLoading={isLoadingRecs}
            error={recsError}
            emptyMessage="No recommendations available. Try adding games to your profile!"
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
