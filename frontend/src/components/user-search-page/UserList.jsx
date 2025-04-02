import React from 'react';
import UserProfileCard from './UserProfileCard';
import { Skeleton } from "../../../src/components/ui/skeleton";

const UserList = ({ users = [], isLoading, error, emptyMessage = "No users found.", onUserClick }) => {
  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* Render 6 skeleton placeholders */}
        {[...Array(6)].map((_, index) => (
          <div key={index} className="flex flex-col space-y-3">
            <Skeleton className="h-[125px] w-full rounded-xl" />
            <div className="space-y-2">
              <Skeleton className="h-4 w-[80%]" />
              <Skeleton className="h-4 w-[60%]" />
            </div>
          </div>
        ))}
      </div>
    );
  }

  if (error) {
    return <div className="p-8 text-center text-red-600">Error: {error}</div>; // Enhanced error styling
  }

  if (users.length === 0) {
    return <div className="p-8 text-center text-muted-foreground">{emptyMessage}</div>;
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"> {/* Responsive grid layout */}
      {users.map((user) => (
        <UserProfileCard
          key={user.id || user.email} // Use id if available, fallback to email
          user={user}
          onClick={() => onUserClick(user)} // Pass the specific user to the handler
        />
      ))}
    </div>
  );
};

export default UserList;