import { useState, useEffect } from "react";
import { Search, Filter, X, ArrowLeft } from "lucide-react";
import { Dialog, DialogTrigger } from "../components/ui/dialog";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { useSearchParams, useNavigate } from "react-router-dom";

// Import game components
import { GameCard } from "../components/game-search-page/GameCard";
import { GameDetailsDialog } from "../components/game-search-page/GameDetailsDialog";
import { RequestGameDialog } from "../components/game-search-page/RequestGameDialog";
import { getUniqueGameNames } from "../components/game-search-page/data";

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
  
  // Get unique games grouped by name
  const uniqueGames = getUniqueGameNames();
  
  // Extract all possible categories for filters
  const categories = [...new Set(uniqueGames.map(game => game.category))];
  
  // Filter games based on all criteria
  const filteredGames = uniqueGames.filter(game => {
    // Name filter (basic search)
    const matchesSearch = searchTerm === "" || 
      game.name.toLowerCase().includes(searchTerm.toLowerCase());
    
    // Category filter
    const matchesCategory = filters.category === "" || 
      game.category === filters.category;
    
    // Player count filter
    const matchesMinPlayers = filters.minPlayers === "" || 
      game.minPlayers >= parseInt(filters.minPlayers);
    const matchesMaxPlayers = filters.maxPlayers === "" || 
      game.maxPlayers <= parseInt(filters.maxPlayers);
    
    return matchesSearch && matchesCategory && matchesMinPlayers && 
           matchesMaxPlayers;
  });
  
  // Function to apply filters
  const applyFilters = () => {
    setIsFilterOpen(false);
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
  
  const handleRequestGame = (game) => {
    setSelectedGame(game);
    setIsRequestModalOpen(true);
  };
  
  const handleSubmitRequest = (requestData) => {
    // In a real app, this would submit the request to an API
    console.log("Game request submitted:", requestData);
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
    if (searchTerm) {
      searchParams.set("q", searchTerm);
      setSearchParams(searchParams);
    } else if (searchParams.has("q")) {
      searchParams.delete("q");
      setSearchParams(searchParams);
    }
  }, [searchTerm]);

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
                <select 
                  value={filters.category}
                  onChange={(e) => setFilters({...filters, category: e.target.value})}
                  className="border border-input rounded-md px-3 py-2 bg-background w-full"
                >
                  <option value="">Any Category</option>
                  {categories.map(category => (
                    <option key={category} value={category}>{category}</option>
                  ))}
                </select>
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
                  min={filters.minPlayers || "1"}
                  placeholder="Any"
                  value={filters.maxPlayers}
                  onChange={(e) => setFilters({...filters, maxPlayers: e.target.value})}
                />
              </div>
            </div>
            
            <div className="flex justify-end mt-4">
              <Button onClick={applyFilters}>Apply Filters</Button>
            </div>
          </div>
        )}
        
        {/* Applied filters display - optional */}
        {hasActiveFilters() && (
          <div className="flex flex-wrap gap-2 mb-6">
            {filters.category && (
              <span className="bg-muted px-3 py-1 rounded-full text-sm flex items-center gap-1">
                Category: {filters.category}
                <X 
                  size={14} 
                  className="cursor-pointer" 
                  onClick={() => setFilters({...filters, category: ""})}
                />
              </span>
            )}
            {filters.minPlayers && (
              <span className="bg-muted px-3 py-1 rounded-full text-sm flex items-center gap-1">
                Min Players: {filters.minPlayers}
                <X 
                  size={14} 
                  className="cursor-pointer" 
                  onClick={() => setFilters({...filters, minPlayers: ""})}
                />
              </span>
            )}
            {filters.maxPlayers && (
              <span className="bg-muted px-3 py-1 rounded-full text-sm flex items-center gap-1">
                Max Players: {filters.maxPlayers}
                <X 
                  size={14} 
                  className="cursor-pointer" 
                  onClick={() => setFilters({...filters, maxPlayers: ""})}
                />
              </span>
            )}
          </div>
        )}
        
        {/* Game display area - expands with content */}
        <div className="flex-1 border rounded-lg">
          <div className="flex flex-col">
            {/* Header/Status area */}
            <div className="py-3 px-4 border-b">
              <p className="text-sm text-muted-foreground">
                {filteredGames.length === 0 
                  ? "No games found matching your criteria" 
                  : `Showing ${filteredGames.length} game${filteredGames.length !== 1 ? 's' : ''}`}
              </p>
            </div>
            
            {/* Games content area - no scroll */}
            <div className="p-4">
              {filteredGames.length === 0 ? (
                <div className="w-full py-24 flex items-center justify-center">
                  <p className="text-lg text-muted-foreground">Try adjusting your filters to find games.</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 w-full">
                  {filteredGames.map(game => (
                    <Dialog key={game.name}>
                      <DialogTrigger asChild>
                        <div className="h-[500px]"> {/* Increased card container height */}
                          <GameCard game={game} showInstanceCount={true} />
                        </div>
                      </DialogTrigger>
                      <GameDetailsDialog 
                        game={game} 
                        onRequestGame={handleRequestGame} 
                      />
                    </Dialog>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
      
      {/* Request Game Modal */}
      <RequestGameDialog 
        open={isRequestModalOpen} 
        onOpenChange={setIsRequestModalOpen}
        onSubmit={handleSubmitRequest}
        game={selectedGame}
      />
    </div>
  );
}