import { useState, useEffect } from "react";
import { Input } from "../../ui/input";
import { Search, CalendarX, RefreshCw } from "lucide-react";
import { searchEventsByTitle } from "../../service/api";
import { EventCard } from "./EventCard";

export function EventSearchBar({ onSearchStateChange }) {
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      if (searchTerm.trim() !== "") {
        handleSearch();
        onSearchStateChange && onSearchStateChange(true);
      } else {
        setSearchResults([]);
        onSearchStateChange && onSearchStateChange(false);
      }
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [searchTerm, onSearchStateChange]);

  const handleSearch = async () => {
    setIsLoading(true);
    try {
      const results = await searchEventsByTitle(searchTerm);
      setSearchResults(results);
    } catch (error) {
      console.error("Search error:", error);
      setSearchResults([]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="w-full">
      {/* Search Input */}
      <div className="relative max-w mb-6">
        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
        <Input
          type="search"
          placeholder="Search events..."
          className="w-full pl-10"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {/* Search Results */}
      {isLoading ? (
        <div className="py-12 text-center">
          <RefreshCw className="h-12 w-12 mx-auto mb-4 text-gray-400 animate-spin" />
          <p className="text-muted-foreground text-lg">Searching for events...</p>
        </div>
      ) : searchTerm && searchResults.length === 0 ? (
        <div className="py-12 text-center">
          <div className="flex flex-col items-center justify-center">
            <div className="relative">
              <div className="absolute inset-0 bg-gray-100 rounded-full scale-125 opacity-30 animate-pulse"></div>
              <CalendarX className="h-16 w-16 mb-4 text-gray-400 relative z-10 animate-bounce" />
            </div>
            <h3 className="text-xl font-semibold mb-2">No events found</h3>
            <p className="text-muted-foreground mb-4 max-w-md">
              We couldn't find any events matching "{searchTerm}". Try a different search term or create your own event!
            </p>
          </div>
        </div>
      ) : (
        <div className="space-y-4">
          {searchResults.map((event) => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      )}
    </div>
  );
}