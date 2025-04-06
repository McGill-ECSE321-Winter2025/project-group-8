// Importing required modules and components for routing, UI, and user data fetching
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button.jsx";
import { useEffect, useState } from "react";
import { getUserInfoByEmail } from "../../service/user-api.js";
import { useAuth } from "../../context/AuthContext.jsx";

// Main menu component shown on all pages
export default function MenuBar() {
  const location = useLocation();
  const navigate = useNavigate();
  
  // Use the auth context instead of managing state locally
  const { user, isAuthenticated, logout, loading } = useAuth();
  const [userName, setUserName] = useState("");

  // Update username when user or auth state changes
  useEffect(() => {
    if (isAuthenticated && user) {
      setUserName(user.name || user.username || user.email || "");
    } else {
      setUserName("");
    }
  }, [user, isAuthenticated]);

  // Handle logout: use auth context logout function
  const handleLogout = () => {
    logout();
    navigate("/");
  };

  // Render the actual menu bar, including navigation and user actions
  return (
    <header className="bg-white border-b shadow-sm">
      <div className="flex items-center justify-between py-4 px-6 md:px-10 max-w-screen-xl mx-auto">
        {/* Logo and homepage link */}
        <Link to="/" className="flex items-center gap-2">
          <img
            src="https://www.svgrepo.com/show/83116/board-games-set.svg"
            alt="Board Games Icon"
            className="w-10 h-10"
          />
          <span className="text-xl font-bold">BoardGameConnect</span>
        </Link>

        {/* Right-side navigation options */}
        <div className="flex items-center gap-3">
          {/* Links shown only when user is logged in */}
          {isAuthenticated && (
            <div className="flex items-center gap-2">
              <Link to="/dashboard">
                <Button
                  variant="ghost"
                  className="text-sm font-semibold"
                  title="Go to your main dashboard"
                >
                  Dashboard
                </Button>
              </Link>

              <Link to="/events">
                <Button
                  variant="ghost"
                  className="text-sm font-semibold"
                  title="View and manage events"
                >
                  Events
                </Button>
              </Link>

              <Link to="/games">
                <Button
                  variant="ghost"
                  className="text-sm font-semibold"
                  title="Find and explore games"
                >
                  Game Search
                </Button>
              </Link>

              <Link to="/user-search">
                <Button
                  variant="ghost"
                  className="text-sm font-semibold"
                  title="Browse and view other users"
                >
                  Users
                </Button>
              </Link>
            </div>
          )}

          {/* Show login/signup buttons when not logged in */}
          {!isAuthenticated && !loading && (
            <>
              <Link to="/login">
                <Button variant="outline" className="text-sm px-4">
                  Login
                </Button>
              </Link>
              <Link to="/register">
                <Button className="text-sm px-4">Sign Up</Button>
              </Link>
            </>
          )}

          {/* Show greeting and logout button when user is logged in */}
          {isAuthenticated && (
            <div className="flex items-center gap-2 pl-3 border-l border-gray-200">
              {userName && (
                <span className="text-sm text-gray-700 font-medium">
                  Hi, {userName} 👋
                </span>
              )}
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
