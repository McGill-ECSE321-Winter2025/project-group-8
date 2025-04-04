import {TabsContent} from "@/components/ui/tabs.jsx";
import {Button} from "@/components/ui/button.jsx";
import Event from "./Event.jsx";

export default function DashboardEvents() {

  const dummyEvents = [
    {
      id: 1,
      name: "Friday Night Strategy Games",
      date: "2025-03-15",
      time: "7:00 PM - 10:00 PM",
      location: "Board Game Cafe, 123 Main St",
      game: "Settlers of Catan",
      participants: {
        current: 4,
        capacity: 6,
      },
    },
    {
      id: 2,
      name: "Weekend Board Game Marathon",
      date: "2025-03-22",
      time: "1:00 PM - 8:00 PM",
      location: "Community Center, 456 Oak Ave",
      game: "Multiple Games",
      participants: {
        current: 12,
        capacity: 20,
      },
    },
  ];

  return <TabsContent value="events" className="space-y-6">
    <div className="flex justify-between items-center">
      <h2 className="text-2xl font-bold">My Events</h2>
      <Button>Create Event</Button>
    </div>
    <div className="space-y-4">
      { dummyEvents.map(event => <Event key={event.id} {...event} />)}
    </div>
  </TabsContent>
}