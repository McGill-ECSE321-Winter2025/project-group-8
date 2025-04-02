import {Link} from "react-router-dom";
import {Button} from "../ui/button.jsx";

export default function MenuBar() {
    return (
    <div className="">
        <header className="bg-background border-b w-full">
            <div className="flex items-center justify-between py-4 mx-10">
                <Link to="/" className="flex items-center gap-2">
                    <span className="text-xl font-bold">BoardGameConnect</span>
                </Link>
                <div className="flex items-center gap-2">
                    <Link to="/games">
                        <Button variant="ghost">Games</Button>
                    </Link>
                    <Link to="/events">
                        <Button variant="ghost">Events</Button>
                    </Link>
                    <Link to="/login">
                        <Button variant="outline">Login</Button>
                    </Link>
                    <Link to="/register">
                        <Button>Sign Up</Button>
                    </Link>
                </div>
            </div>
        </header>
    </div>
    )
}