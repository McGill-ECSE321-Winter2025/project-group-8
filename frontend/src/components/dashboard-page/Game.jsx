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
import ModifyGameDialog from "./ModifyGameDialog.jsx";

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
      {!isDeleting && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -20, transition: { duration: 0.2 } }}
          key={game.id}
          className="mb-4"
        >
          <Card>
            <CardContent className="flex flex-col items-start p-4 space-y-2">
              <img src={imageSrc} alt={name} className="w-32 h-32 object-cover rounded" />
              <h3 className="text-lg font-semibold">{name}</h3>
              <Badge variant={isAvailable ? "success" : "destructive"}>
                {isAvailable ? "Available" : "Unavailable"}
              </Badge>

              <div className="w-full flex flex-col gap-2">
              <ModifyGameDialog
  game={game}
  onUpdateSuccess={(updatedGame) => {
    Object.assign(game, updatedGame); // update the object in place
    if (onDeleteSuccess) onDeleteSuccess(game.id); // optional rerender or effect
  }}
/>

                <Dialog open={open} onOpenChange={setOpen}>
                  <DialogTrigger asChild>
                    <Button className="w-full" variant="destructive">
                      <Trash2 className="mr-2 h-4 w-4" />
                      Delete
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>Delete Game</DialogTitle>
                      <DialogDescription>
                        Are you sure you want to delete <strong>{name}</strong>?
                      </DialogDescription>
                    </DialogHeader>
                    <DialogFooter>
                      <Button variant="outline" onClick={() => setOpen(false)}>
                        Cancel
                      </Button>
                      <Button variant="destructive" onClick={handleDelete}>
                        Confirm
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              </div>
            </CardContent>
          </Card>
        </motion.div>
      )}
    </AnimatePresence>
  );
}
