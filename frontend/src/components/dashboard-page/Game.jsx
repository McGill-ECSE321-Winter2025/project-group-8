import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Trash2 } from "lucide-react"

export default function Game({ name, date, isAvailable }) {
  const [open, setOpen] = useState(false)

  const handleDelete = () => {
    // Add your delete logic here
    console.log(`Deleting game: ${name}`)
    setOpen(false)
  }

  return (
    <>
      <Card>
        <CardContent className="p-0">
          <div className="aspect-[4/3] relative">
            <img
              src="/placeholder.svg?height=300&width=400"
              alt={name || "Game cover"}
              className="object-cover w-full h-full rounded-t-lg"
            />
          </div>
          <div className="p-4">
            <h3 className="font-semibold text-lg">{name}</h3>
            <p className="text-sm text-muted-foreground">Added on {date}</p>
            <div className="flex justify-between items-center mt-4">
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium">Status:</span>
                <Badge
                  variant="outline"
                  className={
                    isAvailable
                      ? "bg-green-500 text-white hover:bg-green-600"
                      : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                  }
                >
                  {isAvailable ? "Available" : "Unavailable"}
                </Badge>
              </div>
              <Dialog open={open} onOpenChange={setOpen}>
                <DialogTrigger asChild>
                  <Button variant="outline" size="sm">
                    Manage
                  </Button>
                </DialogTrigger>
                <DialogContent className="sm:max-w-[425px]">
                  <DialogHeader>
                    <DialogTitle>Delete Game</DialogTitle>
                    <DialogDescription>
                      Are you sure you want to delete "{name}"? This action cannot be undone.
                    </DialogDescription>
                  </DialogHeader>
                  <DialogFooter className="mt-4">
                    <Button variant="outline" onClick={() => setOpen(false)}>
                      Cancel
                    </Button>
                    <Button variant="destructive" onClick={handleDelete}>
                      <Trash2 className="mr-2 h-4 w-4" />
                      Delete
                    </Button>
                  </DialogFooter>
                </DialogContent>
              </Dialog>
            </div>
          </div>
        </CardContent>
      </Card>
    </>
  )
}

