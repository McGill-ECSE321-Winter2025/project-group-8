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

function App() {
  return (
    <div className="flex flex-col min-h-screen mx-auto">
        <MenuBar />
        <Routes>
          <Route path="/"               element={<LandingPage />} />
          <Route path="/events"         element={<EventsPage />} />
          <Route path="/games"          element={<GameSearchPage />}/>
          <Route path="/login"          element={<LoginPage />}/>
          <Route path="/register"       element={<RegistrationPage />}/>
          <Route path="/profile"        element={<UserProfilePage />}/>
          <Route path="/user-search"    element={<UserSearchPage />}/>
          <Route path="/dashboard/*"      element={<DashboardPage />}/>
        </Routes>
    </div>
  )
}

export default App
