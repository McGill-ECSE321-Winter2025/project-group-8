import {Link} from "react-router-dom";
import {Button} from "@/components/ui/button.jsx";

export default function SideMenuBar({userType}) {
  return <>
    <Link to="/dashboard">
      <Button variant="ghost" className="w-full justify-start gap-2">
        Profile
      </Button>
    </Link>
    {userType !== "owner" && (
      <Button variant="ghost" className="w-full justify-start gap-2">
        Become a Game Owner!
      </Button>
    )}
    <Button variant="ghost" className="w-full justify-start gap-2">
      Settings
    </Button>
  </>
}