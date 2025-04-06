import { Link, useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button.jsx";
import { useEffect, useState } from "react";
import { getUserInfoByEmail } from "../../service/user-api.js";

export default function MenuBar() {
  const location = useLocation();
  const navigate = useNavigate();

  const [isLoggedIn, setIsLoggedIn] = useState(!!localStorage.getItem("token"));
  const [userName, setUserName] = useState("");

  useEffect(() => {
    const fetchUserInfo = async () => {
      const token = localStorage.getItem("token");
      const email = localStorage.getItem("userEmail");

      setIsLoggedIn(!!token);

      if (token && email) {
        try {
          const user = await getUserInfoByEmail(email);
          setUserName(user.username); // use correct key
        } catch (err) {
          console.error("Failed to fetch user name:", err);
        }
      }
    };

    fetchUserInfo();
  }, [location.pathname]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("userEmail");
    localStorage.removeItem("user");
    setIsLoggedIn(false);
    setUserName("");
    navigate("/");
  };

  return (
    <header className="bg-white border-b shadow-sm">
      <div className="flex items-center justify-between py-4 px-6 md:px-10 max-w-screen-xl mx-auto">
        <Link to="/" className="flex items-center gap-2">
          <img
            src="https://www.svgrepo.com/show/83116/board-games-set.svg"
            alt="Board Games Icon"
            className="w-10 h-10"
          />
          <span className="text-xl font-bold">BoardGameConnect</span>
        </Link>

        <div className="flex items-center gap-3">
          {isLoggedIn && (
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

          {!isLoggedIn && (
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

          {isLoggedIn && (
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
