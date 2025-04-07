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

export const RequestGameDialog = ({ open, onOpenChange, onSubmit, game }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);

  const form = useForm({
    defaultValues: {
      date: '',
      time: '',
      duration: '2',
      players: game?.minPlayers || '2',
      message: '',
    }
  });

  const handleSubmit = async (data) => {
    try {
      setIsSubmitting(true);

      const startDateTime = new Date(`${data.date}T${data.time}`);
      const endDateTime = new Date(startDateTime);
      endDateTime.setHours(endDateTime.getHours() + parseFloat(data.duration));

      const getCurrentUserId = () => {
        const id = localStorage.getItem("userId");
        if (!id) throw new Error("User not logged in.");
        return parseInt(id);
      };

      const requestData = {
        requesterId: getCurrentUserId(),
        requestedGameId: game?.id,
        startDate: startDateTime.getTime(),
        endDate: endDateTime.getTime()
      };

      const response = await createBorrowRequest(requestData);
      console.log("Borrow request created:", response);

      onSubmit?.({
        game,
        ...data,
        requestId: response.id,
        status: response.status
      });

      toast.success(`Request to play ${game?.name} was successfully sent! 🎉`);

      onOpenChange(false);
      form.reset();
    } catch (error) {
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
          <DialogTitle>Request to Play {game?.name}</DialogTitle>
          <DialogDescription>
            Fill out the form below to request this game for your next event
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="date"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Date</FormLabel>
                    <FormControl>
                      <Input type="date" required {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="time"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Time</FormLabel>
                    <FormControl>
                      <Input type="time" required {...field} />
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
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Duration (hours)</FormLabel>
                    <FormControl>
                      <Input type="number" min="0.5" step="0.5" required {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="players"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Number of Players</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        min={game?.minPlayers || 1}
                        max={game?.maxPlayers || 10}
                        required
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
                  <FormLabel>Message to Owner</FormLabel>
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
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Submitting..." : "Create Borrow Request"}
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
