import React from 'react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../../../src/components/ui/card.jsx';
import {
  Avatar,
  AvatarFallback,
  AvatarImage,
} from '../../../src/components/ui/avatar.jsx'; // Using alias path

const UserProfileCard = ({ user, onClick }) => {
  // Provide default values or placeholders if user data might be missing
  const username = user?.username || 'N/A';

  return (
    <Card className="mb-4 transition-shadow hover:shadow-md cursor-pointer" onClick={onClick}> {/* Added margin-bottom, hover effect, cursor, and onClick */}
      <CardHeader className="flex flex-row items-center space-x-4">
        <Avatar>
          {/* If user.avatarUrl exists, use it, otherwise show fallback */}
          <AvatarImage src={user?.avatarUrl} alt={username} />
          {/* Fallback uses initials - get first letter or 'U' */}
          <AvatarFallback>{username ? username[0].toUpperCase() : 'U'}</AvatarFallback>
        </Avatar>
        <CardTitle className="mb-0">{username}</CardTitle> {/* Added mb-0 to remove default margin */}
      </CardHeader>
    </Card>
  );
};

export default UserProfileCard;