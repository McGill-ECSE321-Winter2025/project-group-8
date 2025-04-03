import { useState, useEffect } from "react";
import { Input } from "../../ui/input";
import { Search, CalendarX, RefreshCw } from "lucide-react";
import { searchEventsByTitle } from "../../service/api";
import { EventCard } from "./EventCard";
import { motion, AnimatePresence } from "framer-motion";

// Animation variants for search results
const resultsContainer = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
      delayChildren: 0.3 // Delay to allow previous content to fully exit
    }
  }
};

const resultItem = {
  hidden: { opacity: 0, y: 20 },
  show: { 
    opacity: 1, 
    y: 0,
    transition: { 
      type: "spring",
      stiffness: 300,
      damping: 24
    }
  }
};

export function EventSearchBar({ onSearchStateChange }) {
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [prevSearchTerm, setPrevSearchTerm] = useState("");

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      if (searchTerm.trim() !== "") {
        // Only trigger search state change if we have a new search term
        if (searchTerm !== prevSearchTerm) {
          onSearchStateChange && onSearchStateChange(true);
          setPrevSearchTerm(searchTerm);
        }
        handleSearch();
      } else {
        setSearchResults([]);
        setHasSearched(false);
        onSearchStateChange && onSearchStateChange(false);
        setPrevSearchTerm("");
      }
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [searchTerm, onSearchStateChange, prevSearchTerm]);

  const handleSearch = async () => {
    setIsLoading(true);
    try {
      const results = await searchEventsByTitle(searchTerm);
      setSearchResults(results);
      setHasSearched(true);
    } catch (error) {
      console.error("Search error:", error);
      setSearchResults([]);
      setHasSearched(true);
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

      {/* Search Results with AnimatePresence for smooth transitions */}
      <AnimatePresence mode="wait">
        {isLoading ? (
          <motion.div 
            className="py-12 text-center"
            key="loading"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <RefreshCw className="h-12 w-12 mx-auto mb-4 text-gray-400 animate-spin" />
            <p className="text-muted-foreground text-lg">Searching for events...</p>
          </motion.div>
        ) : searchTerm && hasSearched && searchResults.length === 0 ? (
          <motion.div 
            className="py-12 text-center"
            key="no-results"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            transition={{ type: "spring", stiffness: 300, damping: 25 }}
          >
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
          </motion.div>
        ) : searchTerm && searchResults.length > 0 ? (
          <motion.div 
            className="space-y-4"
            key="results"
            variants={resultsContainer}
            initial="hidden"
            animate="show"
          >
            {searchResults.map((event) => (
              <motion.div key={event.id} variants={resultItem}>
                <EventCard event={event} />
              </motion.div>
            ))}
          </motion.div>
        ) : null}
      </AnimatePresence>
    </div>
  );
}