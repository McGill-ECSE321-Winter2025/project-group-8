"use client"

import { useState } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { AlertCircle, Calendar, Edit, Trash2 } from "lucide-react"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Textarea } from "@/components/ui/textarea"

export default function MyEvent({
                                  id,
                                  name,
                                  date,
                                  time,
                                  location,
                                  description = "",
                                  game,
                                  participants: { current, capacity },
                                  onUpdateEvent,
                                  onDeleteEvent,
                                }) {
  const [open, setOpen] = useState(false)
  const [activeTab, setActiveTab] = useState("edit")

  // Form state
  const [formData, setFormData] = useState({
    title: name,
    date: date,
    time: time,
    location: location,
    description: description,
    maxParticipants: capacity,
    game: game,
  })

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleUpdateEvent = () => {
    if (typeof onUpdateEvent === "function") {
      onUpdateEvent(id, formData)
    }
    setOpen(false)
  }

  const handleDeleteEvent = () => {
    if (typeof onDeleteEvent === "function") {
      onDeleteEvent(id)
    }
    setOpen(false)
  }

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
              {description && (
                <div className="text-sm mt-2">
                  <span className="font-medium">Description:</span> {description}
                </div>
              )}
            </div>
            <div className="flex gap-2 mt-4">
              <Dialog open={open} onOpenChange={setOpen}>
                <DialogTrigger asChild>
                  <Button variant="outline" size="sm">
                    <Edit className="h-4 w-4 mr-2" />
                    Manage Event
                  </Button>
                </DialogTrigger>
                <DialogContent className="sm:max-w-[550px]">
                  <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
                    <TabsList className="grid w-full grid-cols-2 mt-4 mb-2">
                      <TabsTrigger value="edit">Edit Event</TabsTrigger>
                      <TabsTrigger value="delete">Delete Event</TabsTrigger>
                    </TabsList>

                    <TabsContent value="edit" className="space-y-4">
                      <DialogHeader>
                        <DialogTitle>Edit Event Details</DialogTitle>
                        <DialogDescription>Make changes to your event. Click save when you're done.</DialogDescription>
                      </DialogHeader>

                      <div className="grid gap-4 py-4">
                        <div className="grid gap-2">
                          <Label htmlFor="title">Event Title</Label>
                          <Input id="title" name="title" value={formData.title} onChange={handleInputChange} />
                        </div>

                        <div className="grid grid-cols-2 gap-4">
                          <div className="grid gap-2">
                            <Label htmlFor="date">Date</Label>
                            <Input
                              id="date"
                              name="date"
                              type="date"
                              value={formData.date}
                              onChange={handleInputChange}
                            />
                          </div>
                          <div className="grid gap-2">
                            <Label htmlFor="time">Time</Label>
                            <Input
                              id="time"
                              name="time"
                              type="time"
                              value={formData.time}
                              onChange={handleInputChange}
                            />
                          </div>
                        </div>

                        <div className="grid gap-2">
                          <Label htmlFor="location">Location</Label>
                          <Input id="location" name="location" value={formData.location} onChange={handleInputChange} />
                        </div>

                        <div className="grid gap-2">
                          <Label htmlFor="game">Game</Label>
                          <Input id="game" name="game" value={formData.game} onChange={handleInputChange} />
                        </div>

                        <div className="grid gap-2">
                          <Label htmlFor="maxParticipants">Maximum Participants</Label>
                          <Input
                            id="maxParticipants"
                            name="maxParticipants"
                            type="number"
                            min="1"
                            value={formData.maxParticipants}
                            onChange={handleInputChange}
                          />
                        </div>

                        <div className="grid gap-2">
                          <Label htmlFor="description">Description</Label>
                          <Textarea
                            id="description"
                            name="description"
                            rows={3}
                            value={formData.description}
                            onChange={handleInputChange}
                          />
                        </div>
                      </div>

                      <DialogFooter>
                        <Button variant="outline" onClick={() => setOpen(false)}>
                          Cancel
                        </Button>
                        <Button onClick={handleUpdateEvent}>Save Changes</Button>
                      </DialogFooter>
                    </TabsContent>

                    <TabsContent value="delete">
                      <DialogHeader>
                        <DialogTitle className="flex items-center gap-2">
                          <AlertCircle className="h-5 w-5 text-destructive" />
                          Delete Event
                        </DialogTitle>
                        <DialogDescription>
                          Are you sure you want to delete this event? This action cannot be undone.
                        </DialogDescription>
                      </DialogHeader>

                      <div className="py-4">
                        <div className="rounded-lg bg-muted p-4 text-sm">
                          <p>
                            <span className="font-medium">Event:</span> {name}
                          </p>
                          <p>
                            <span className="font-medium">Date:</span> {date}
                          </p>
                          <p>
                            <span className="font-medium">Time:</span> {time}
                          </p>
                          <p>
                            <span className="font-medium">Location:</span> {location}
                          </p>
                          <p>
                            <span className="font-medium">Participants:</span> {current}/{capacity}
                          </p>
                        </div>
                      </div>

                      <DialogFooter>
                        <Button variant="outline" onClick={() => setActiveTab("edit")}>
                          Go Back
                        </Button>
                        <Button variant="destructive" onClick={handleDeleteEvent}>
                          <Trash2 className="h-4 w-4 mr-2" />
                          Delete Event
                        </Button>
                      </DialogFooter>
                    </TabsContent>
                  </Tabs>
                </DialogContent>
              </Dialog>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}

