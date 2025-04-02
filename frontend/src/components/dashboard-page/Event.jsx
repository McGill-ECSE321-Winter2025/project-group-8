import {Card, CardContent} from "@/components/ui/card.jsx";
import {Calendar} from "lucide-react";
import {Button} from "@/components/ui/button.jsx";

export default function Event( { name, date, time, location, game, participants: {current, capacity} } ) {
  return (
    <Card>
      <CardContent className="p-4">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="md:w-1/4">
            <div className="aspect-square bg-muted rounded-lg flex items-center justify-center">
              <Calendar className="h-12 w-12 text-muted-foreground" />
            </div>
          </div>
          <div className="flex-1">
            <h3 className="text-xl font-semibold">{name}</h3>
            <div className="grid gap-1 mt-2">
              <div className="text-sm">
                <span className="font-medium">Date:</span> {date}
              </div>
              <div className="text-sm">
                <span className="font-medium">Time:</span> {time}
              </div>
              <div className="text-sm">
                <span className="font-medium">Location:</span> {location}
              </div>
              <div className="text-sm">
                <span className="font-medium">Game:</span> {game}
              </div>
              <div className="text-sm">
                <span className="font-medium">Participants:</span> {current}/{capacity}
              </div>
            </div>
            <div className="flex gap-2 mt-4">
              <Button variant="outline" size="sm">
                View Details
              </Button>
              <Button variant="outline" size="sm">
                Cancel Registration
              </Button>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}