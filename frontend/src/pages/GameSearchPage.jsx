import { useState, useEffect, useCallback } from "react"; // Added useCallback
import { Search, Filter, X, ArrowLeft, Loader2 } from "lucide-react"; // Added Loader2
import { Dialog, DialogTrigger } from "../components/ui/dialog";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { useSearchParams, useNavigate } from "react-router-dom";

// Import game components
import { GameCard } from "../components/game-search-page/GameCard";
import { GameDetailsDialog } from "../components/game-search-page/GameDetailsDialog";
import { RequestGameDialog } from "../components/game-search-page/RequestGameDialog";
// Removed mock data import
import { searchGames } from "../service/game-api"; // Added API service import

export default function GameSearchPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState(searchParams.get("q") || "");
  const fromUserId = searchParams.get("fromUser");
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [filters, setFilters] = useState({
    category: "",
    minPlayers: "",
    maxPlayers: "",
    minRating: ""
  });
  const [isRequestModalOpen, setIsRequestModalOpen] = useState(false);
  const [selectedGame, setSelectedGame] = useState(null);
  const [selectedInstance, setSelectedInstance] = useState(null);
  
  // Add missing state variables for API integration
  const [games, setGames] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Function to apply filters
  const applyFilters = () => {
    setIsFilterOpen(false);
    // Fetching is handled by useEffect when filters change
  };

  // Function to clear all filters
  const clearFilters = () => {
    setFilters({
      category: "",
      minPlayers: "",
      maxPlayers: "",
      minRating: ""
    });
    setIsFilterOpen(false);
  };
  
  // Check if any filters are applied
  const hasActiveFilters = () => {
    return Object.values(filters).some(value => value !== "");
  };
  
  const handleRequestGame = (game, instance) => {
    setSelectedGame(game);
    setSelectedInstance(instance);
    setIsRequestModalOpen(true);
  };
  
  const handleSubmitRequest = (requestData) => {
    // In a real app, this would submit the request to an API
    console.log("Game request submitted:", requestData);
    // Reset the selected instance
    setSelectedInstance(null);
  };

  // Effect to update search from URL parameters
  useEffect(() => {
    const queryParam = searchParams.get("q");
    if (queryParam) {
      setSearchTerm(queryParam);
    }
  }, [searchParams]);
  
  // Effect to update URL when search term changes
  useEffect(() => {
    // Update URL search param 'q' when searchTerm changes
    if (searchTerm) {
      searchParams.set("q", searchTerm);
      setSearchParams(searchParams);
    } else if (searchParams.has("q")) {
      searchParams.delete("q");
      setSearchParams(searchParams, { replace: true }); // Use replace to avoid history spam
    }
  }, [searchTerm, searchParams, setSearchParams]);

  // Effect to fetch games when search term or filters change
  useEffect(() => {
    const fetchGames = async () => {
      setIsLoading(true);
      setError(null);
      const criteria = {
        name: searchTerm || undefined, // Use searchTerm for name criteria
        category: filters.category || undefined,
        minPlayers: filters.minPlayers || undefined,
        maxPlayers: filters.maxPlayers || undefined,
      };
      // Remove empty/undefined criteria before sending
      Object.keys(criteria).forEach(key => (criteria[key] === undefined || criteria[key] === '') && delete criteria[key]);

      try {
        const fetchedGames = await searchGames(criteria);
        setGames(fetchedGames);
      } catch (err) {
        console.error("Error fetching games:", err); // Debug log
        setError(err.message || "Failed to fetch games. Please try again later.");
        setGames([]); // Clear games on error
      } finally {
        setIsLoading(false);
      }
    };

    // Debounce the fetch call slightly to avoid spamming API on rapid typing/filter changes
    const debounceTimer = setTimeout(() => {
      fetchGames();
    }, 300); // 300ms debounce

    return () => clearTimeout(debounceTimer); // Cleanup timer on unmount or dependency change
  }, [searchTerm, filters]); // Re-fetch when search term or filters change


  // Function to navigate back to the user search page while preserving the user preview state
  const handleBackToUsers = () => {
    if (fromUserId) {
      navigate(`/user-search?previewUser=${fromUserId}`);
    } else {
      navigate('/user-search');
    }
  };

  return (
    <div className="flex flex-col min-h-screen">
      {/* Main container with page-level scroll */}
      <div className="flex-1 flex flex-col p-4 sm:p-6 lg:p-8 overflow-y-auto">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold">Game Library</h1>
          {fromUserId && (
            <Button 
              variant="outline" 
              className="flex items-center gap-2"
              onClick={handleBackToUsers}
            >
              <ArrowLeft size={16} />
              Back to Users
            </Button>
          )}
        </div>
        
        {/* Search and filter bar - fixed height */}
        <div className="flex flex-col md:flex-row gap-4 mb-6">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Search for games..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          
          <div className="flex gap-2">
            <Button 
              variant={hasActiveFilters() ? "default" : "outline"} 
              className="flex items-center gap-2"
              onClick={() => setIsFilterOpen(!isFilterOpen)}
            >
              <Filter size={16} />
              Filter
              {hasActiveFilters() && <span className="bg-primary-foreground text-primary w-5 h-5 rounded-full text-xs flex items-center justify-center">{Object.values(filters).filter(v => v !== "").length}</span>}
            </Button>
          </div>
        </div>
        
        {/* Expanded filters - collapsible */}
        {isFilterOpen && (
          <div className="mb-6 p-4 border rounded-lg">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-medium">Filter Games</h3>
              <Button variant="ghost" size="sm" onClick={clearFilters}>
                Clear All
              </Button>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {/* Category filter */}
               <div>
                 <label className="block text-sm font-medium mb-1">Category</label>
                 <Input
                   placeholder="Enter category (e.g., Strategy)"
                   value={filters.category}
                   onChange={(e) => setFilters({...filters, category: e.target.value})}
                 />
               </div>

              {/* Player count filters */}
              <div>
                <label className="block text-sm font-medium mb-1">Min Players</label>
                <Input 
                  type="number" 
                  min="1"
                  placeholder="Any"
                  value={filters.minPlayers}
                  onChange={(e) => setFilters({...filters, minPlayers: e.target.value})}
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-1">Max Players</label>
                <Input 
                  type="number" 
                  min="1"
                  placeholder="Any"
                  value={filters.maxPlayers}
                  onChange={(e) => setFilters({...filters, maxPlayers: e.target.value})}
                />
              </div>
            </div>
            
            <div className="flex justify-end mt-4">
              <Button variant="default" onClick={applyFilters}>
                Apply Filters
              </Button>
            </div>
          </div>
        )}
        
        {/* Loading and error states */}
        {isLoading && (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="h-8 w-8 animate-spin text-primary" />
            <span className="ml-2 text-lg">Loading games...</span>
          </div>
        )}
        
        {error && !isLoading && (
          <div className="bg-destructive/10 p-4 rounded-lg text-destructive mb-6">
            <p className="font-medium">{error}</p>
            <p>Please try again or adjust your search criteria.</p>
          </div>
        )}
        
        {/* Game results grid */}
        {!isLoading && !error && (
          <>
            {games.length === 0 ? (
              <div className="text-center py-12">
                <p className="text-lg text-muted-foreground">No games found matching your criteria.</p>
                <p className="text-sm text-muted-foreground">Try adjusting your search or filters.</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 mt-2">
                {games.map((game) => (
                  <GameCard 
                    key={game.id} 
                    game={game} 
                    onRequest={handleRequestGame} 
                  />
                ))}
              </div>
            )}
          </>
        )}
      </div>

      {/* Game request dialog */}
      {isRequestModalOpen && (
        <RequestGameDialog
          open={isRequestModalOpen}
          onClose={() => setIsRequestModalOpen(false)}
          game={selectedGame}
          gameInstance={selectedInstance}
          onSubmit={handleSubmitRequest}
        />
      )}
    </div>
  );
}
