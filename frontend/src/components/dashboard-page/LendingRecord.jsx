import {Card, CardContent} from "@/components/ui/card.jsx";
import {Button} from "@/components/ui/button.jsx";

export default function LendingRecord({name, requester, startDate, endDate}) {
  return <Card>
    <CardContent className="p-4">
      <div className="flex flex-col md:flex-row gap-4">
        <div className="md:w-1/4">
          <div className="aspect-square bg-muted rounded-lg flex items-center justify-center">
          </div>
        </div>
        <div className="flex-1">
          <div className="flex justify-between">
            <h3 className="text-xl font-semibold">"{name}" has been lent</h3>
            <div className="px-4 py-1 mr-4 rounded-full text-xs bg-yellow-100 text-yellow-800">
              Pending
            </div>
          </div>
          <div className="grid gap-1 mt-2">
            <div className="text-sm">
              <span className="font-medium">Borrower:</span> {requester}
            </div>
            <div className="text-sm">
              <span className="font-medium">Lent on:</span> {startDate}
            </div>
            <div className="text-sm">
              <span className="font-medium">Return by:</span> {endDate}
            </div>
          </div>
          <div className="flex mt-4">
            <Button variant="positive" size="sm">
              Mark as returned
            </Button>
          </div>
        </div>
      </div>
    </CardContent>
  </Card>
}