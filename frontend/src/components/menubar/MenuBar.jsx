import { Link, useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button.jsx";
import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";

export default function MenuBar() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuth();

  const handleLogout = async () => {
    await logout();
    navigate("/");
  };

  const isLandingPage = location.pathname === "/";

  return (
    <header className="bg-white border-b shadow-sm">
      <div className="flex items-center justify-between py-4 px-6 md:px-10 max-w-screen-xl mx-auto">
        <Link to={isAuthenticated ? "/dashboard" : "/"} className="flex items-center gap-2">
          <img
            src="https://www.svgrepo.com/show/83116/board-games-set.svg"
            alt="Board Games Icon"
            className="w-10 h-10"
          />
          <span className="text-xl font-bold">BoardGameConnect</span>
        </Link>

        <div className="flex items-center gap-3">
          {/* Navigation Buttons (only for logged in users) */}
          {isAuthenticated && (
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
               {/* Add Link to User Search Page */}
               <Link to="/user-search">
                 <Button variant="ghost" className="text-sm font-semibold">Users</Button>
               </Link>
            </div>
          )}

          {/* Login / Sign Up (only if not logged in) */}
          {!isAuthenticated && (
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
          {isAuthenticated && (
            <div className="flex items-center gap-2 pl-3 border-l border-gray-200">
              {user && (
                <span className="text-sm text-gray-700 font-medium">Hi, {user.name}</span>
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
