import { useState, useEffect } from "react";
import { Button } from "@/components/ui/button";
import confetti from "canvas-confetti";
import { registerForEvent, unregisterFromEvent } from "@/service/event-api.js";
import { motion, AnimatePresence } from "framer-motion";
import {
  Card,
  CardContent
} from "../../ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Calendar, AlertCircle, Users, MapPin, Gamepad2, Info } from "lucide-react";
import { cn } from '@/components/lib/utils';
import { toast } from "sonner";

// Accept onRegistrationUpdate, isCurrentUserRegistered, and registrationId props
export function EventCard({ event, onRegistrationUpdate, isCurrentUserRegistered, registrationId }) {
  // Initialize isRegistered state based on the prop passed from the parent
  const [isRegistered, setIsRegistered] = useState(isCurrentUserRegistered || false);
  const [isAnimating, setIsAnimating] = useState(false);
  const [error, setError] = useState(null);
  const [showDescription, setShowDescription] = useState(false);
  const [isCancelConfirmOpen, setIsCancelConfirmOpen] = useState(false);

  // Update isRegistered state if the prop changes (e.g., after parent refresh)
  useEffect(() => {
    setIsRegistered(isCurrentUserRegistered || false);
  }, [isCurrentUserRegistered]);

  const handleRegisterClick = async (e) => {
    setError(null);

    if (isRegistered) {
      setIsCancelConfirmOpen(true);
      return;
    }

    // Register logic
    try {
      setIsAnimating(true);
      const { clientX: x, clientY: y } = e;

      await registerForEvent(event.id); // Pass event ID

      // If successful:
      setIsRegistered(true); // Update button state locally first
      setIsAnimating(false);
      toast.success(`Successfully registered for ${event.title || event.name}!`);
      if (onRegistrationUpdate) { // Call the refresh function from parent
        onRegistrationUpdate(); // This will cause props to update
      }

      // Trigger confetti
      confetti({
        particleCount: 100,
        spread: 70,
        origin: { x: x / window.innerWidth, y: y / window.innerHeight },
      });
    } catch (error) {
      setIsAnimating(false);
      const errorMsg = error.message || "Something went wrong. Please try again.";
      if (errorMsg.includes("full capacity")) {
        setError("⚠️ Event is at full capacity!");
        toast.error("Event is at full capacity!");
      } else if (errorMsg.includes("already registered")) {
        setError("❌ You are already registered for this event!");
        setIsRegistered(true); // Sync state
        toast.warning("You are already registered for this event!");
      } else {
        setError(errorMsg);
        toast.error(errorMsg);
      }
    }
  };
  

  const handleConfirmCancelRegistration = async () => {
     setError(null);
     try {
       // Check if we actually have a registration ID to delete
       if (!registrationId) {
         throw new Error("Cannot unregister: Registration ID not found.");
       }
       setIsAnimating(true);
       // Call the actual unregister function with the correct registration ID
       await unregisterFromEvent(registrationId);

       // If successful:
       setIsRegistered(false); // Update button state locally first
       setIsAnimating(false);
       setIsCancelConfirmOpen(false);
       toast.info(`Unregistered from ${event.title || event.name}.`);
       if (onRegistrationUpdate) { // Call the refresh function from parent
         onRegistrationUpdate(); // This will cause props to update
       }
     } catch (error) {
       setIsAnimating(false);
       const errorMsg = error.message || "Something went wrong. Please try again.";
       setError(errorMsg);
       toast.error(errorMsg);
       setIsCancelConfirmOpen(false);
     }
  };


  const toggleDescription = () => {
    setShowDescription(!showDescription);
  };

  // Safely format date/time
  const eventDate = event.dateTime ? new Date(event.dateTime) : null;
  const formattedDate = eventDate ? eventDate.toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" }) : 'Date N/A';
  const formattedTime = eventDate ? eventDate.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) : 'Time N/A';

  return (
    <motion.div
      className="rounded-lg overflow-hidden bg-white shadow flex flex-col h-full"
      whileHover={{ y: -5, boxShadow: "0 10px 25px rgba(0,0,0,0.1)" }}
      transition={{ type: "spring", stiffness: 300, damping: 20 }}
    >
      {/* Event Image */}
      <div className="w-full h-48 bg-gray-200 relative flex-shrink-0">
        {event.featuredGameImage && event.featuredGameImage !== "https://placehold.co/400x300/e9e9e9/1d1d1d?text=No+Image" ? (
          <img
            src={event.featuredGameImage}
            alt={event.featuredGame?.name || "Featured Game"}
            className="w-full h-full object-cover object-center"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-muted">
             <span className="text-muted-foreground">No Image</span>
          </div>
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent"></div>
        <div className="absolute bottom-0 left-0 p-4 w-full">
          <h3 className="font-bold text-xl text-white drop-shadow-lg shadow-black line-clamp-2">
            {event.title || event.name || 'Untitled Event'}
          </h3>
          <div className="flex items-center text-white mt-1 drop-shadow-md">
            <span className="font-medium text-xs">{formattedDate}</span>
          </div>
        </div>
      </div>

      {/* Event Details */}
      <div className="p-4 flex flex-col flex-grow">
        {/* Host Info */}
        <div className="flex items-center text-gray-700 mb-2 text-sm">
          <Users className="w-4 h-4 mr-2 flex-shrink-0" />
          <span className="font-medium truncate">
            Hosted by: {event.hostName || "Unknown Host"}
          </span>
        </div>

        {/* Game Info */}
        <div className="flex items-center text-gray-700 mb-2 text-sm">
          <Gamepad2 className="w-4 h-4 mr-2 flex-shrink-0" />
          <span className="truncate">
            Featured Game: {event.game || "Unknown Game"}
          </span>
        </div>

        {/* Location Info */}
        <div className="flex items-center text-gray-700 mb-2 text-sm">
          <MapPin className="w-4 h-4 mr-2 flex-shrink-0" />
          <span className="truncate">
            Location: {event.location || "Not specified"}
          </span>
        </div>

        {/* Date and Time */}
        <div className="flex items-center text-gray-700 mb-2 text-sm">
          <Calendar className="w-4 h-4 mr-2 flex-shrink-0" />
          <span>{formattedDate} - {formattedTime}</span>
        </div>

        {/* Participants - Display directly from prop */}
        <div className="flex items-center text-gray-700 mb-4 text-sm">
          <Users className="w-4 h-4 mr-2 flex-shrink-0" />
          <span>

            {event.currentNumberParticipants}/{event.maxParticipants} participants
          </span>
        </div>

        {/* Description Expandable Section */}
        <AnimatePresence>
          {showDescription && (
            <motion.div
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: "auto", opacity: 1, marginBottom: 8 }}
              exit={{ height: 0, opacity: 0, marginBottom: 0 }}
              transition={{ duration: 0.3 }}
              className="overflow-hidden"
            >
              <Card className="bg-gray-50 border-border/50">
                <CardContent className="pt-3 pb-3 px-4">
                  <h4 className="font-semibold mb-1 text-sm flex items-center"><Info className="w-4 h-4 mr-1.5"/>Description</h4>
                  <p className="text-gray-700 text-sm">{event.description || "No description available."}</p>
                </CardContent>
              </Card>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Spacer to push buttons down */}
        <div className="flex-grow"></div>

        {/* Error Message */}
        {error && (
          <motion.div
            className="text-red-500 text-sm mb-2 text-center"
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0 }}
          >
            {error}
          </motion.div>
        )}

        {/* Action Buttons */}
        <div className="space-y-2 mt-2">
            <Button
              variant="outline"
              className="w-full text-sm"
              onClick={toggleDescription}
            >
              {showDescription ? "Hide Details" : "Show Details"}
            </Button>
            <Button
              className={`w-full text-white transition-all duration-300 text-sm ${
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

         {/* Unregister Confirmation Dialog */}
         <Dialog open={isCancelConfirmOpen} onOpenChange={setIsCancelConfirmOpen}>
            <DialogContent className="sm:max-w-[425px]">
              <DialogHeader>
                <DialogTitle className="flex items-center gap-2">
                  <AlertCircle className="h-5 w-5 text-destructive" />
                  Cancel Registration
                </DialogTitle>
                <DialogDescription>
                  Are you sure you want to cancel your registration for "{event.title || event.name}"? 
                </DialogDescription>
              </DialogHeader>
              <DialogFooter>
                <Button variant="outline" onClick={() => setIsCancelConfirmOpen(false)}>
                  Keep Registration
                </Button>
                <Button variant="destructive" onClick={handleConfirmCancelRegistration} disabled={isAnimating}>
                  {isAnimating ? "Cancelling..." : "Cancel Registration"}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>

      </div>
    </motion.div>
  );
}
