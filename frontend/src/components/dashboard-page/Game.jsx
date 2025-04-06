import { useState } from "react";
import { Button } from "@/components/ui/button";
import { deleteGame } from "@/service/game-api.js"; // Import the deleteGame API function
// import { toast } from 'react-toastify'; // Example for notifications
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Trash2 } from "lucide-react"

export default function Game({ id, name, imageSrc, refreshGames }) { // Use imageSrc, remove date, isAvailable
  const [open, setOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false); // Loading state for deletion
  const [deleteError, setDeleteError] = useState(null); // Error state for deletion

  const handleDelete = async () => {
    if (!id) {
      console.error("Game ID is missing!");
      // toast.error("Cannot delete game: ID missing.");
      setDeleteError("Cannot delete game: ID missing.");
      return;
    }

    setIsDeleting(true);
    setDeleteError(null);

    try {
      await deleteGame(id);
      // toast.success(`Game "${name}" deleted successfully!`);
      setOpen(false); // Close dialog on success
      if (refreshGames) {
        refreshGames(); // Refresh the list in the parent component
      }
    } catch (err) {
      console.error(`Failed to delete game "${name}":`, err);
      // toast.error(err.message || `Failed to delete game "${name}". Please try again.`);
      setDeleteError(err.message || `Failed to delete game. Please try again.`);
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <>
      <Card className="min-w-[260px]">
        <CardContent className="p-0">
          <div className="aspect-[4/3] relative">
            <img
              src={imageSrc || "/placeholder.svg?height=300&width=400"} // Use imageSrc prop
              alt={name || "Game cover"}
              className="object-cover w-full h-full rounded-t-lg"
            />
          </div>
          <div className="p-4">
            <h3 className="font-semibold text-lg">{name}</h3>
            {/* Removed date and availability status display */}
            <div className="flex justify-end items-center mt-4"> {/* Adjusted alignment after removing status */}
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
                      Are you sure you want to delete "{name}"? This action cannot be undone.
                      {deleteError && <p className="text-red-500 text-sm mt-2">{deleteError}</p>}
                    </DialogDescription>
                  </DialogHeader>
                  <DialogFooter className="mt-4">
                    <Button variant="outline" onClick={() => setOpen(false)} disabled={isDeleting}>
                      Cancel
                    </Button>
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
    </>
  )
}

