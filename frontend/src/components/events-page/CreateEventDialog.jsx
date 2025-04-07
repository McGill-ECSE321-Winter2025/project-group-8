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

  // Load user's games when dialog opens
  useEffect(() => {
    // Only fetch when dialog is open and user email is available
    if (!open || !userEmail) {
      // Clear games if dialog closes or user logs out
      if (!open) {
        setUserGames([]);
        setGameSearchResults([]);
      }
      return;
    }

    console.log("Starting to fetch games...");
    setIsLoadingUserGames(true);
    setUserGames([]); // Clear previous games while loading
    setGameSearchResults([]);

    async function fetchUserGames() {
      try {
        console.log(`Fetching games for user ${userEmail}`);
        const games = await getGamesByOwner(userEmail);

        if (isMountedRef.current) { // Still check if mounted before setting state
          console.log(`Received ${games.length} games - updating state`);
          setUserGames(games);
          // Initially show all fetched games in the dropdown
          setGameSearchResults([...games]);
        }
      } catch (error) {
        console.error("Error fetching games:", error);
        if (isMountedRef.current) {
          // Optionally set an error state here
          setUserGames([]); // Clear games on error
          setGameSearchResults([]);
        }
      } finally {
        if (isMountedRef.current) {
          console.log("Setting isLoadingUserGames to false in finally block");
          setIsLoadingUserGames(false);
        }
      }
    }

    fetchUserGames();

  }, [open, userEmail]); // Dependencies remain the same

  // Watch the game search term input to trigger filtering
  const watchedGameSearchTerm = watch("gameSearchTermInput");

  // Filter user's games based on search term - separate from the fetch effect
  useEffect(() => {
    console.log(`Filtering games: ${userGames.length} available, search term: "${watchedGameSearchTerm || ''}"`);
    
    // If no games are loaded, nothing to filter
    if (userGames.length === 0) {
      console.log("No games to filter");
      return;
    }
    
    // Don't search if a game is already selected by ID
    if (selectedGameId) {
      return;
    }

    // If search term is empty, show all user games
    if (!watchedGameSearchTerm || watchedGameSearchTerm.trim() === '') {
      console.log(`Showing all ${userGames.length} games`);
      setGameSearchResults([...userGames]);
      return;
    }

    // Filter from loaded user games
    const searchTerm = watchedGameSearchTerm.toLowerCase().trim();
    const filteredGames = userGames.filter(game => 
      game.name.toLowerCase().includes(searchTerm)
    );
    console.log(`Filtered ${filteredGames.length} games matching "${searchTerm}"`);
    setGameSearchResults(filteredGames);
  }, [watchedGameSearchTerm, selectedGameId, userGames]);

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
    console.log("Input focused, setting isInputFocused to true");
    setIsInputFocused(true);
    
    // Always update game search results when focused
    if (userGames.length > 0 && !selectedGameId) {
      console.log(`Showing all ${userGames.length} games in dropdown on focus`);
      setGameSearchResults([...userGames]);
    }
  }, [userGames, selectedGameId]);

  // Handle showing dropdown
  const showDropdown = useCallback(() => {
    if (userGames.length > 0 && !selectedGameId) {
      console.log(`Manually showing all ${userGames.length} games in dropdown`);
      setIsInputFocused(true);
      setGameSearchResults([...userGames]);
    }
  }, [userGames, selectedGameId]);

  // Handle input blur
  const handleInputBlur = useCallback(() => {
    // Use timeout to allow click events on dropdown items to fire first
    setTimeout(() => {
      console.log("Hiding game search results");
      setIsInputFocused(false);
    }, 200);
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

  // Replace Label component to show/hide loading spinner
  const GameLabel = () => (
    <div className="flex items-center">
      <Label htmlFor="gameSearchTermInput">Your Game <span className="text-red-500">*</span></Label>
      {isLoadingUserGames && <Loader2 className="ml-2 h-4 w-4 animate-spin" />}
    </div>
  );

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
            <GameLabel />
            <div className="flex items-center gap-2">
              <Input
                id="gameSearchTermInput"
                placeholder={isLoadingUserGames ? "Loading your games..." : "Search your games..."}
                {...register("gameSearchTermInput")}
                onChange={(e) => {
                  setValue("gameSearchTermInput", e.target.value);
                  if (selectedGameId) setSelectedGameId(null);
                }}
                onFocus={() => setIsInputFocused(true)}
                onBlur={() => setTimeout(() => setIsInputFocused(false), 200)}
                autoComplete="off"
                className={!selectedGameId && submitError.includes("select") ? "border-red-500" : ""}
                disabled={isLoadingUserGames}
              />
            </div>
            
            {/* Display no games message if needed */}
            {userGames.length === 0 && !isLoadingUserGames && (
              <p className="text-sm text-yellow-600 mt-1">
                You don't have any games. Please add a game in your collection first.
                <br/>
                <span className="text-xs text-gray-500">
                  API returned {userGames?.length || 0} games.
                </span>
              </p>
            )}
            
            {/* Debug info */}
            <div className="text-xs text-muted-foreground mt-1">
              {isLoadingUserGames ? 
                "Loading your games..." : 
                `${userGames.length} games available for selection (${gameSearchResults.length} in dropdown)`
              }
            </div>
            
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

            {/* Simple Game Selector - Always visible when games are available */}
            {userGames.length > 0 && !selectedGameId && !isLoadingUserGames && (
              <div className="mt-2 border rounded-md border-gray-300 dark:border-gray-700">
                <div className="p-2 bg-gray-50 dark:bg-gray-800 border-b border-gray-300 dark:border-gray-700">
                  <p className="text-sm font-medium">Select a game: ({userGames.length} available)</p>
                </div>
                <div className="max-h-40 overflow-y-auto">
                  {gameSearchResults.length > 0 ? (
                    gameSearchResults.map((game) => (
                      <button
                        key={game.id}
                        type="button"
                        className="w-full text-left px-3 py-2 text-sm border-b border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700"
                        onClick={() => handleGameSelect(game)}
                      >
                        {game.name}
                      </button>
                    ))
                  ) : (
                    <div className="p-3 text-sm text-gray-500 text-center">
                      No matching games found
                    </div>
                  )}
                </div>
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