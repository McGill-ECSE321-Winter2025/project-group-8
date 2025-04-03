import { useState } from "react";
import { 
  Dialog, 
  DialogContent, 
  DialogDescription, 
  DialogFooter, 
  DialogHeader, 
  DialogTitle 
} from "../../ui/dialog";
import { Button } from "../ui/button";
import { Input } from "../../ui/input";

export function RequestGameDialog({ open, onOpenChange, onSubmit, game }) {
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [notes, setNotes] = useState("");

  // Early return if no game selected
  if (!game && open) {
    return (
      <Dialog open={open} onOpenChange={onOpenChange}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>No Game Selected</DialogTitle>
            <DialogDescription>
              Please select a game to request.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button onClick={() => onOpenChange(false)}>
              Close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    );
  }

  const handleSubmit = () => {
    // This would be an API call in a real application, using the CreateBorrowRequestDto structure
    const borrowRequest = {
      requesterId: 1001, // This would be the current user's ID
      requestedGameId: game?.id,
      startDate: startDate,
      endDate: endDate,
      notes: notes
    };
    
    console.log("Submitting borrow request:", borrowRequest);
    onSubmit(borrowRequest);
    
    // Reset form
    setStartDate("");
    setEndDate("");
    setNotes("");
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Request Game{game ? `: ${game.name}` : ""}</DialogTitle>
          <DialogDescription>
            Requesting copy from: {game?.owner?.name || "the owner"}
          </DialogDescription>
        </DialogHeader>
        
        <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
          <div>
            <label htmlFor="startDate" className="block text-sm font-medium mb-1">
              Start Date
            </label>
            <Input 
              id="startDate" 
              type="date" 
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              min={new Date().toISOString().split('T')[0]}
              required
            />
          </div>
          
          <div>
            <label htmlFor="endDate" className="block text-sm font-medium mb-1">
              End Date
            </label>
            <Input 
              id="endDate" 
              type="date" 
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              min={startDate || new Date().toISOString().split('T')[0]}
              required
            />
          </div>
          
          <div>
            <label htmlFor="notes" className="block text-sm font-medium mb-1">
              Additional Notes
            </label>
            <textarea
              id="notes"
              rows="3"
              className="w-full border border-input rounded-md px-3 py-2 bg-background text-sm"
              placeholder="Any special requests or information the owner should know..."
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
            />
          </div>
        </form>
        
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Cancel
          </Button>
          <Button onClick={() => {
            handleSubmit();
            onOpenChange(false);
          }}>
            Submit Request
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
} 