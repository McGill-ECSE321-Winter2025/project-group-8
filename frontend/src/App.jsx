import { useState } from 'react'
import './App.css'
import {BrowserRouter, Route, Routes} from "react-router-dom";
import LandingPage from "./pages/LandingPage.jsx";
import EventsPage from "./pages/EventsPage.jsx";
import GameSearchPage from "./pages/GameSearchPage.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import RegistrationPage from "./pages/RegistrationPage.jsx";
import UserProfilePage from "./pages/UserProfilePage.jsx";
import UserSearchPage from "./pages/UserSearchPage.jsx";
import DashboardPage from "./pages/DashboardPage.jsx";
import MenuBar from "./components/menubar/MenuBar.jsx";
import ProtectedRoute from "./components/common/ProtectedRoute.jsx"; // Import ProtectedRoute
import { Toaster } from 'sonner';

function App() {
  return (
    <div className="flex flex-col min-h-screen mx-auto">
        <MenuBar />
        <Routes>
          {/* Public Routes */}
          <Route path="/"               element={<LandingPage />} />
          <Route path="/login"          element={<LoginPage />}/>
          <Route path="/register"       element={<RegistrationPage />}/>

          {/* Protected Routes */}
          <Route path="/events"         element={<ProtectedRoute><EventsPage /></ProtectedRoute>} />
          <Route path="/games"          element={<ProtectedRoute><GameSearchPage /></ProtectedRoute>}/>
          <Route path="/profile"        element={<ProtectedRoute><UserProfilePage /></ProtectedRoute>}/>
          <Route path="/user-search"    element={<ProtectedRoute><UserSearchPage /></ProtectedRoute>}/> {/* Protected user search too */}
          <Route path="/dashboard/profile"    element={<ProtectedRoute><DashboardPage /></ProtectedRoute>}/>
        </Routes>
        <Toaster position="top-right" richColors expand={true} />
    </div>
  )
}

export default App
