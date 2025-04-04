import React, { useState } from 'react';
import { Filter, Search, X } from 'lucide-react'; // Added Search and X icons
import { Input } from "@/components/ui/input.jsx";
import { Button } from "@/components/ui/button.jsx";
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu.jsx";
import { motion } from "framer-motion"; // Import framer-motion
import { cn } from "@/components/lib/utils"; // Import cn utility

// Added filterGameOwnersOnly and setFilterGameOwnersOnly props
const UserSearchBar = ({ 
  searchQuery, 
  setSearchQuery, 
  onSearchSubmit, 
  filterGameOwnersOnly, 
  setFilterGameOwnersOnly 
}) => {
  const [isFocused, setIsFocused] = useState(false);
  
  // Handler for Enter key press
  const handleKeyDown = (event) => {
    if (event.key === 'Enter') {
      onSearchSubmit();
    }
  };

  // Handle clearing the search input
  const handleClearSearch = () => {
    setSearchQuery('');
    // Optionally trigger a search with empty query
    onSearchSubmit();
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: -10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className={cn(
        "flex w-full items-center gap-3 p-4 rounded-lg",
        "bg-card/80 backdrop-blur-sm",
        "border border-border/50",
        "transition-shadow duration-200",
        isFocused ? "shadow-md" : "shadow-sm"
      )}
    >
      <div className="relative flex-grow flex items-center">
        <Search className="absolute left-3 h-4 w-4 text-muted-foreground" />
        <Input
          type="text"
          placeholder="Search users..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyDown={handleKeyDown}
          onFocus={() => setIsFocused(true)}
          onBlur={() => setIsFocused(false)}
          className={cn(
            "pl-10 pr-8 transition-all",
            "bg-transparent"
          )}
        />
        {searchQuery && (
          <motion.button
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.8 }}
            className="absolute right-3 rounded-full p-1 hover:bg-muted focus:outline-none focus:ring-1 focus:ring-primary"
            onClick={handleClearSearch}
            aria-label="Clear search"
          >
            <X className="h-3 w-3 text-muted-foreground" />
          </motion.button>
        )}
      </div>
      
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button 
            variant="outline" 
            size="icon" 
            className={cn(
              "transition-all duration-200",
              filterGameOwnersOnly && "bg-primary/10 text-primary border-primary/20"
            )}
          >
            <Filter className="h-4 w-4" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="w-56">
          <DropdownMenuLabel>Filter Users</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuCheckboxItem
            checked={filterGameOwnersOnly}
            onCheckedChange={setFilterGameOwnersOnly}
          >
            Game Owner
          </DropdownMenuCheckboxItem>
          {/* Additional filters can be added here */}
        </DropdownMenuContent>
      </DropdownMenu>
      
      <Button 
        onClick={onSearchSubmit}
        className="transition-all duration-200 hover:shadow-md"
      >
        Search
      </Button>
    </motion.div>
  );
};

export default UserSearchBar;