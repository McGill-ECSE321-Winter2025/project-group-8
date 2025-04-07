import React, { useState } from 'react';
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
import { Textarea } from '../ui/textarea';
import { useForm } from "react-hook-form";
import { createBorrowRequest } from '../../service/borrow_request-api.js';
import { toast } from 'sonner';
import { useAuth } from '@/context/AuthContext';

export const RequestGameDialog = ({ open, onOpenChange, onSubmit, game, gameInstance }) => {
  const { user } = useAuth();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const form = useForm({
    defaultValues: {
      date: '',
      time: '',
      duration: '1',
      players: game?.minPlayers || 1,
      message: '',
    }
  });

  // Get today's date in YYYY-MM-DD format
  const today = new Date().toISOString().split('T')[0];

  const handleSubmit = async (data) => {
    if (!user?.id) {
       toast.error("You must be logged in to request a game.");
       return;
    }
    if (!game?.id) {
        toast.error("Game information is missing.");
        return;
    }
    if (!gameInstance?.id) {
        toast.error("Please select a specific game copy to borrow.");
        return;
    }

    try {
      setIsSubmitting(true);

      const startDateTime = new Date(`${data.date}T${data.time}`);
      const endDateTime = new Date(startDateTime);
      endDateTime.setHours(endDateTime.getHours() + parseFloat(data.duration));

      if (isNaN(startDateTime.getTime()) || isNaN(endDateTime.getTime())) {
        throw new Error("Invalid date or time input.");
      }
      if (endDateTime <= startDateTime) {
        throw new Error("End date/time must be after the start date/time.");
      }

      const requestData = {
        requesterId: user.id,
        requestedGameId: game.id,
        gameInstanceId: gameInstance.id,
        startDate: startDateTime.toISOString(),
        endDate: endDateTime.toISOString(),
        message: data.message,
      };

      const response = await createBorrowRequest(requestData);
      console.log("Borrow request created:", response);

      onSubmit?.({
        game,
        gameInstance,
        ...data,
        requestId: response.id,
        status: response.status
      });

      toast.success(`Request to borrow ${game?.name} was successfully sent! 🎉`);

      onOpenChange(false);
      form.reset();
    } catch (error) {
      console.error("Borrow request error:", error);
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

        {gameInstance && (
          <div className="bg-muted/50 p-3 rounded-md mb-4">
            <p className="font-medium text-sm">Selected Copy:</p>
            <p className="text-sm">Owner: {gameInstance.owner?.name || 'Unknown'}</p>
            {gameInstance.condition && (
              <p className="text-sm">Condition: {gameInstance.condition}</p>
            )}
          </div>
        )}

        <Form {...form}>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
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
              <Button type="submit" disabled={isSubmitting || !gameInstance}>
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
