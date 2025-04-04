import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Calendar, CheckCircle } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"

export default function LendingRecord({ id, name, requester, startDate, endDate, onMarkAsReturned }) {
  const [isModalOpen, setIsModalOpen] = useState(false)

  const handleConfirmReturn = () => {
    if (typeof onMarkAsReturned === "function") {
      onMarkAsReturned(id)
    }

    setIsModalOpen(false)
  }

  return (
    <>
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
                  <span className="font-medium">Borrowed by:</span> {requester}
                </div>
                <div className="text-sm">
                  <span className="font-medium">Borrow date:</span> {startDate}
                </div>
                <div className="text-sm">
                  <span className="font-medium">Due date:</span> {endDate}
                </div>
              </div>
              <div className="flex gap-2 mt-4">
                <Button variant="outline" size="sm" className="gap-2" onClick={() => setIsModalOpen(true)}>
                  <CheckCircle className="h-4 w-4" />
                  Mark as Returned
                </Button>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Confirmation Modal */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <CheckCircle className="h-5 w-5 text-green-500" />
              Confirm Return
            </DialogTitle>
            <DialogDescription>Are you sure you want to mark this item as returned?</DialogDescription>
          </DialogHeader>

          <div className="py-4">
            <div className="rounded-lg bg-muted p-4 text-sm">
              <p>
                <span className="font-medium">Game:</span> {name}
              </p>
              <p>
                <span className="font-medium">Borrowed by:</span> {requester}
              </p>
              <p>
                <span className="font-medium">Borrow date:</span> {startDate}
              </p>
              <p>
                <span className="font-medium">Due date:</span> {endDate}
              </p>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleConfirmReturn}>Mark as Returned</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}

