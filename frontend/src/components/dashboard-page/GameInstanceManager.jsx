import { useState, useEffect, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Loader2, Edit, Plus, Check, X } from "lucide-react";
import { getGameInstances, updateGameInstance, createGameInstance } from "@/service/game-api.js";
import { useAuth } from "@/context/AuthContext";
import { toast } from "sonner";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { useForm } from "react-hook-form";

export default function GameInstanceManager({ gameId, gameName, refreshGames }) {
  const [instances, setInstances] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editingInstance, setEditingInstance] = useState(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const { user } = useAuth();

  const editForm = useForm({
    defaultValues: {
      condition: "",
      location: "",
      available: true,
    }
  });

  const addForm = useForm({
    defaultValues: {
      condition: "Excellent",
      location: "Home",
    }
  });

  // Fetch instances
  const fetchInstances = useCallback(async () => {
    if (!gameId) return;
    
    setIsLoading(true);
    setError(null);
    
    try {
      const data = await getGameInstances(gameId);
      // Filter instances to only show those owned by the current user
      const userInstances = data.filter(instance => 
        instance.owner && instance.owner.id === user?.id
      );
      setInstances(userInstances);
    } catch (err) {
      console.error(`Failed to fetch instances for game ${gameId}:`, err);
      setError("Failed to load game copies");
      toast.error("Could not load game copies");
    } finally {
      setIsLoading(false);
    }
  }, [gameId, user?.id]);

  useEffect(() => {
    fetchInstances();
  }, [fetchInstances]);

  // Handle opening the edit dialog
  const handleEditClick = (instance) => {
    setEditingInstance(instance);
    editForm.reset({
      condition: instance.condition || "",
      location: instance.location || "",
      available: instance.available
    });
    setIsEditModalOpen(true);
  };

  // Handle updating an instance
  const handleUpdateInstance = async (data) => {
    if (!editingInstance) return;
    
    setIsLoading(true);
    try {
      await updateGameInstance(editingInstance.id, {
        ...data,
        gameId: gameId
      });
      
      toast.success("Game copy updated successfully");
      await fetchInstances();
      setIsEditModalOpen(false);
    } catch (err) {
      console.error("Failed to update game instance:", err);
      toast.error("Failed to update game copy");
    } finally {
      setIsLoading(false);
    }
  };

  // Handle adding a new instance
  const handleAddInstance = async (data) => {
    setIsLoading(true);
    try {
      await createGameInstance(gameId, {
        ...data,
        gameId: gameId,
        ownerId: user?.id
      });
      
      toast.success("New game copy added successfully");
      await fetchInstances();
      setIsAddModalOpen(false);
      if (refreshGames) refreshGames();
    } catch (err) {
      console.error("Failed to add game instance:", err);
      toast.error("Failed to add game copy");
    } finally {
      setIsLoading(false);
    }
  };

  // Condition options
  const conditionOptions = [
    { value: "New", label: "New" },
    { value: "Excellent", label: "Excellent" },
    { value: "Good", label: "Good" },
    { value: "Fair", label: "Fair" },
    { value: "Poor", label: "Poor" }
  ];

  return (
    <div className="space-y-4">
      {isLoading && (
        <div className="flex justify-center items-center py-6">
          <Loader2 className="h-6 w-6 animate-spin text-primary" />
        </div>
      )}
      
      {error && (
        <div className="text-center text-destructive py-4">{error}</div>
      )}
      
      {!isLoading && !error && (
        <>
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-medium">Your Copies of {gameName}</h3>
            <Button 
              variant="outline" 
              size="sm" 
              onClick={() => {
                addForm.reset({
                  condition: "Excellent",
                  location: "Home"
                });
                setIsAddModalOpen(true);
              }}
              className="flex items-center gap-1"
            >
              <Plus className="h-4 w-4" />
              Add Copy
            </Button>
          </div>

          {instances.length === 0 ? (
            <div className="text-center text-muted-foreground py-6">
              You don't have any copies of this game yet.
            </div>
          ) : (
            <div className="grid gap-4">
              {instances.map(instance => (
                <Card key={instance.id} className="overflow-hidden">
                  <CardContent className="p-4">
                    <div className="flex justify-between items-start">
                      <div>
                        <h4 className="font-medium">{gameName} (Copy #{instance.id})</h4>
                        <div className="grid grid-cols-2 gap-x-4 gap-y-2 mt-2 text-sm">
                          <div>
                            <span className="font-medium text-muted-foreground">Condition:</span>{" "}
                            {instance.condition || "Not specified"}
                          </div>
                          <div>
                            <span className="font-medium text-muted-foreground">Location:</span>{" "}
                            {instance.location || "Not specified"}
                          </div>
                          <div>
                            <span className="font-medium text-muted-foreground">Acquired:</span>{" "}
                            {new Date(instance.acquiredDate).toLocaleDateString()}
                          </div>
                          <div>
                            <span className="font-medium text-muted-foreground">Status:</span>{" "}
                            <Badge variant={instance.available ? "positive" : "destructive"}>
                              {instance.available ? "Available" : "Unavailable"}
                            </Badge>
                          </div>
                        </div>
                      </div>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleEditClick(instance)}
                        className="ml-4"
                      >
                        <Edit className="h-4 w-4" />
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </>
      )}

      {/* Edit Instance Dialog */}
      <Dialog 
        open={isEditModalOpen} 
        onOpenChange={(open) => {
          // When closing, first blur any active element to ensure proper focus management
          if (!open && document.activeElement instanceof HTMLElement) {
            document.activeElement.blur();
          }
          setIsEditModalOpen(open);
        }}
      >
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Edit Game Copy</DialogTitle>
            <DialogDescription>
              Update the details of your game copy.
            </DialogDescription>
          </DialogHeader>
          
          <Form {...editForm}>
            <form onSubmit={editForm.handleSubmit(handleUpdateInstance)} className="space-y-4">
              <FormField
                control={editForm.control}
                name="condition"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Condition</FormLabel>
                    <Select 
                      onValueChange={field.onChange} 
                      defaultValue={field.value} 
                      value={field.value}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select condition" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {conditionOptions.map(option => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormDescription>
                      The physical condition of your game copy.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={editForm.control}
                name="location"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Location</FormLabel>
                    <FormControl>
                      <Input placeholder="e.g., Home, Office" {...field} />
                    </FormControl>
                    <FormDescription>
                      Where you keep this game copy.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={editForm.control}
                name="available"
                render={({ field }) => (
                  <FormItem className="flex flex-row items-center justify-between rounded-lg border p-3">
                    <div className="space-y-0.5">
                      <FormLabel>Available for borrowing</FormLabel>
                      <FormDescription>
                        Make this copy available to other players.
                      </FormDescription>
                    </div>
                    <FormControl>
                      <div 
                        onClick={() => editForm.setValue('available', !field.value)}
                        className={`cursor-pointer flex items-center justify-center h-8 w-8 rounded-full ${field.value ? 'bg-green-100' : 'bg-red-100'}`}
                      >
                        {field.value ? (
                          <Check className="h-5 w-5 text-green-600" />
                        ) : (
                          <X className="h-5 w-5 text-red-600" />
                        )}
                      </div>
                    </FormControl>
                  </FormItem>
                )}
              />

              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setIsEditModalOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                  Save Changes
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>

      {/* Add Instance Dialog */}
      <Dialog 
        open={isAddModalOpen} 
        onOpenChange={(open) => {
          // When closing, first blur any active element to ensure proper focus management
          if (!open && document.activeElement instanceof HTMLElement) {
            document.activeElement.blur();
          }
          setIsAddModalOpen(open);
        }}
      >
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Add New Game Copy</DialogTitle>
            <DialogDescription>
              Register a new copy of {gameName} to your collection.
            </DialogDescription>
          </DialogHeader>
          
          <Form {...addForm}>
            <form onSubmit={addForm.handleSubmit(handleAddInstance)} className="space-y-4">
              <FormField
                control={addForm.control}
                name="condition"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Condition</FormLabel>
                    <Select 
                      onValueChange={field.onChange} 
                      defaultValue={field.value}
                    >
                      <FormControl>
                        <SelectTrigger>
                          <SelectValue placeholder="Select condition" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {conditionOptions.map(option => (
                          <SelectItem key={option.value} value={option.value}>
                            {option.label}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormDescription>
                      The physical condition of your game copy.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={addForm.control}
                name="location"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Location</FormLabel>
                    <FormControl>
                      <Input placeholder="e.g., Home, Office" {...field} />
                    </FormControl>
                    <FormDescription>
                      Where you keep this game copy.
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setIsAddModalOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isLoading}>
                  {isLoading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
                  Add Copy
                </Button>
              </DialogFooter>
            </form>
          </Form>
        </DialogContent>
      </Dialog>
    </div>
  );
} 