import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom'; // Import useNavigate
import UserSearchBar from '../components/user-search-page/UserSearchBar.jsx';
import UserList from '../components/user-search-page/UserList.jsx';
// Import the correct function from user-api.js
import { getUserInfoByEmail } from '@/service/user-api.js';
// Keep UserPreviewOverlay import commented out for now
// import UserPreviewOverlay from '../components/ui/UserPreviewOverlay.jsx';
import { Loader2 } from 'lucide-react'; // Import Loader

function UserSearchPage() {
  const navigate = useNavigate(); // Initialize navigate
  const [searchParams, setSearchParams] = useSearchParams();
  const [searchQuery, setSearchQuery] = useState(searchParams.get('q') || '');
  const [searchResults, setSearchResults] = useState([]);
  const [recommendations, setRecommendations] = useState([]); // Keep for potential future use
  const [isLoadingSearch, setIsLoadingSearch] = useState(false);
  const [isLoadingRecs, setIsLoadingRecs] = useState(false); // Keep for potential future use
  const [searchError, setSearchError] = useState(null);
  const [recsError, setRecsError] = useState(null); // Keep for potential future use
  const [filterGameOwnersOnly, setFilterGameOwnersOnly] = useState(
    searchParams.get('gameOwner') === 'true'
  );

  // Keep state for preview commented out for now
  // const [selectedUser, setSelectedUser] = useState(null);
  // const [isPreviewOpen, setIsPreviewOpen] = useState(false);

  // Function to handle search by exact email
  const handleSearch = async (query) => {
    if (!query || query.trim() === '') {
        setSearchResults([]);
        setSearchError(null);
        setIsLoadingSearch(false);
        return;
    }
    setIsLoadingSearch(true);
    setSearchError(null);
    setSearchResults([]); // Clear previous results before new search
    try {
      // Call getUserInfoByEmail assuming query is the exact email
      const result = await getUserInfoByEmail(query);
      // Adapt the result for UserList/UserProfileCard
      const userResult = {
          // The backend GET /account/{email} returns AccountResponse DTO
          // which has name, events list, and gameOwner boolean.
          // We need an ID for the key prop and email for navigation.
          id: result.email, // Use email as unique key for now
          username: result.username, // Use 'username' directly from AccountResponse DTO
          email: result.email, // Use the email from the response
          isGameOwner: result.gameOwner,
          avatarUrl: "/placeholder.svg?height=48&width=48" // Placeholder avatar
      };
      setSearchResults([userResult]); // Put the single result in an array
      setSearchError(null); // Clear any previous error on success
    } catch (error) {
       setSearchResults([]); // Always clear results on error
       // Handle "user not found" specifically (e.g., 400/404 from backend) vs other errors
       const errorMsg = error.message || '';
       if (errorMsg.includes("400") || errorMsg.includes("404") || errorMsg.toLowerCase().includes("not exist")) {
            setSearchError(null); // Don't set an error message for "not found"
       } else {
           // Set a generic error for other failures
           setSearchError('Search failed. Please try again.');
           console.error("User search failed:", error); // Log the actual error for debugging
       }
    } finally {
      setIsLoadingSearch(false);
    }
  };


  // Load initial search if query param exists
  useEffect(() => {
    const initialQuery = searchParams.get('q');
    if (initialQuery) {
      setSearchQuery(initialQuery); // Set state to trigger search effect
    }
    // Also handle initial filter state
    setFilterGameOwnersOnly(searchParams.get('gameOwner') === 'true');
  }, []); // Run only once on mount


  // Effect for debounced search - trigger on searchQuery change
  useEffect(() => {
    // Debounce to avoid API call on every keystroke
    const debounceTimer = setTimeout(() => {
      // Trigger search only if query is not empty
       if (searchQuery.trim() !== '') {
           handleSearch(searchQuery);
       } else {
           // Clear results if search query is cleared
           setSearchResults([]);
           setSearchError(null);
       }
    }, 500); // 500ms debounce

    // Cleanup function
    return () => clearTimeout(debounceTimer);
  }, [searchQuery]); // Depend only on searchQuery


  // Update URL when search query changes (filter handled separately if needed)
  useEffect(() => {
    const params = new URLSearchParams(searchParams); // Preserve existing params like previewUser
    if (searchQuery) {
        params.set('q', searchQuery);
    } else {
        params.delete('q');
    }
    // Update URL without causing re-render loop if possible
    // Using replace: true might be better if search updates frequently
    setSearchParams(params, { replace: true });
  }, [searchQuery]);


  // TODO: Implement recommendations fetch if backend endpoint exists
  // useEffect(() => {
  //   const loadRecommendations = async () => { ... };
  //   loadRecommendations();
  // }, []);

  // TODO: Implement user preview fetch if backend endpoint exists
  // useEffect(() => {
  //   const previewUserId = searchParams.get('previewUser');
  //   if (previewUserId) { ... }
  // }, [searchParams.get('previewUser')]);


  const handleUserCardClick = (user) => {
    // Navigate to profile page, passing email as query param
    if (user && user.email) {
        navigate(`/profile?email=${encodeURIComponent(user.email)}`);
    } else {
        console.error("Cannot navigate to profile: user email missing.", user);
        toast.error("Could not open user profile."); // Use toast for user feedback
    }
  };

  // Keep handlePreviewClose commented out
  // const handlePreviewClose = () => { ... };

  // Function to handle manual search submission (e.g., pressing Enter)
  const handleSearchSubmit = () => {
    // The useEffect hook already handles debounced search based on searchQuery state
    // This function might not be strictly necessary unless you want immediate search on Enter
    if (searchQuery.trim() !== '') {
      handleSearch(searchQuery); // Trigger immediate search
    }
  };

  return (
    <div className="container p-8 space-y-6">
      {/* Search Bar Area */}
      <div className="w-full md:w-3/4 lg:w-2/3 mx-auto">
        <UserSearchBar
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery} // Pass setter to update state
          onSearchSubmit={handleSearchSubmit}
          // Filter functionality is disabled for now as backend doesn't support it with email search
          filterGameOwnersOnly={false} // Keep filter state, but maybe disable UI?
          setFilterGameOwnersOnly={() => {}} // Disable filter changes for now
        />
         <p className="text-sm text-muted-foreground mt-2 text-center">Search by exact user email.</p>
      </div>

      {/* Conditional Search Results Area */}
      {/* Show results only when a search has been attempted (query is not empty) */}
      {searchQuery.trim() !== '' && (
        <div className="mt-12">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-2xl font-semibold tracking-tight">Search Results</h2>
            {!isLoadingSearch && searchResults.length > 0 && (
              <span className="text-sm text-muted-foreground">
                 Found {searchResults.length} user{searchResults.length !== 1 ? 's' : ''}
               </span>
             )}
             {/* The explicit "No user found" span that was here is now removed */}
          </div>
          {/* Pass error state to UserList, and let it handle the empty message */}
          <UserList
            users={searchResults}
            isLoading={isLoadingSearch}
            error={searchError} // Pass the generic error state
            emptyMessage="No user found matching your search." // Use the message from UserList
            onUserClick={handleUserCardClick}
          />
        </div>
      )}

      {/* Conditional Recommendations Area - Keep disabled */}
      {/* {searchQuery.trim() === '' && ( ... )} */}

      {/* Keep preview overlay commented out */}
      {/* <UserPreviewOverlay ... /> */}
    </div>
  );
}

export default UserSearchPage;
