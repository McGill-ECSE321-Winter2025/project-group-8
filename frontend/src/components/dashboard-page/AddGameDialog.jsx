 import { useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"; // Adjusted path
import { Button } from "@/components/ui/button"; // Adjusted path
import { Input } from "@/components/ui/input"; // Adjusted path
import { Label } from "@/components/ui/label"; // Adjusted path
import { createGame } from "../../service/game-api.js"; // Adjusted path

export default function AddGameDialog({ open, onOpenChange, onGameAdded }) {
  const [isLoading, setIsLoading] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const { register, handleSubmit, formState: { errors }, reset } = useForm({
    defaultValues: {
      name: "",
      minPlayers: "",
      maxPlayers: "",
      image: "",
      category: "",
    },
  });

  const onSubmit = handleSubmit(async (data) => {
    setIsLoading(true);
    setSubmitError("");

    try {
      // Ensure player counts are numbers
      const gameData = {
        ...data,
        minPlayers: parseInt(data.minPlayers, 10),
        maxPlayers: parseInt(data.maxPlayers, 10),
      };
      const result = await createGame(gameData);
      toast.success(`Successfully added game: ${result.name}`);
      reset(); // Reset form
      onOpenChange(false); // Close dialog
      if (onGameAdded) {
        onGameAdded(result); // Notify parent component if needed
      }
    } catch (error) {
      const errorMsg = "Failed to add game. Game name must be unique.";
      setSubmitError(errorMsg);
      toast.error(errorMsg);
    } finally {
      setIsLoading(false);
    }
  });

  // Custom reset function
  const handleCancel = () => {
      reset();
      setSubmitError("");
      onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={(isOpen) => {
        if (!isOpen) handleCancel();
        else onOpenChange(true);
    }}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Add New Game</DialogTitle>
        </DialogHeader>

        <form onSubmit={onSubmit} className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="name">Game Name <span className="text-red-500">*</span></Label>
            <Input
              id="name"
              {...register("name", { required: "Game name is required" })}
              className={errors.name ? "border-red-500" : ""}
            />
            {errors.name && <p className="text-red-500 text-sm">{errors.name.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="minPlayers">Min Players <span className="text-red-500">*</span></Label>
              <Input
                id="minPlayers"
                type="number"
                min="1"
                {...register("minPlayers", {
                  required: "Min players is required",
                  valueAsNumber: true,
                  min: { value: 1, message: "Must be at least 1" }
                })}
                className={errors.minPlayers ? "border-red-500" : ""}
              />
              {errors.minPlayers && <p className="text-red-500 text-sm">{errors.minPlayers.message}</p>}
            </div>
            <div className="space-y-2">
              <Label htmlFor="maxPlayers">Max Players <span className="text-red-500">*</span></Label>
              <Input
                id="maxPlayers"
                type="number"
                min="1"
                {...register("maxPlayers", {
                  required: "Max players is required",
                  valueAsNumber: true,
                  validate: (value, { minPlayers }) => parseInt(value, 10) >= parseInt(minPlayers, 10) || "Max players must be >= min players"
                })}
                className={errors.maxPlayers ? "border-red-500" : ""}
              />
              {errors.maxPlayers && <p className="text-red-500 text-sm">{errors.maxPlayers.message}</p>}
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="category">Category</Label>
            <Input id="category" {...register("category")} placeholder="e.g., Strategy, Party, Family" />
          </div>

          <div className="space-y-2">
            <Label htmlFor="image">Image URL</Label>
            <Input id="image" {...register("image")} placeholder="https://example.com/image.png" />
          </div>

          {submitError && (
            <p className="text-red-500 text-sm text-center">{submitError}</p>
          )}

          <DialogFooter className="pt-4">
            <Button variant="outline" type="button" onClick={handleCancel}>
              Cancel
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? "Adding..." : "Add Game"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
