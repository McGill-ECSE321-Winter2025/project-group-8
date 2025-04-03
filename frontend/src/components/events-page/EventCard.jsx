import { Button } from "@/components/ui/button";

export function EventCard({ event }) {
  return (
    <div className="rounded-lg overflow-hidden bg-white shadow">
      {/* Card Header with Image */}
      <div className="w-full h-90 bg-gray-200 relative">
        {event.featuredGameImage && (
          <img
            src={event.featuredGameImage}
            alt={event.featuredGame}
            className="w-full h-full object-cover object-center"
          />
        )}
        {/* Gradient overlay for better text visibility */}
        <div className="absolute inset-0 bg-gradient-to-t from-black to-transparent opacity-70"></div>
        
        {/* Title text with improved positioning and shadow */}
        <div className="absolute bottom-0 left-0 p-6 w-full">
          <h3 className="font-bold text-3xl text-white drop-shadow-lg shadow-black">
            {event.title}
          </h3>
          <div className="flex items-center text-white mt-2 drop-shadow-md">
            <span className="font-medium">
              {new Date(event.dateTime).toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
                year: 'numeric'
              })}
            </span>
          </div>
        </div>
      </div>

      {/* Card Content */}
      <div className="p-4">
        {/* Date and Time */}
        <div className="flex items-center text-gray-700 mb-2">
          <svg className="w-5 h-5 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="16" y1="2" x2="16" y2="6"></line>
            <line x1="8" y1="2" x2="8" y2="6"></line>
            <line x1="3" y1="10" x2="21" y2="10"></line>
          </svg>
          <span>
            {new Date(event.dateTime).toLocaleDateString('en-US', {
              month: 'short',
              day: 'numeric',
              year: 'numeric'
            })}
            {' - '}
            {new Date(event.dateTime).toLocaleTimeString([], {
              hour: '2-digit',
              minute: '2-digit'
            })}
          </span>
        </div>

        {/* Location */}
        <div className="flex items-center text-gray-700 mb-2">
          <svg className="w-5 h-5 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
            <circle cx="12" cy="10" r="3"></circle>
          </svg>
          <span>{event.location}</span>
        </div>

        {/* Game */}
        <div className="flex items-center text-gray-700 mb-4">
          <svg className="w-5 h-5 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
            <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
          </svg>
          <span>{event.featuredGame}</span>
        </div>

        {/* Participants */}
        <div className="flex items-center text-gray-700 mb-4">
          <svg className="w-5 h-5 mr-2" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
          <span>{event.participantCount}/{event.maxParticipants} participants</span>
        </div>

        {/* Register Button */}
        <Button 
          className="w-full bg-black hover:bg-gray-800 text-white"
        >
          Register
        </Button>
      </div>
    </div>
  );
}