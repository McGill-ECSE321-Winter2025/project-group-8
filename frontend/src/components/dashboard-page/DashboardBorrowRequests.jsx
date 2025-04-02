import BorrowRequest from "@/components/dashboard-page/BorrowRequest.jsx";
import {TabsContent} from "@/components/ui/tabs.jsx";
import {Button} from "@/components/ui/button.jsx";

export default function DashboardBorrowRequests() {

  const dummyRequests = [
    {
      id: 1,
      name: "Dragon's Dilemma",
      requester: "Alice Nguyen",
      date: "March 30, 2025",
      endDate: "April 5, 2025",
    },
    {
      id: 2,
      name: "Mystic Rails",
      requester: "Jamal Thompson",
      date: "April 1, 2025",
      endDate: "April 7, 2025",
    },
    {
      id: 3,
      name: "Realm of Riddles",
      requester: "Sophie Tremblay",
      date: "March 28, 2025",
      endDate: "April 3, 2025",
    },
    {
      id: 4,
      name: "Cyber Syndicate",
      requester: "Leo Chen",
      date: "April 2, 2025",
      endDate: "April 9, 2025",
    },
  ];

  return <TabsContent value="requests" className="space-y-6">
    <div className="flex justify-between items-center">
      <h2 className="text-2xl font-bold">My Borrow Requests</h2>
    </div>
    <div className="space-y-4">
      {dummyRequests.map(request => <BorrowRequest key={request.id} {...request} />)}
    </div>
  </TabsContent>
}