import React, { useState } from 'react'; // Added useState
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '../ui/dialog';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '../ui/form';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Textarea } from '../ui/textarea'; // Added Textarea import
import { useForm } from "react-hook-form";
import { createBorrowRequest } from '../../service/borrow_request-api.js';
import { toast } from 'sonner';
import { useAuth } from '@/context/AuthContext'; // Added useAuth import

// Removed selectedInstance prop
export const RequestGameDialog = ({ open, onOpenChange, onSubmit, game }) => {
  const { user } = useAuth(); // Get user from AuthContext
  const [isSubmitting, setIsSubmitting] = useState(false); // State from origin

  const form = useForm({
    // Default values for fields from origin/dev-Yessine-D3
    defaultValues: {
      date: '',
      time: '',
      duration: '1', // Default duration, e.g., 1 hour
      players: game?.minPlayers || 1,
      message: '',
    }
  });

  // Get today's date in YYYY-MM-DD format
  const today = new Date().toISOString().split('T')[0];

  // handleSubmit logic adapted from origin/dev-Yessine-D3, using AuthContext
  const handleSubmit = async (data) => {
    if (!user?.id) {
       toast.error("You must be logged in to request a game.");
       return;
    }
    if (!game?.id) {
        toast.error("Game information is missing.");
        return;
    }

    try {
      setIsSubmitting(true);

      // Date/Time calculation from origin
      const startDateTime = new Date(`${data.date}T${data.time}`);
      const endDateTime = new Date(startDateTime);
      // Assuming duration is in hours
      endDateTime.setHours(endDateTime.getHours() + parseFloat(data.duration));

      // Validate calculated dates
      if (isNaN(startDateTime.getTime()) || isNaN(endDateTime.getTime())) {
        throw new Error("Invalid date or time input.");
      }
      if (endDateTime <= startDateTime) {
        throw new Error("End date/time must be after the start date/time.");
      }

      const requestData = {
        requesterId: user.id, // Use user ID from AuthContext
        requestedGameId: game.id,
        // Convert dates to ISO string or timestamp as expected by backend
        // Assuming backend expects ISO string based on other examples
        startDate: startDateTime.toISOString(),
        endDate: endDateTime.toISOString(),
        // Note: 'players' and 'message' from the form are not part of CreateBorrowRequestDto
        // They might be intended for display or other logic not shown here.
      };

      const response = await createBorrowRequest(requestData);
      console.log("Borrow request created:", response);

      // Call onSubmit prop if provided (optional)
      onSubmit?.({
        game,
        ...data, // Pass form data
        requestId: response.id,
        status: response.status
      });

      toast.success(`Request to borrow ${game?.name} was successfully sent! 🎉`);

      onOpenChange(false);
      form.reset();
    } catch (error) {
      console.error("Borrow request error:", error);
      // Error handling from origin
      let message = error.message || "Failed to submit borrow request.";
      if (message.toLowerCase().includes("own game")) {
        message = "You cannot borrow a game that you own.";
      } else if (message.toLowerCase().includes("not found")) {
        message = "The game or user could not be found.";
      } else if (message.toLowerCase().includes("400")) {
        message = "The request could not be processed. Please check the details and try again.";
      }
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Request to Borrow {game?.name}</DialogTitle>
          <DialogDescription>
            Fill out the form below to request to borrow this game.
          </DialogDescription>
        </DialogHeader>

        {/* Removed selectedInstance display from HEAD */}

        <Form {...form}>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
            {/* Form fields from origin/dev-Yessine-D3 */}
            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="date"
                rules={{
                  required: 'Date is required',
                   validate: (value) => new Date(value) >= new Date(today) || 'Date must be today or in the future'
                }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Date</FormLabel>
                    <FormControl>
                      <Input type="date" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="time"
                 rules={{ required: 'Time is required' }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Start Time</FormLabel>
                    <FormControl>
                      <Input type="time" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="duration"
                rules={{
                    required: 'Duration is required',
                    min: { value: 0.5, message: 'Duration must be at least 0.5 hours' }
                 }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Duration (hours)</FormLabel>
                    <FormControl>
                      <Input type="number" min="0.5" step="0.5" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="players"
                 rules={{
                    required: 'Number of players is required',
                    min: { value: game?.minPlayers || 1, message: `Min players: ${game?.minPlayers || 1}` },
                    max: { value: game?.maxPlayers || 10, message: `Max players: ${game?.maxPlayers || 10}` }
                 }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Number of Players</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        min={game?.minPlayers || 1}
                        max={game?.maxPlayers || 10}
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="message"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Message to Owner (Optional)</FormLabel>
                  <FormControl>
                    <Textarea
                      placeholder="Any special requests or information for the game owner"
                      rows={3}
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter className="gap-2 mt-4">
              {/* Submit button uses isSubmitting state from origin */}
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Submitting..." : "Submit Request"}
              </Button>
              <Button
                type="button"
                variant="outline"
                onClick={() => {
                  form.reset();
                  onOpenChange(false);
                }}
                disabled={isSubmitting}
              >
                Cancel
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
};
