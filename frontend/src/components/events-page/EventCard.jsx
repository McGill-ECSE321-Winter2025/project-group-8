import { useState } from "react";
import { Button } from "@/components/ui/button";
import confetti from "canvas-confetti";
import { registerForEvent, unregisterFromEvent } from "../../service/api";
import { motion } from "framer-motion";

export function EventCard({ event, attendeeId }) {
  const [isRegistered, setIsRegistered] = useState(false);
  const [isAnimating, setIsAnimating] = useState(false);
  const [error, setError] = useState(null);

  const handleRegisterClick = async (e) => {
    setError(null); // Reset error state

    if (isRegistered) {
      // Unregister logic
      try {
        setIsAnimating(true);
        const response = await unregisterFromEvent(event.registrationId);

        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.message || "Unregistration failed");
        }

        setIsRegistered(false);
        setIsAnimating(false);
      } catch (error) {
        setIsAnimating(false);
        setError(error.message || "Something went wrong. Please try again.");
      }
      return;
    }

    // Register logic
    try {
      setIsAnimating(true);
      const { clientX: x, clientY: y } = e;

      const response = await registerForEvent(attendeeId, event.id);

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Registration failed");
      }

      setIsRegistered(true);
      setIsAnimating(false);

      // Trigger confetti at the button click position
      confetti({
        particleCount: 100,
        spread: 70,
        origin: {
          x: x / window.innerWidth,
          y: y / window.innerHeight,
        },
      });
    } catch (error) {
      setIsAnimating(false);

      // Handle specific error messages
      if (error.message.includes("full capacity")) {
        setError("⚠️ Event is at full capacity!");
      } else if (error.message.includes("already exists")) {
        setError("❌ You are already registered for this event!");
      } else {
        setError(error.message || "Something went wrong. Please try again.");
      }
    }
  };

  return (
    <motion.div 
      className="rounded-lg overflow-hidden bg-white shadow"
      whileHover={{ y: -5, boxShadow: "0 10px 25px rgba(0,0,0,0.1)" }}
      transition={{ type: "spring", stiffness: 300, damping: 20 }}
    >
      {/* Event Image */}
      <div className="w-full h-90 bg-gray-200 relative">
        {event.featuredGameImage && (
          <img
            src={event.featuredGameImage}
            alt={event.featuredGame}
            className="w-full h-full object-cover object-center"
          />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-black to-transparent opacity-70"></div>
        <div className="absolute bottom-0 left-0 p-6 w-full">
          <h3 className="font-bold text-3xl text-white drop-shadow-lg shadow-black">
            {event.title}
          </h3>
          <div className="flex items-center text-white mt-2 drop-shadow-md">
            <span className="font-medium">
              {new Date(event.dateTime).toLocaleDateString("en-US", {
                month: "short",
                day: "numeric",
                year: "numeric",
              })}
            </span>
          </div>
        </div>
      </div>

      {/* Event Details */}
      <div className="p-4">
        <div className="flex items-center text-gray-700 mb-2">
          <svg
            className="w-5 h-5 mr-2"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="16" y1="2" x2="16" y2="6"></line>
            <line x1="8" y1="2" x2="8" y2="6"></line>
            <line x1="3" y1="10" x2="21" y2="10"></line>
          </svg>
          <span>
            {new Date(event.dateTime).toLocaleDateString("en-US", {
              month: "short",
              day: "numeric",
              year: "numeric",
            })}{" "}
            -{" "}
            {new Date(event.dateTime).toLocaleTimeString([], {
              hour: "2-digit",
              minute: "2-digit",
            })}
          </span>
        </div>

        {/* Participants */}
        <div className="flex items-center text-gray-700 mb-4">
          <svg
            className="w-5 h-5 mr-2"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
          <span>
            {event.participantCount}/{event.maxParticipants} participants
          </span>
        </div>

        {/* Error Message */}
        {error && (
          <motion.div 
            className="text-red-500 text-sm mb-2"
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0 }}
          >
            ❌ {error}
          </motion.div>
        )}

        {/* Register/Unregister Button */}
        <Button
          className={`w-full text-white transition-all duration-300 ${
            isRegistered
              ? "bg-red-500 hover:bg-red-600"
              : "bg-black hover:bg-gray-800"
          } ${isAnimating ? "scale-95" : "scale-100"}`}
          onClick={handleRegisterClick}
          disabled={isAnimating}
        >
          {isRegistered ? "Unregister" : "Register"}
        </Button>
      </div>
    </motion.div>
  );
}