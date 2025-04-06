import { useState, useEffect } from "react"; // Import useState and useEffect
import { Button } from "@/components/ui/button.jsx";
import Game from "./Game.jsx";
import { TabsContent } from "@/components/ui/tabs.jsx";
import AddGameDialog from "./AddGameDialog.jsx"; // Import the dialog
import { Loader2 } from "lucide-react"; // Import Loader icon
import { getGamesByOwner } from "../../service/game-api.js"; // Import the service function

export default function DashboardGameLibrary({ userType }) {
  const [isAddGameDialogOpen, setIsAddGameDialogOpen] = useState(false);
  const [games, setGames] = useState([]); // State for fetched games
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Function to fetch games
  const fetchGames = async () => {
    setIsLoading(true);
    setError(null);
    const ownerEmail = localStorage.getItem("userEmail");
    if (!ownerEmail) {
      setError("User email not found. Please log in again.");
      setIsLoading(false);
      setGames([]); // Clear games if email is missing
      return;
    }

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
  };

  // Fetch games on component mount
  useEffect(() => {
    // Only fetch if the user is an owner (or adjust logic if players should see their borrowed games here)
    if (userType === "owner") {
       fetchGames();
    } else {
        setIsLoading(false); // Not loading if not an owner
        setGames([]); // Ensure games is empty for non-owners in this view
    }
  }, [userType]); // Re-fetch if userType changes (though unlikely)

  // Function to handle adding a game (refreshes the list)
  const handleGameAdded = (newGame) => {
    console.log("New game added:", newGame);
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
