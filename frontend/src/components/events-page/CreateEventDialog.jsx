import { useState } from "react";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "../../ui/dialog";
import { Button } from "../../ui/button";
import { Input } from "../../ui/input";
import { Textarea } from "../../ui/textarea";
import { Label } from "../../ui/label";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

export default function CreateEventDialog({ open, onOpenChange }) {
  const [isLoading, setIsLoading] = useState(false);
  
  const { register, handleSubmit, formState: { errors }, reset } = useForm({
    defaultValues: {
      title: "",
      dateTime: "",
      location: "",
      description: "",
      maxParticipants: "",
      featuredGame: "",
      host: "" 
    }
  });
  
  const onSubmit = async (data) => {
    console.log("Form submitted with data:");
    setIsLoading(true);
    
    try {
      const formattedData = {
        ...data,
        dateTime: new Date(data.dateTime).toISOString(),
        maxParticipants: parseInt(data.maxParticipants)
      };
      
    const response = await fetch('http://localhost:8080/api/events', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(formattedData),
    });
      
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to create event');
      }
      
      const result = await response.json();
      
      toast.success(`Successfully created event: ${result.title}`);
      
      reset();
      onOpenChange(false);
    } catch (error) {
      toast.error(error.message);
    } finally {
      setIsLoading(false);
    }
  };
  
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[525px]">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Create New Event</DialogTitle>
        </DialogHeader>
        
        <form onSubmit={(e) => { 
  console.log("Submitting form...");
  handleSubmit(onSubmit)(e);
}} className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="title">Event Title <span className="text-red-500">*</span></Label>
            <Input 
              id="title"
              {...register("title", { required: "Title is required" })}
              className={errors.title ? "border-red-500" : ""}
            />
            {errors.title && <p className="text-red-500 text-sm">{errors.title.message}</p>}
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="dateTime">Date and Time <span className="text-red-500">*</span></Label>
            <Input 
              id="dateTime"
              type="datetime-local"
              {...register("dateTime", { required: "Date and time is required" })}
              className={errors.dateTime ? "border-red-500" : ""}
            />
            {errors.dateTime && <p className="text-red-500 text-sm">{errors.dateTime.message}</p>}
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="location">Location</Label>
            <Input id="location" {...register("location")} />
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="featuredGame">Featured Game <span className="text-red-500">*</span></Label>
            <Input 
              id="featuredGame"
              {...register("featuredGame", { required: "Featured game is required" })}
              className={errors.featuredGame ? "border-red-500" : ""}
            />
            {errors.featuredGame && <p className="text-red-500 text-sm">{errors.featuredGame.message}</p>}
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="maxParticipants">Maximum Participants <span className="text-red-500">*</span></Label>
            <Input 
              id="maxParticipants"
              type="number"
              min="1"
              {...register("maxParticipants", { 
                required: "Maximum participants is required",
                min: { value: 1, message: "Must be greater than 0" }
              })}
              className={errors.maxParticipants ? "border-red-500" : ""}
            />
            {errors.maxParticipants && <p className="text-red-500 text-sm">{errors.maxParticipants.message}</p>}
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="description">Description</Label>
            <Textarea 
              id="description"
              {...register("description")}
              className="min-h-[100px]"
            />
          </div>
          
          <input type="hidden" {...register("host", { required: true })} value="current-user-id" />
          
          <DialogFooter className="pt-4">
            <Button variant="outline" type="button" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isLoading}>
              {isLoading ? "Creating..." : "Create Event"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}