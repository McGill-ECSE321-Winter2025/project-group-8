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

  const validateDates = (data) => {
    // Create error object to store validation errors
    const errors = {};
    
    // Validate start date is provided
    if (!data.startDate) {
      errors.startDate = {
        type: "required",
        message: "Start date is required"
      };
    } 
    // Validate start date is not in the past
    else if (new Date(data.startDate) < new Date(today)) {
      errors.startDate = {
        type: "min",
        message: "Start date must be today or in the future"
      };
    }
    
    // Validate end date is provided
    if (!data.endDate) {
      errors.endDate = {
        type: "required",
        message: "End date is required"
      };
    } 
    // Validate end date is not before start date
    else if (data.startDate && new Date(data.endDate) < new Date(data.startDate)) {
      errors.endDate = {
        type: "min",
        message: "End date must be on or after the start date"
      };
    }
    
    return errors;
  };

  const handleSubmit = (data) => {
    // Perform manual validation
    const errors = validateDates(data);
    
    // If there are validation errors, set them in the form
    if (Object.keys(errors).length > 0) {
      Object.keys(errors).forEach(field => {
        form.setError(field, errors[field]);
      });
      return;
    }
    
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
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Start Date</FormLabel>
                    <FormControl>
                      <Input
                        type="date"
                        min={today}
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
                name="endDate"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>End Date</FormLabel>
                    <FormControl>
                      <Input
                        type="date"
                        min={form.watch("startDate") || today}
                        required
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