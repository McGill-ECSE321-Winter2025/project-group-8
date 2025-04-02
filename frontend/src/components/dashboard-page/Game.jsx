import {TabsContent} from "@/components/ui/tabs.jsx";
import {Button} from "@/components/ui/button.jsx";
import {Card, CardContent} from "@/components/ui/card.jsx";

export default function Game({userType, name, date, status}) {
  return <TabsContent value="games" className="space-y-6">
    <div className="flex justify-between items-center">
      <h2 className="text-2xl font-bold">My Games</h2>
      {userType === "owner" && <Button>Add New Game</Button>}
    </div>

    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
      <Card>
        <CardContent className="p-0">
          <div className="aspect-[4/3] relative">
            <img
              src="/placeholder.svg?height=300&width=400"
              alt="Settlers of Catan"
              className="object-cover w-full h-full rounded-t-lg"
            />
          </div>
          <div className="p-4">
            <h3 className="font-semibold text-lg">{name}</h3>
            <p className="text-sm text-muted-foreground">Added on {date}</p>
            <div className="flex justify-between items-center mt-4">
              <div className="text-sm">
                <span className="font-medium">Status:</span> {status}
              </div>
              <Button variant="outline" size="sm">
                Manage
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  </TabsContent>
}