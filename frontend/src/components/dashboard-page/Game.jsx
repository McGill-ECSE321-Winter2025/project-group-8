import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge"; // Keep Badge import in case needed later
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Trash2 } from "lucide-react";
import { deleteGame } from "../../service/game-api.js"; // Keep single import
import { motion, AnimatePresence } from "framer-motion"; // Import Framer Motion

// Combined props: pass the whole game object and the refresh callback
export default function Game({ game, refreshGames }) {
  // State from HEAD for better feedback
  const [open, setOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false); // Loading state for deletion
  const [deleteError, setDeleteError] = useState(null); // Error state for deletion

  // Use game.id from the game prop
  const gameId = game?.id;
  const gameName = game?.name || "this game"; // Use game name or placeholder
  const imageSrc = game?.image || "https://upload.wikimedia.org/wikipedia/commons/1/14/No_Image_Available.jpg.svg?height=300&width=400"; // Use origin's placeholder

  // handleDelete logic from HEAD, adapted for game prop and refreshGames
  const handleDelete = async () => {
    if (!gameId) {
      console.error("Game ID is missing!");
      setDeleteError("Cannot delete game: ID missing.");
      return;
    }

    setIsDeleting(true);
    setDeleteError(null);

    try {
      await deleteGame(gameId);
      // Consider adding toast notification here if desired
      setOpen(false); // Close dialog on success
      if (refreshGames) {
        refreshGames(); // Refresh the list in the parent component
      }
    } catch (err) {
      console.error(`Failed to delete game "${gameName}" (ID: ${gameId}):`, err);
      setDeleteError(err.message || `Failed to delete ${gameName}. Please try again.`);
    } finally {
      setIsDeleting(false);
    }
  };

  // JSX structure using Framer Motion from origin/dev-Yessine-D3
  return (
    <AnimatePresence>
      {/* Render only if not actively being deleted via animation state */}
      {/* Note: isDeleting state now controls button, not the whole card visibility */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        exit={{ opacity: 0, y: -20, transition: { duration: 0.2 } }}
        key={gameId} // Unique key for animation
      >
        <Card className="min-w-[260px]">
          <CardContent className="p-0">
            <div className="aspect-[4/3] relative">
              <img
                src={imageSrc}
                alt={gameName}
                className="object-cover w-full h-full rounded-t-lg"
              />
            </div>
            <div className="p-4">
              {/* Simplified display like HEAD */}
              <h3 className="font-semibold text-lg">{gameName}</h3>
              <div className="flex justify-end items-center mt-4">
                {/* Dialog structure from HEAD */}
                <Dialog open={open} onOpenChange={setOpen}>
                  <DialogTrigger asChild>
                    <Button variant="outline" size="sm">
                      Remove
                    </Button>
                  </DialogTrigger>
                  <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                      <DialogTitle>Delete Game</DialogTitle>
                      <DialogDescription>
                        Are you sure you want to delete "{gameName}"? This action cannot be undone.
                        {/* Error display from HEAD */}
                        {deleteError && <p className="text-red-500 text-sm mt-2">{deleteError}</p>}
                      </DialogDescription>
                    </DialogHeader>
                    <DialogFooter className="mt-4">
                      <Button variant="outline" onClick={() => setOpen(false)} disabled={isDeleting}>
                        Cancel
                      </Button>
                      {/* Delete button with loading state from HEAD */}
                      <Button variant="destructive" onClick={handleDelete} disabled={isDeleting}>
                        {isDeleting ? (
                          <>Deleting...</>
                        ) : (
                          <>
                            <Trash2 className="mr-2 h-4 w-4" />
                            Delete
                          </>
                        )}
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              </div>
            </div>
          </CardContent>
        </Card>
      </motion.div>
    </AnimatePresence>
  );
}
