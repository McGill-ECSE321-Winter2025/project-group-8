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
      
      // Format the date and time into a proper ISO string for startDate
      const startDateTime = new Date(`${data.date}T${data.time}`);
      
      // Calculate endDate by adding duration hours to startDate
      const endDateTime = new Date(startDateTime);
      endDateTime.setHours(endDateTime.getHours() + parseFloat(data.duration));

      const getCurrentUserId = () => {
        return parseInt(localStorage.getItem("userId")); // Or from context/state
      };
      
      // Prepare request data for the API
      const requestData = {
        requesterId: getCurrentUserId(), // You need to implement this
        requestedGameId: game?.id,
        startDate: startDateTime.toISOString(),
        endDate: endDateTime.toISOString()
      };
      
      // Call the API to create the borrow request
      const response = await createBorrowRequest(requestData);
      
      // Call the parent component's onSubmit with both form data and API response
      onSubmit({
        game,
        ...data,
        requestId: response.id,
        status: response.status
      });
      
      // Show success toast
      toast({
        title: "Request Submitted",
        description: `Your request to play ${game?.name} has been sent.`,
      });
      
      // Close dialog and reset form
      onOpenChange(false);
      form.reset();
    } catch (error) {
      // Show error toast
      toast({
        title: "Request Failed",
        description: error.message || "Failed to submit game request. Please try again.",
        variant: "destructive",
      });
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
                      <Input
                        type="date"
                        required
                        {...field}
                      />
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
                      <Input
                        type="time"
                        required
                        {...field}
                      />
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
                      <Input
                        type="number"
                        min="0.5"
                        step="0.5"
                        required
                        {...field}
                      />
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