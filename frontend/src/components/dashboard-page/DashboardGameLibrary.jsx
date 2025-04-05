import { useState, useEffect, useCallback } from "react"; // Import useState, useEffect, useCallback
import { useAuth } from "@/context/AuthContext"; // Import useAuth
import { Button } from "@/components/ui/button.jsx";
import Game from "./Game.jsx";
import { TabsContent } from "@/components/ui/tabs.jsx";
import AddGameDialog from "./AddGameDialog.jsx"; // Import the dialog
import { Loader2 } from "lucide-react"; // Import Loader icon
import { getGamesByOwner } from "../../service/game-api.js"; // Import the service function

export default function DashboardGameLibrary({ userType }) {
  const { user } = useAuth(); // Get user from context
  const [isAddGameDialogOpen, setIsAddGameDialogOpen] = useState(false);
  const [games, setGames] = useState([]); // State for fetched games
  const [isLoading, setIsLoading] = useState(true); // Keep loading state for fetch operation
  const [error, setError] = useState(null);

  // Function to fetch games
  // Use useCallback to memoize fetchGames, prevent re-creation if user object reference changes unnecessarily
  const fetchGames = useCallback(async () => {
    if (!user?.email) { // Check if user and user.email exist
      setError("User email not found. Cannot fetch games.");
      setIsLoading(false);
      setGames([]);
      return;
    }
    
    const ownerEmail = user.email; // Get email from context user object
    
    setIsLoading(true);
    setError(null);

    try {
      const fetchedGames = await getGamesByOwner(ownerEmail);
      setGames(fetchedGames || []); // Ensure games is always an array
    } catch (err) {
      console.error("Failed to fetch games:", err);
      setError(err.message || "Could not load your games.");
      setGames([]); // Clear games on error
    } finally {
      setIsLoading(false);
    }
  }, [user?.email]); // Add closing parenthesis and dependency array for useCallback

  // Fetch games when the component mounts or when the user object changes (specifically the email)
  useEffect(() => {
    // Only fetch if the user is identified as an owner and their email is available
    if (userType === "owner" && user?.email) {
       fetchGames();
    } else if (userType !== "owner") {
        // If not an owner, explicitly set loading to false and games to empty
        setIsLoading(false);
        setGames([]);
    }
    // Add fetchGames to dependency array as it's now memoized with useCallback
  }, [userType, user?.email, fetchGames]);

  // Function to handle adding a game (refreshes the list)
  const handleGameAdded = (newGame) => {

    // Re-fetch the list to include the new game
    fetchGames();
  };

  return (
    <>
      <TabsContent value="games" className="space-y-6">
        <div className="flex justify-between items-center">
          <h2 className="text-2xl font-bold">My Games</h2>
          {/* Only show button if userType is owner */}
          {userType === "owner" && (
            <Button onClick={() => setIsAddGameDialogOpen(true)}>Add New Game</Button>
          )}
        </div>

        {/* Conditional Rendering based on loading/error/data */}
        {isLoading ? (
          <div className="flex justify-center items-center py-10">
            <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
          </div>
        ) : error ? (
          <div className="text-center py-10 text-destructive">
            <p>Error loading games: {error}</p>
          </div>
        ) : games.length === 0 && userType === "owner" ? (
           <div className="text-center py-10 text-muted-foreground">
             You haven't added any games yet. Click "Add New Game" to start!
           </div>
        ) : games.length === 0 && userType !== "owner" ? (
            <div className="text-center py-10 text-muted-foreground">
              Game library is only available for Game Owners.
            </div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {games.map(game => (
              // Adapt game data for the Game component if necessary
              // Assuming Game component expects `imageSrc` and potentially other props
              <Game
                key={game.id}
                id={game.id}
                name={game.name}
                imageSrc={game.image || "/placeholder.svg?height=300&width=400"} // Use game image or placeholder
                // Pass other relevant props if Game component needs them
                // date={game.dateAdded ? new Date(game.dateAdded).toLocaleDateString() : 'N/A'}
                // isAvailable={true} // Backend doesn't seem to track availability directly on Game model yet
              />
            ))}
          </div>
        )}
      </TabsContent>

      {/* Render the Add Game Dialog */}
      <AddGameDialog
        open={isAddGameDialogOpen}
        onOpenChange={setIsAddGameDialogOpen}
        onGameAdded={handleGameAdded}
      />
    </>
  );
}
