import { useState, useEffect } from "react"; // Import useEffect
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "../../ui/dialog";
import { Button } from "../../ui/button";
import { Input } from "../../ui/input";
import { Textarea } from "../../ui/textarea";
import { Label } from "../../ui/label";
import { createEvent } from "../../service/event-api.js";
// Import searchGames API function and Loader icon
import { searchGames } from "../../service/game-api.js";
import { Loader2 } from "lucide-react";

// Accept onEventAdded prop
export default function CreateEventDialog({ open, onOpenChange, onEventAdded }) {
  const [isLoading, setIsLoading] = useState(false);
  const [submitError, setSubmitError] = useState("");
  // State for game search
  const [gameSearchResults, setGameSearchResults] = useState([]);
  const [selectedGameId, setSelectedGameId] = useState(null);
  const [isSearchingGames, setIsSearchingGames] = useState(false);


  const { register, handleSubmit, formState: { errors }, reset, setValue, watch } = useForm({
    defaultValues: {
      title: "",
      dateTime: "",
      location: "",
      description: "",
      maxParticipants: "",
      // featuredGame removed from form default values, use gameSearchTermInput instead
      gameSearchTermInput: "",
    },
  });

  // Watch the game search term input to trigger API calls
  const watchedGameSearchTerm = watch("gameSearchTermInput");

  // Debounced effect for game search API call
  useEffect(() => {
    // Clear results if search term is cleared
    if (!watchedGameSearchTerm) {
      setGameSearchResults([]);
      // If user clears input AFTER selecting, clear the selection too
      if (selectedGameId) setSelectedGameId(null);
      return;
    }

    const fetchGames = async () => {
      // Don't search if a game is already selected by ID
      if (selectedGameId) {
          setGameSearchResults([]); // Clear results if a game is selected
          return;
      }
      setIsSearchingGames(true);
      try {
        const results = await searchGames({ name: watchedGameSearchTerm });
        setGameSearchResults(results || []); // Ensure results is always an array
      } catch (error) {
        console.error("Failed to search games:", error);
        setGameSearchResults([]); // Clear results on error
      } finally {
        setIsSearchingGames(false);
      }
    };

    const debounceTimer = setTimeout(() => {
       // Only search if term is long enough and no game is selected
       if (watchedGameSearchTerm.length > 1 && !selectedGameId) {
           fetchGames();
       } else {
           setGameSearchResults([]); // Clear results for short terms or if game selected
       }
    }, 500); // 500ms debounce

    return () => clearTimeout(debounceTimer);
  }, [watchedGameSearchTerm, selectedGameId]); // Depend on watched term and selection


  const handleGameSelect = (game) => {
    setSelectedGameId(game.id);
    setValue("gameSearchTermInput", game.name); // Update the input field to show selected game name
    setGameSearchResults([]); // Clear search results
    setSubmitError(""); // Clear potential previous submit error
  };


  const onSubmit = handleSubmit(async (data) => {
    // Manually check if a game was selected
    if (!selectedGameId) {
       setSubmitError("Please select a featured game from the search results.");
       return; // Prevent submission
    }

    // Remove the temporary search input value from the data to be submitted
    const { gameSearchTermInput, ...formData } = data;

    console.log("Form data (after removing search term):", formData);
    setIsLoading(true);
    setSubmitError("");

    // Add the selected game ID to the data payload
    const payload = {
      ...formData,
      featuredGameId: selectedGameId, // Pass the selected ID
    };
    console.log("Payload to send:", payload);

    try {
      const result = await createEvent(payload); // createEvent expects featuredGameId
      toast.success(`Successfully created event: ${result.title}`);
      if (onEventAdded) { // Call the refresh function passed from parent
        onEventAdded();
      }
      handleCancel(); // Use handleCancel to reset everything
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
      reset(); // Reset react-hook-form fields
      setSelectedGameId(null);
      setGameSearchResults([]);
      setSubmitError("");
      onOpenChange(false); // Close dialog
  };


  return (
    <Dialog open={open} onOpenChange={(isOpen) => {
        // Reset form and state if dialog is closed externally
        if (!isOpen) handleCancel();
        else onOpenChange(true);
    }}>
      <DialogContent className="sm:max-w-[525px]">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Create New Event</DialogTitle>
        </DialogHeader>

        {/* Use onSubmit from react-hook-form */}
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

          {/* Game Search Input and Results */}
          <div className="space-y-2 relative">
            <Label htmlFor="gameSearchTermInput">Featured Game <span className="text-red-500">*</span></Label>
            <div className="flex items-center gap-2">
                 <Input
                   id="gameSearchTermInput"
                   placeholder="Search for game..."
                   {...register("gameSearchTermInput")} // Register this input
                   // Clear selection if user types again after selecting
                   onChange={(e) => {
                       setValue("gameSearchTermInput", e.target.value); // Update form state
                       if (selectedGameId) setSelectedGameId(null); // Clear selection on type
                   }}
                   autoComplete="off" // Prevent browser autocomplete
                   className={!selectedGameId && submitError.includes("select a featured game") ? "border-red-500" : ""} // Error indication if game not selected on submit attempt
                 />
                 {isSearchingGames && <Loader2 className="h-4 w-4 animate-spin" />}
            </div>
             {/* Display error if submit attempted without selection */}
             {!selectedGameId && submitError.includes("select a featured game") && <p className="text-red-500 text-sm">{submitError}</p>}

            {/* Search Results Dropdown */}
            {gameSearchResults.length > 0 && !selectedGameId && ( // Only show results if no game is selected
              <ul className="absolute z-10 w-full bg-background border border-border rounded-md mt-1 max-h-40 overflow-y-auto shadow-lg">
                {gameSearchResults.map((game) => (
                  <li
                    key={game.id}
                    className="px-3 py-2 hover:bg-accent cursor-pointer text-sm"
                    onClick={() => handleGameSelect(game)}
                  >
                    {game.name} {/* Assuming backend returns name */}
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
                valueAsNumber: true, // Ensure value is treated as number
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
          {submitError && !submitError.includes("select a featured game") && (
            <p className="text-red-500 text-sm text-center">{submitError}</p>
          )}

          <DialogFooter className="pt-4">
            {/* Use custom cancel handler */}
            <Button variant="outline" type="button" onClick={handleCancel}>
              Cancel
            </Button>
            <Button type="submit" disabled={isLoading || isSearchingGames}>
              {isLoading ? "Creating..." : "Create Event"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
