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

function App() {
  const [count, setCount] = useState(0)

  return (
    <>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/events" element={<EventsPage />} />
          <Route path="/games" element={<GameSearchPage />}/>
          <Route path="/login" element={<LoginPage />}/>
          <Route path="/signup" element={<RegistrationPage/>}/>
          <Route path="/profile" element={<UserProfilePage />}/>
          <Route path="/user-search" element={<UserSearchPage />}/>
          <Route path="/dashboard" element={<DashboardPage />}/>
        </Routes>
      </BrowserRouter>
    </>
  )
}

export default App
