import {Button} from "@/components/ui/button.jsx";
import Game from "./Game.jsx";
import {TabsContent} from "@/components/ui/tabs.jsx";

export default function DashboardGameLibrary({ userType }) {

  const dummyItems = [
    {
      id: 1,
      name: "Dragon's Dilemma",
      date: "January 12, 2025",
      isAvailable: true,
      imageSrc: "/placeholder.svg?height=300&width=400",
    },
    {
      id: 2,
      name: "Mystic Rails",
      date: "February 3, 2025",
      isAvailable: false,
      imageSrc: "/placeholder.svg?height=300&width=400",
    },
    {
      id: 3,
      name: "Castle Siege Tactics",
      date: "February 18, 2025",
      isAvailable: true,
      imageSrc: "/placeholder.svg?height=300&width=400",
    },
    {
      id: 4,
      name: "Cyber Syndicate",
      date: "March 1, 2025",
      isAvailable: false,
      imageSrc: "/placeholder.svg?height=300&width=400",
    },
    {
      id: 5,
      name: "Realm of Riddles",
      date: "March 27, 2025",
      isAvailable: true,
      imageSrc: "/placeholder.svg?height=300&width=400",
    },
    {
      id: 6,
      name: "Galactic Traders",
      date: "April 1, 2025",
      isAvailable: true,
      imageSrc: "/placeholder.svg?height=300&width=400",
    },
  ];

  return <TabsContent value="games" className="space-y-6">
    <div className="flex justify-between items-center">
      <h2 className="text-2xl font-bold">My Games</h2>
      {userType === "owner" && <Button>Add New Game</Button>}
    </div>

    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
      {dummyItems.map(item => <Game key={item.id} {...item} />)}
    </div>
  </TabsContent>
}