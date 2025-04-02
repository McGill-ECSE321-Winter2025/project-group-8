import {Button} from "@/components/ui/button.jsx";

export default function BorrowRequest({ name, requester, date, endDate }) {
  return <div className="flex flex-col md:flex-row gap-4">
    <div className="md:w-1/4">
      <div className="aspect-square bg-muted rounded-lg flex items-center justify-center">
      </div>
    </div>
    <div className="flex-1">
      <div className="flex justify-between">
        <h3 className="text-xl font-semibold">Request for "{name}"</h3>
        <div className="px-2 py-1 rounded-full text-xs bg-yellow-100 text-yellow-800">
          Pending
        </div>
      </div>
      <div className="grid gap-1 mt-2">
        <div className="text-sm">
          <span className="font-medium">From:</span> {requester}
        </div>
        <div className="text-sm">
          <span className="font-medium">Requested on:</span> {date}
        </div>
        <div className="text-sm">
          <span className="font-medium">Duration:</span> {endDate}
        </div>
      </div>
      <div className="flex gap-2 mt-4">
        <Button size="sm">Accept</Button>
        <Button variant="outline" size="sm">
          Decline
        </Button>
      </div>
    </div>
  </div>
}