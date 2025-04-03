import {TabsContent} from "@/components/ui/tabs.jsx";
import LendingRecord from "@/components/dashboard-page/LendingRecord.jsx";

export default function DashboardLendingRecord() {

  const dummyLendingRecords = [
    {
      id: 1,
      name: "Castle Siege Tactics",
      requester: "Emily Park",
      startDate: "March 29, 2025",
      endDate: "April 5, 2025",
    },
    {
      id: 2,
      name: "Galactic Traders",
      requester: "Daniel Cho",
      startDate: "March 31, 2025",
      endDate: "April 7, 2025",
    },
    {
      id: 3,
      name: "Realm of Riddles",
      requester: "Noah Wilson",
      startDate: "March 27, 2025",
      endDate: "April 3, 2025",
    },
    {
      id: 4,
      name: "Dragon’s Dilemma",
      requester: "Isabelle Gagnon",
      startDate: "April 1, 2025",
      endDate: "April 8, 2025",
    },
  ];

  return <TabsContent value="borrowing" className="space-y-6">
    <div className="flex justify-between items-center">
      <h2 className="text-2xl font-bold">Lending History</h2>
    </div>
    <div className="space-y-4">
      { dummyLendingRecords.map(record => <LendingRecord key={record.id} {...record} />)}
    </div>
  </TabsContent>
}