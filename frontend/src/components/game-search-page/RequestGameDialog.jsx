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

export const RequestGameDialog = ({ open, onOpenChange, onSubmit, game }) => {
  const form = useForm({
    defaultValues: {
      date: '',
      time: '',
      duration: '2',
      players: game?.minPlayers || '2',
      message: '',
    }
  });

  const handleSubmit = (data) => {
    onSubmit({
      game,
      ...data
    });
    onOpenChange(false);
    form.reset();
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
              <Button type="submit">Submit Request</Button>
              <Button 
                type="button" 
                variant="outline" 
                onClick={() => {
                  form.reset();
                  onOpenChange(false);
                }}
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