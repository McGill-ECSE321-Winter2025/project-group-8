import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
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
import { deleteGame } from "../../service/game-api.js";
import { motion, AnimatePresence } from "framer-motion"; // Import Framer Motion

export default function Game({ name, date, isAvailable, imageSrc, game, onDeleteSuccess }) {
  const [open, setOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false); // Add a state to track deletion

  const handleDelete = async () => {
    setIsDeleting(true); // Start the deletion animation
    const success = await deleteGame(game.id);
    if (success) {
      setOpen(false);
      if (onDeleteSuccess) {
        onDeleteSuccess(game.id);
      }
    } else {
      setIsDeleting(false); // Reset the state if deletion fails
      // Handle error (e.g., show an error message to the user)
    }
  };

  return (
    <AnimatePresence>
      {!isDeleting && ( // Only render if not deleting
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -20, transition: { duration: 0.2 } }} // Add exit animation
          key={game.id} // Add a unique key for the animation
        >
          <Card className="min-w-[260px]">
            <CardContent className="p-0">
              <div className="aspect-[4/3] relative">
                <img
                  src={imageSrc}
                  alt={name || "Game cover"}
                  className="object-cover w-full h-full rounded-t-lg"
                />
              </div>
              <div className="p-4">
                <h3 className="font-semibold text-lg">{name}</h3>
                <p className="text-sm text-muted-foreground">Added on {date}</p>
                <div className="flex justify-between items-center mt-4">
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-medium">Status:</span>
                    <Badge
                      variant="outline"
                      className={
                        isAvailable
                          ? "bg-green-600 text-white hover:bg-green-700"
                          : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                      }
                    >
                      {isAvailable ? "Available" : "Unavailable"}
                    </Badge>
                  </div>
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
                          Are you sure you want to delete "{name}"? This action
                          cannot be undone.
                        </DialogDescription>
                      </DialogHeader>
                      <DialogFooter className="mt-4">
                        <Button variant="outline" onClick={() => setOpen(false)}>
                          Cancel
                        </Button>
                        <Button variant="destructive" onClick={handleDelete}>
                          <Trash2 className="mr-2 h-4 w-4" />
                          Delete
                        </Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>
                </div>
              </div>
            </CardContent>
          </Card>
        </motion.div>
      )}
    </AnimatePresence>
  );
}