import { useState } from "react";
import { Button } from "@/components/ui/button.jsx";
import CreateEventDialog from "../components/events-page/CreateEventDialog.jsx";

export default function EventsPage() {
  const [createDialogOpen, setCreateDialogOpen] = useState(false);

  return (
    <div className="bg-background text-foreground p-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-4xl font-bold text-primary">Upcoming Events</h1>
          <h2 className="text-xl text-muted-foreground mt-2">
            Join board game events in your area.
          </h2>
        </div>
        <Button 
          className="mt-6 font-bold" 
          onClick={() => setCreateDialogOpen(true)}
        >
          Create Event
        </Button>
      </div>

      <CreateEventDialog 
        open={createDialogOpen} 
        onOpenChange={setCreateDialogOpen} 
      />
    </div>
  );
}

