import { useState, useEffect, useCallback, useRef } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from "../../ui/dialog";
import { Button } from "../../ui/button";
import { Input } from "../../ui/input";
import { Textarea } from "../../ui/textarea";
import { Label } from "../../ui/label";
import { createEvent } from "../../service/event-api.js";
// Import searchGames API function and Loader icon
import { getGamesByOwner } from "../../service/game-api.js";
import { Loader2 } from "lucide-react";
import { useAuth } from "@/context/AuthContext"; // Import useAuth

// Accept onEventAdded prop
export default function CreateEventDialog({ open, onOpenChange, onEventAdded }) {
  const [isLoading, setIsLoading] = useState(false);
  const [submitError, setSubmitError] = useState("");
  // State for game search
  const [gameSearchResults, setGameSearchResults] = useState([]);
  const [selectedGameId, setSelectedGameId] = useState(null);
  const [isSearchingGames, setIsSearchingGames] = useState(false);
  const [userGames, setUserGames] = useState([]);
  const [isLoadingUserGames, setIsLoadingUserGames] = useState(false);
  const { user } = useAuth(); // Get user from AuthContext
  
  // Track if component is mounted
  const isMountedRef = useRef(true);
  // Track last fetched email to prevent redundant fetches
  const fetchedRef = useRef({
    email: null,
    timestamp: 0
  });

  // Add state to track input focus
  const [isInputFocused, setIsInputFocused] = useState(false);

  const { register, handleSubmit, formState: { errors }, reset, setValue, watch } = useForm({
    defaultValues: {
      title: "",
      dateTime: "",
      location: "",
      description: "",
      maxParticipants: "",
      gameSearchTermInput: "",
    },
  });

  // Get current user's email from context
  const userEmail = user?.email;
  
  // Cleanup on unmount
  useEffect(() => {
    return () => {
      isMountedRef.current = false;
    };
  }, []);

  // Debug loading state
  useEffect(() => {
    if (isLoadingUserGames) {
      console.log("Loading user games...");
    } else {
      console.log(`User games loaded: ${userGames.length} games available`);
    }
  }, [isLoadingUserGames, userGames.length]);

  // Load user's games when dialog opens
  useEffect(() => {
    // Only fetch when dialog is open and user email is available
    if (!open || !userEmail) return;
    
    // Don't refetch if already loaded for this user recently (within 5 minutes)
    const now = Date.now();
    const isFresh = fetchedRef.current.email === userEmail && 
                    (now - fetchedRef.current.timestamp) < 5 * 60 * 1000;
                    
    if (isFresh && userGames.length > 0) {
      console.log("Using cached games data");
      return;
    }

    // Don't start a new fetch if already loading
    if (isLoadingUserGames) {
      console.log("Already loading games, skipping");
      return;
    }
    
    async function fetchUserGames() {
      setIsLoadingUserGames(true);
      
      try {
        console.log(`Fetching games for user ${userEmail}`);
        const games = await getGamesByOwner(userEmail);
        
        if (isMountedRef.current) {
          console.log(`Received ${games.length} games`);
          setUserGames(games);
          
          // Update fetch timestamp
          fetchedRef.current = {
            email: userEmail,
            timestamp: Date.now()
          };
        }
      } catch (error) {
        console.error("Error fetching games:", error);
        if (isMountedRef.current) {
          toast.error("Failed to load your games");
        }
      } finally {
        if (isMountedRef.current) {
          setIsLoadingUserGames(false);
        }
      }
    }
    
    fetchUserGames();
    
    // Add a safety timeout to reset loading state if stuck
    const safetyTimer = setTimeout(() => {
      if (isMountedRef.current && isLoadingUserGames) {
        console.log("Safety timeout triggered - resetting loading state");
        setIsLoadingUserGames(false);
      }
    }, 10000);
    
    return () => clearTimeout(safetyTimer);
  }, [open, userEmail, isLoadingUserGames, userGames.length]);

  // Watch the game search term input to trigger filtering
  const watchedGameSearchTerm = watch("gameSearchTermInput");

  // Filter user's games based on search term
  useEffect(() => {
    // If no games are loaded, don't try to filter
    if (userGames.length === 0) {
      setGameSearchResults([]);
      return;
    }

    // If search term is empty, show all user games in the dropdown if input is focused
    if (!watchedGameSearchTerm) {
      if (isInputFocused) {
        setGameSearchResults(userGames);
      } else {
        setGameSearchResults([]);
      }
      return;
    }

    // Don't search if a game is already selected by ID
    if (selectedGameId) {
      setGameSearchResults([]);
      return;
    }

    // Filter from already loaded user games
    const searchTerm = watchedGameSearchTerm.toLowerCase();
    const filteredGames = userGames.filter(game => 
      game.name.toLowerCase().includes(searchTerm)
    );
    console.log(`Filtered ${filteredGames.length} games matching "${searchTerm}"`);
    setGameSearchResults(filteredGames);
  }, [watchedGameSearchTerm, selectedGameId, userGames, isInputFocused]);

  const handleGameSelect = useCallback((game) => {
    setSelectedGameId(game.id);
    setValue("gameSearchTermInput", game.name);
    setGameSearchResults([]);
    setSubmitError("");
  }, [setValue]);

  const onSubmit = handleSubmit(async (data) => {
    // Manually check if a game was selected
    if (!selectedGameId) {
      setSubmitError("Please select one of your games from the search results.");
      return;
    }

    // Verify the selected game belongs to the user
    const isUserGame = userGames.some(game => game.id === selectedGameId);
    if (!isUserGame) {
      setSubmitError("You can only create events for games you own.");
      return;
    }

    // Remove the temporary search input value from the data to be submitted
    const { gameSearchTermInput, ...formData } = data;

    setIsLoading(true);
    setSubmitError("");

    // Add the selected game ID to the data payload
    const payload = {
      ...formData,
      featuredGameId: selectedGameId,
    };

    try {
      const result = await createEvent(payload);
      
      if (isMountedRef.current) {
        toast.success(`Successfully created event: ${result.title}`);
        if (onEventAdded) {
          onEventAdded();
        }
        handleCancel();
      }
    } catch (error) {
      console.error("Create event error:", error);
      
      if (isMountedRef.current) {
        const errorMsg = error.message || "Failed to create event. Please try again.";
        setSubmitError(errorMsg);
        toast.error(errorMsg);
      }
    } finally {
      if (isMountedRef.current) {
        setIsLoading(false);
      }
    }
  });

  // Custom reset function to clear game search state as well
  const handleCancel = useCallback(() => {
    reset();
    setSelectedGameId(null);
    setGameSearchResults([]);
    setSubmitError("");
    onOpenChange(false);
  }, [reset, onOpenChange]);

  // Custom UI helper function to handle input focus
  const handleInputFocus = useCallback(() => {
    setIsInputFocused(true);
    // When input is focused and we have games but no search text, show all games
    if (userGames.length > 0 && !selectedGameId && !watchedGameSearchTerm) {
      setGameSearchResults(userGames);
    }
  }, [userGames, selectedGameId, watchedGameSearchTerm]);

  // Handle input blur
  const handleInputBlur = useCallback(() => {
    // Use timeout to allow click events on dropdown items to fire first
    setTimeout(() => setIsInputFocused(false), 200);
  }, []);

  // Reset state when dialog opens to ensure a fresh start
  useEffect(() => {
    if (open) {
      // Clear any selected game and search results when dialog opens
      // but don't reset form inputs that might be partially filled
      setSelectedGameId(null);
      setGameSearchResults([]);
      setSubmitError("");
    }
  }, [open]);

  return (
    <Dialog open={open} onOpenChange={(isOpen) => {
      if (!isOpen) handleCancel();
      else onOpenChange(true);
    }}>
      <DialogContent className="sm:max-w-[525px]">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Create New Event</DialogTitle>
          <DialogDescription>
            Fill out the form below to create a new event featuring one of your games.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={onSubmit} className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="title">Event Title <span className="text-red-500">*</span></Label>
            <Input
              id="title"
              {...register("title", { required: "Title is required" })}
              className={errors.title ? "border-red-500" : ""}
            />
            {errors.title && <p className="text-red-500 text-sm">{errors.title.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="dateTime">Date and Time <span className="text-red-500">*</span></Label>
            <Input
              id="dateTime"
              type="datetime-local"
              {...register("dateTime", { required: "Date and time is required" })}
              className={errors.dateTime ? "border-red-500" : ""}
            />
            {errors.dateTime && <p className="text-red-500 text-sm">{errors.dateTime.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="location">Location</Label>
            <Input id="location" {...register("location")} />
          </div>

          {/* Game Search Input and Results - Now filtering from user's games */}
          <div className="space-y-2 relative">
            <Label htmlFor="gameSearchTermInput">
              Your Game <span className="text-red-500">*</span>
              {isLoadingUserGames && <Loader2 className="ml-2 inline h-4 w-4 animate-spin" />}
            </Label>
            <div className="flex items-center gap-2">
              <Input
                id="gameSearchTermInput"
                placeholder={isLoadingUserGames ? "Loading your games..." : "Search your games..."}
                {...register("gameSearchTermInput")}
                onChange={(e) => {
                  setValue("gameSearchTermInput", e.target.value);
                  if (selectedGameId) setSelectedGameId(null);
                }}
                onFocus={handleInputFocus}
                onBlur={handleInputBlur}
                autoComplete="off"
                className={!selectedGameId && submitError.includes("select") ? "border-red-500" : ""}
                disabled={isLoadingUserGames} // Disable while loading user games
              />
            </div>
            
            {/* Debug info - will help troubleshoot the loading state */}
            <div className="text-xs text-muted-foreground mt-1">
              {isLoadingUserGames ? 
                "Loading your games..." : 
                `${userGames.length} games available for selection`
              }
            </div>
            
            {/* Display no games message if needed */}
            {userGames.length === 0 && !isLoadingUserGames && (
              <p className="text-sm text-yellow-600 mt-1">
                You don't have any games. Please add a game in your collection first.
              </p>
            )}
            
            {/* Display selected game */}
            {selectedGameId && (
              <div className="text-sm font-medium text-green-600 mt-1">
                Selected game: {userGames.find(g => g.id === selectedGameId)?.name}
              </div>
            )}
            
            {/* Display error if submit attempted without selection */}
            {submitError && submitError.includes("select") && (
              <p className="text-red-500 text-sm">{submitError}</p>
            )}

            {/* Search Results Dropdown - Shows all or filtered games */}
            {!selectedGameId && !isLoadingUserGames && (
              <div className="relative">
                {gameSearchResults.length > 0 && isInputFocused && (
                  <ul className="absolute z-10 w-full bg-background border border-border rounded-md mt-1 max-h-40 overflow-y-auto shadow-lg">
                    {/* Show header if displaying all games */}
                    {!watchedGameSearchTerm && gameSearchResults.length === userGames.length && userGames.length > 0 && (
                      <li className="px-3 py-1 text-xs text-muted-foreground border-b">
                        All your games
                      </li>
                    )}
                    
                    {/* Show filtered results */}
                    {gameSearchResults.map((game) => (
                      <li
                        key={game.id}
                        className="px-3 py-2 hover:bg-accent cursor-pointer text-sm"
                        onClick={() => handleGameSelect(game)}
                      >
                        {game.name}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="maxParticipants">Maximum Participants <span className="text-red-500">*</span></Label>
            <Input
              id="maxParticipants"
              type="number"
              min="1"
              {...register("maxParticipants", {
                required: "Maximum participants is required",
                valueAsNumber: true,
                min: { value: 1, message: "Must be greater than 0" }
              })}
              className={errors.maxParticipants ? "border-red-500" : ""}
            />
            {errors.maxParticipants && <p className="text-red-500 text-sm">{errors.maxParticipants.message}</p>}
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              {...register("description")}
              className="min-h-[100px]"
            />
          </div>

          {/* Display general submit error only if it's not the game selection error */}
          {submitError && !submitError.includes("select") && (
            <p className="text-red-500 text-sm text-center">{submitError}</p>
          )}

          <DialogFooter className="pt-4">
            <Button variant="outline" type="button" onClick={handleCancel}>
              Cancel
            </Button>
            <Button 
              type="submit" 
              disabled={isLoading || isLoadingUserGames || userGames.length === 0}
            >
              {isLoading ? "Creating..." : "Create Event"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}