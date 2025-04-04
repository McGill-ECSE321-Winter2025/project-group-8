import React, { memo } from 'react';
import UserProfileCard from './UserProfileCard';
import { Skeleton } from "@/components/ui/skeleton";
import { motion } from 'framer-motion';

// Staggered animation variants
const containerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1
    }
  }
};

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: {
    opacity: 1,
    y: 0,
    transition: { type: "spring", stiffness: 300, damping: 24 }
  }
};

// Memoized UserProfileCard component for better performance
const MemoizedUserProfileCard = memo(UserProfileCard);

const UserList = ({ users = [], isLoading, error, emptyMessage = "No users found.", onUserClick }) => {
  if (isLoading) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {/* Render skeleton placeholders */}
        {[...Array(6)].map((_, index) => (
          <motion.div 
            key={index} 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.3, delay: index * 0.05 }}
            className="flex flex-col space-y-3"
          >
            <Skeleton className="h-[150px] w-full rounded-xl" />
            <div className="space-y-2">
              <Skeleton className="h-4 w-[80%]" />
              <Skeleton className="h-4 w-[60%]" />
            </div>
          </motion.div>
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="p-8 text-center rounded-lg bg-red-50 border border-red-200 text-red-600"
      >
        Error: {error}
      </motion.div>
    );
  }

  if (users.length === 0) {
    return (
      <motion.div 
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        className="p-8 text-center rounded-lg bg-muted/30 text-muted-foreground"
      >
        {emptyMessage}
      </motion.div>
    );
  }

  return (
    <motion.div 
      className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
      variants={containerVariants}
      initial="hidden"
      animate="visible"
    >
      {users.map((user) => (
        <motion.div key={user.id || user.email} variants={itemVariants} className="h-full">
          <MemoizedUserProfileCard
            user={user}
            onClick={() => onUserClick(user)}
          />
        </motion.div>
      ))}
    </motion.div>
  );
};

export default UserList;