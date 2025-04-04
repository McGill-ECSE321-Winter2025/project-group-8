import { Link, useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button.jsx";
import { useEffect, useState } from "react";

export default function MenuBar() {
  const location = useLocation();
  const navigate = useNavigate();
  // Use isLoggedIn state based on token presence
  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem("token"));

  // Update login status whenever the URL path changes
  useEffect(() => {
    // Directly check the token presence when the location changes
    setIsLoggedIn(!!localStorage.getItem("token"));
  }, [location.pathname]); // Add location.pathname as a dependency


  const handleLogout = () => {
    // Remove token and userId on logout
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    setIsLoggedIn(false); // Update state immediately
    navigate("/"); // Redirect to landing page
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
          {isLoggedIn && ( // Check isLoggedIn state
            <div className="flex items-center gap-2">
              {/* Link to Dashboard instead of Games? */}
              <Link to="/dashboard">
                <Button variant="ghost" className="text-sm font-semibold">Dashboard</Button>
              </Link>
              <Link to="/events">
                <Button variant="ghost" className="text-sm font-semibold">Events</Button>
              </Link>
               <Link to="/games">
                 <Button variant="ghost" className="text-sm font-semibold">Game Search</Button>
               </Link>
            </div>
          )}

          {/* Login / Sign Up (only if not logged in) */}
          {!isLoggedIn && ( // Check isLoggedIn state
            <>
              <Link to="/login">
                <Button variant="outline" className="text-sm px-4">Login</Button>
              </Link>
              <Link to="/register">
                <Button className="text-sm px-4">Sign Up</Button>
              </Link>
            </>
          )}

          {/* Logout Button (only if logged in) */}
          {isLoggedIn && ( // Check isLoggedIn state
            <div className="flex items-center gap-2 pl-3 border-l border-gray-200">
              {/* Removed user name display for simplicity for now */}
              {/* <span className="text-sm text-gray-700 font-medium">Hi, {user.name}</span> */}
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
