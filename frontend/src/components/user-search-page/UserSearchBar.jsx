import React, { useState } from 'react'; // Added useState
import { Filter } from 'lucide-react'; // Added Filter icon
import { Input } from "../../../src/components/ui/input.jsx"; // Path to src/src/components/ui/input
import { Button } from "../../../src/components/ui/button.jsx"; // Path to src/components/ui/button
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "../../../src/components/ui/dropdown-menu.jsx"; // Path to src/src/components/ui/dropdown-menu

// Added filterGameOwnersOnly and setFilterGameOwnersOnly props
const UserSearchBar = ({ searchQuery, setSearchQuery, onSearchSubmit, filterGameOwnersOnly, setFilterGameOwnersOnly }) => {
  // Handler for Enter key press
  const handleKeyDown = (event) => {
    if (event.key === 'Enter') {
      onSearchSubmit();
    }
  };
  return (
    <div className="flex w-full items-center gap-3 bg-card p-4 rounded-lg shadow-sm"> {/* Increased gap, added bg/padding/rounding/shadow */}
      <Input
        type="text"
        placeholder="Search users..."
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        onKeyDown={handleKeyDown} // Added Enter key listener
        className="flex-grow"
      />
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="outline" size="icon">
            <Filter className="h-4 w-4" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuLabel>Filter By</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuCheckboxItem
            checked={filterGameOwnersOnly}
            onCheckedChange={setFilterGameOwnersOnly}
          >
            Game Owner
          </DropdownMenuCheckboxItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
};

export default UserSearchBar;