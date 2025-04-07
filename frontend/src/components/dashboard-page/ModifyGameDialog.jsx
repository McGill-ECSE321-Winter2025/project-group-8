import { useState } from "react";
import {
  Dialog,
  DialogTrigger,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { updateGame } from "../../service/game-api";

export default function ModifyGameDialog({ game, onUpdateSuccess }) {
  const [name, setName] = useState(game.name || "");
  const [minPlayers, setMinPlayers] = useState(game.minPlayers || "");
  const [maxPlayers, setMaxPlayers] = useState(game.maxPlayers || "");
  const [category, setCategory] = useState(game.category || "");
  const [image, setImage] = useState(game.image || "");
  const [open, setOpen] = useState(false);

  const handleUpdate = async () => {
    const ownerId = localStorage.getItem("userEmail");

    const gameDto = {
      name,
      minPlayers: parseInt(minPlayers),
      maxPlayers: parseInt(maxPlayers),
      category,
      image,
      ownerId,
    };

    const updated = await updateGame(game.id, gameDto);

    if (!updated) {
      alert("Update failed.");
      return;
    }

    if (onUpdateSuccess) {
      onUpdateSuccess({ ...game, ...gameDto });
    }

    setOpen(false);
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button
          variant="outline"
          className="w-full mt-2 border border-red-300 text-red-600 hover:bg-red-100"
        >
          Modify Game
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Modify Game</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Game Name *</label>
            <Input value={name} onChange={(e) => setName(e.target.value)} placeholder="Game Name *" />
          </div>

          <div className="flex gap-2">
            <div className="w-full">
              <label className="block text-sm font-medium text-gray-700 mb-1">Min Players *</label>
              <Input value={minPlayers} onChange={(e) => setMinPlayers(e.target.value)} placeholder="Min Players *" />
            </div>
            <div className="w-full">
              <label className="block text-sm font-medium text-gray-700 mb-1">Max Players *</label>
              <Input value={maxPlayers} onChange={(e) => setMaxPlayers(e.target.value)} placeholder="Max Players *" />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
            <Input value={category} onChange={(e) => setCategory(e.target.value)} placeholder="e.g., Strategy, Party, Family" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Image URL</label>
            <Input value={image} onChange={(e) => setImage(e.target.value)} placeholder="https://example.com/image.png" />
          </div>
        </div>
        <DialogFooter>
          <Button onClick={handleUpdate}>Save</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
