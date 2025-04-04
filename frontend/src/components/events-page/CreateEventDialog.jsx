import { useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "../../ui/dialog";
import { Button } from "../../ui/button";
import { Input } from "../../ui/input";
import { Textarea } from "../../ui/textarea";
import { Label } from "../../ui/label";
import { createEvent } from "../../service/event-api.js";

export default function CreateEventDialog({ open, onOpenChange }) {
  const [isLoading, setIsLoading] = useState(false);
  const [submitError, setSubmitError] = useState(""); 

  const { register, handleSubmit, formState: { errors }, reset } = useForm({
    defaultValues: {
      title: "",
      dateTime: "",
      location: "",
      description: "",
      maxParticipants: "",
      featuredGame: "",
      host: "d",
    },
  });

  const onSubmit = handleSubmit(async (data) => {
    console.log("Form data:", data); // This is for debugging
    setIsLoading(true);
    setSubmitError(""); 

    try {
      const result = await createEvent(data);
      toast.success(`Successfully created event: ${result.title}`);
      reset();
      onOpenChange(false);
    } catch (error) {
      setSubmitError("Failed to create event. Please try again."); 
      toast.error(error.message);
    } finally {
      setIsLoading(false);
    }
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[525px]">
        <DialogHeader>
          <DialogTitle className="text-xl font-bold">Create New Event</DialogTitle>
        </DialogHeader>

        <form onSubmit={onSubmit} className="space-y-4 py-4">
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
          {submitError && (
            <p className="text-red-500 text-sm text-center">{submitError}</p>
          )}

          <DialogFooter className="pt-4">
            <Button variant="outline" type="button" onClick={() => {
              setSubmitError(""); 
              onOpenChange(false);
            }}>
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
