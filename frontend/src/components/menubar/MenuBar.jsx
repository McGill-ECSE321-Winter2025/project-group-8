import { Link, useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button.jsx";
import { useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { BellIcon } from "lucide-react";
import {
  Menubar,
  MenubarContent,
  MenubarItem,
  MenubarMenu,
  MenubarTrigger,
} from "@/components/ui/menubar";

export default function MenuBar() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  // Fetch notifications when user is authenticated
  useEffect(() => {
    if (isAuthenticated && user) {
      fetchNotifications();

      // Set up polling for new notifications every 30 seconds
      const interval = setInterval(fetchNotifications, 30000);
      return () => clearInterval(interval);
    }
  }, [isAuthenticated, user]);

  // Function to fetch borrow request notifications
  const fetchNotifications = async () => {
    try {
      if (!user?.id) return;

      // For game owners: fetch their games' requests with status updates
      const ownerRequestsResponse = await fetch('/borrowrequests', {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
      });
      const ownerRequests = await ownerRequestsResponse.json();
      
      // Filter for APPROVED or DECLINED statuses in the last 7 days
      const recentStatusChanges = ownerRequests.filter(req => {
        // Check if status is APPROVED or DECLINED
        if (req.status !== 'APPROVED' && req.status !== 'DECLINED') return false;
        
        // Check if updated within last 7 days (assuming updateDate exists or use requestDate)
        const updateDate = new Date(req.updateDate || req.requestDate);
        const sevenDaysAgo = new Date();
        sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
        return updateDate > sevenDaysAgo;
      });

      // If user is a requester, get their requests too
      const requesterRequestsResponse = await fetch(`/borrowrequests/requester/${user.id}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
      });
      const requesterRequests = await requesterRequestsResponse.json();

      // Combine notifications
      const allNotifications = [
        ...recentStatusChanges.map(req => ({
          id: `owner-${req.id}`,
          message: `Request for game #${req.gameId} was ${req.status.toLowerCase()}`,
          status: req.status,
          date: new Date(req.updateDate || req.requestDate),
          read: false
        })),
        ...requesterRequests
          .filter(req => req.status === 'APPROVED' || req.status === 'DECLINED')
          .map(req => ({
            id: `requester-${req.id}`,
            message: `Your request for game #${req.gameId} was ${req.status.toLowerCase()}`,
            status: req.status,
            date: new Date(req.updateDate || req.requestDate),
            read: false
          }))
      ];

      // Sort by date (newest first)
      allNotifications.sort((a, b) => b.date - a.date);
      
      setNotifications(allNotifications);
      setUnreadCount(allNotifications.filter(n => !n.read).length);
    } catch (error) {
      console.error("Error fetching notifications:", error);
    }
  };

  // Mark all notifications as read
  const markAllAsRead = () => {
    setNotifications(notifications.map(n => ({...n, read: true})));
    setUnreadCount(0);
  };

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

          {/* Notifications */}
          {isAuthenticated && (
            <Menubar className="border-none shadow-none bg-transparent">
              <MenubarMenu>
                <MenubarTrigger className="focus:bg-gray-100 hover:bg-gray-100 rounded-full p-2 relative">
                  <BellIcon className="w-5 h-5 text-gray-700" />
                  {unreadCount > 0 && (
                    <span className="absolute -top-1 -right-1 text-xs bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-[10px] font-bold">
                      {unreadCount > 9 ? '9+' : unreadCount}
                    </span>
                  )}
                </MenubarTrigger>
                <MenubarContent className="w-80 max-h-[400px] overflow-auto" align="end">
                  <div className="py-2 px-3 bg-gray-50 border-b flex justify-between items-center">
                    <h3 className="font-medium text-sm">Notifications</h3>
                    {notifications.length > 0 && (
                      <Button 
                        variant="ghost" 
                        className="h-7 text-xs"
                        onClick={markAllAsRead}
                      >
                        Mark all read
                      </Button>
                    )}
                  </div>
                  
                  {notifications.length === 0 ? (
                    <div className="py-6 text-center text-gray-500">
                      <p className="text-sm">No notifications</p>
                    </div>
                  ) : (
                    <>
                      {notifications.map((notification) => (
                        <MenubarItem 
                          key={notification.id} 
                          className={`px-3 py-2 cursor-default ${notification.read ? 'bg-white' : 'bg-blue-50'}`}
                        >
                          <div className="flex items-start gap-2">
                            <div className={`w-2 h-2 mt-1.5 rounded-full flex-shrink-0 ${
                              notification.status === 'APPROVED' ? 'bg-green-500' : 'bg-red-500'
                            }`}/>
                            <div className="flex-1">
                              <p className="text-sm">{notification.message}</p>
                              <p className="text-xs text-gray-500 mt-1">
                                {notification.date.toLocaleDateString()} at {notification.date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                              </p>
                            </div>
                          </div>
                        </MenubarItem>
                      ))}
                    </>
                  )}
                </MenubarContent>
              </MenubarMenu>
            </Menubar>
          )}

          {/* Logout Button (only if logged in) */}
          {isAuthenticated && (
            <div className="flex items-center gap-2 pl-3 border-l border-gray-200">
              {user && (
                <span className="text-sm text-gray-700 font-medium">Hi, {user.name}</span>
              )}
              <Button
                onClick={handleLogout}
                className="text-sm px-3 py-1.5 bg-red-600 text-white hover:bg-red-700 border-none"
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
