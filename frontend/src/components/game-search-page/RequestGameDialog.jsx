import React from 'react';
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
import { useForm } from "react-hook-form";

export const RequestGameDialog = ({ open, onOpenChange, onSubmit, game, selectedInstance }) => {
  const form = useForm({
    defaultValues: {
      startDate: '',
      endDate: '',
    }
  });
  
  // Get today's date in YYYY-MM-DD format
  const today = new Date().toISOString().split('T')[0];

  const handleSubmit = (data) => {
    // Validation is now handled by react-hook-form rules
    // Convert form data to match CreateBorrowRequestDto
    const borrowRequest = {
      game,
      instance: selectedInstance,
      startDate: data.startDate,
      endDate: data.endDate,
      // These fields would be set by the backend:
      // requesterId (from authentication)
      // requestedGameId (from selected game)
    };
    
    onSubmit(borrowRequest);
    onOpenChange(false);
    form.reset();
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Request to Borrow {game?.name}</DialogTitle>
          <DialogDescription>
            Fill out the form below to request to borrow this game
          </DialogDescription>
        </DialogHeader>
        
        {selectedInstance && (
          <div className="bg-muted/50 p-3 rounded-md mb-4">
            <p className="text-sm font-medium">Selected Copy:</p>
            <p className="text-sm">Owned by {selectedInstance.owner.name}</p>
          </div>
        )}
        
        <Form {...form}>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="startDate"
                rules={{
                  required: 'Start date is required',
                  validate: (value) => new Date(value) >= new Date(today) || 'Start date must be today or in the future'
                }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Start Date</FormLabel>
                    <FormControl>
                      <Input
                        type="date"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              
              <FormField
                control={form.control}
                name="endDate"
                rules={{
                  required: 'End date is required',
                  validate: (value) => {
                    const startDate = form.getValues("startDate");
                    return !startDate || new Date(value) >= new Date(startDate) || 'End date must be on or after the start date';
                  }
                }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>End Date</FormLabel>
                    <FormControl>
                      <Input
                        type="date"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            
            <DialogFooter className="gap-2 mt-4">
              <Button type="submit" disabled={!selectedInstance}>
                {!selectedInstance ? "Select a copy first" : "Submit Request"}
              </Button>
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