import { useState, useEffect } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "../../ui/dialog";
import { Button } from "../../ui/button";
import { Input } from "../../ui/input";
import { Textarea } from "../../ui/textarea";
import { Label } from "../../ui/label";
import { createEvent } from "../../service/event-api.js";
// Import searchGames API function and Loader icon
import { searchGames, getGamesByOwner } from "../../service/game-api.js";
import { Loader2 } from "lucide-react";

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

  // Get current user's email from localStorage
  const userEmail = localStorage.getItem("userEmail");

  // Load user's games when dialog opens
  useEffect(() => {
    if (open && userEmail) {
      const loadUserGames = async () => {
        setIsLoadingUserGames(true);
        try {
          const games = await getGamesByOwner(userEmail);
          setUserGames(games || []);
        } catch (error) {
          console.error("Failed to load user's games:", error);
          toast.error("Failed to load your games. Please try again.");
        } finally {
          setIsLoadingUserGames(false);
        }
      };
      
      loadUserGames();
    }
  }, [open, userEmail]);

  // Watch the game search term input to trigger filtering
  const watchedGameSearchTerm = watch("gameSearchTermInput");

  // Filter user's games based on search term
  useEffect(() => {
    // Clear results if search term is cleared
    if (!watchedGameSearchTerm) {
      setGameSearchResults([]);
      if (selectedGameId) setSelectedGameId(null);
      return;
    }

    // Don't search if a game is already selected by ID
    if (selectedGameId) {
      setGameSearchResults([]);
      return;
    }

    // Filter from already loaded user games instead of making API call
    const filterUserGames = () => {
      const searchTerm = watchedGameSearchTerm.toLowerCase();
      const filteredGames = userGames.filter(game => 
        game.name.toLowerCase().includes(searchTerm)
      );
      setGameSearchResults(filteredGames);
    };

    const debounceTimer = setTimeout(() => {
      if (watchedGameSearchTerm.length > 1 && !selectedGameId) {
        filterUserGames();
      } else {
        setGameSearchResults([]);
      }
    }, 300);

    return () => clearTimeout(debounceTimer);
  }, [watchedGameSearchTerm, selectedGameId, userGames]);

  const handleGameSelect = (game) => {
    setSelectedGameId(game.id);
    setValue("gameSearchTermInput", game.name);
    setGameSearchResults([]);
    setSubmitError("");
  };

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
      toast.success(`Successfully created event: ${result.title}`);
      if (onEventAdded) {
        onEventAdded();
      }
      handleCancel();
    } catch (error) {
      console.error("Create event error:", error);
      const errorMsg = error.message || "Failed to create event. Please try again.";
      setSubmitError(errorMsg);
      toast.error(errorMsg);
    } finally {
      setIsLoading(false);
    }
  });

  // Custom reset function to clear game search state as well
  const handleCancel = () => {
    reset();
    setSelectedGameId(null);
    setGameSearchResults([]);
    setSubmitError("");
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={(isOpen) => {
      if (!isOpen) handleCancel();
      else onOpenChange(true);
    }}>
      <DialogContent className="sm:max-w-[525px]">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Create New Event</DialogTitle>
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
                placeholder="Search your games..."
                {...register("gameSearchTermInput")}
                onChange={(e) => {
                  setValue("gameSearchTermInput", e.target.value);
                  if (selectedGameId) setSelectedGameId(null);
                }}
                autoComplete="off"
                className={!selectedGameId && submitError.includes("select") ? "border-red-500" : ""}
                disabled={isLoadingUserGames} // Disable while loading user games
              />
              {isSearchingGames && <Loader2 className="h-4 w-4 animate-spin" />}
            </div>
            
            {/* Display no games message if needed */}
            {userGames.length === 0 && !isLoadingUserGames && (
              <p className="text-sm text-yellow-600 mt-1">
                You don't have any games. Please add a game in your collection first.
              </p>
            )}
            
            {/* Display error if submit attempted without selection */}
            {submitError && submitError.includes("select") && (
              <p className="text-red-500 text-sm">{submitError}</p>
            )}

            {/* Search Results Dropdown - Now showing only user's games */}
            {gameSearchResults.length > 0 && !selectedGameId && (
              <ul className="absolute z-10 w-full bg-background border border-border rounded-md mt-1 max-h-40 overflow-y-auto shadow-lg">
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
              disabled={isLoading || isSearchingGames || isLoadingUserGames || userGames.length === 0}
            >
              {isLoading ? "Creating..." : "Create Event"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}