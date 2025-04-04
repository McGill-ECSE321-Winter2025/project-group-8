import { Link, useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button.jsx";
import { useEffect, useState } from "react";

export default function MenuBar() {
  const location = useLocation();
  const navigate = useNavigate();
  const [user, setUser] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("user");
    setUser(null);
    navigate("/");
  };

  const isLandingPage = location.pathname === "/";

  return (
    <header className="bg-white border-b shadow-sm">
      <div className="flex items-center justify-between py-4 px-6 md:px-10 max-w-screen-xl mx-auto">
        <Link to="/" className="flex items-center gap-2">
          <span className="text-xl font-bold">BoardGameConnect</span>
        </Link>

        <div className="flex items-center gap-3">
          {/* Navigation Buttons (only for logged in users) */}
          {user && (
            <div className="flex items-center gap-2">
              <Link to="/games">
                <Button variant="ghost" className="text-sm font-semibold">Games</Button>
              </Link>
              <Link to="/events">
                <Button variant="ghost" className="text-sm font-semibold">Events</Button>
              </Link>
            </div>
          )}

          {/* Login / Sign Up (only if not logged in and on landing page) */}
          {!user && isLandingPage && (
            <>
              <Link to="/login">
                <Button variant="outline" className="text-sm px-4">Login</Button>
              </Link>
              <Link to="/register">
                <Button className="text-sm px-4">Sign Up</Button>
              </Link>
            </>
          )}

          {/* Greeting + Logout (only if logged in) */}
          {user && (
            <div className="flex items-center gap-2 pl-3 border-l border-gray-200">
              <span className="text-sm text-gray-700 font-medium">Hi, {user.name}</span>
              <Button
                onClick={handleLogout}
                variant="outline"
                className="text-sm px-3 py-1.5 border-gray-300 hover:bg-gray-100"
              >
                Logout
              </Button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
