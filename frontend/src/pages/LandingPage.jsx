import React from "react";
import { useNavigate } from "react-router-dom";

export default function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col min-h-screen">
      {/* Hero Section */}
      <main className="flex flex-col items-center justify-center text-center py-32 px-4 bg-gradient-to-b from-white to-gray-100">
        <h1 className="text-5xl font-bold mb-6">Connect Through Board Games</h1>
        <p className="text-gray-600 mb-8 max-w-2xl text-lg">
          Find games, meet players, and organize events with fellow board game enthusiasts in your area.
        </p>
        <div className="flex gap-4">
          <button
            onClick={() => navigate('/register')}
            className="bg-black text-white px-8 py-3 rounded text-lg"
          >
            <i className="fas fa-users mr-2"></i>Join the Community
          </button>
          <button
            onClick={() => navigate('/games')}
            className="border border-black px-8 py-3 rounded text-lg"
          >
            <i className="fas fa-dice mr-2"></i>Browse Games
          </button>
        </div>
      </main>

      {/* How It Works Section */}
      <section className="py-24 px-6 bg-white">
        <h2 className="text-3xl font-bold text-center mb-14">How It Works</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-12 text-center">
          <div>
            <div className="text-5xl mb-4">
              <i className="fas fa-gamepad"></i>
            </div>
            <h3 className="text-xl font-semibold mb-2">Discover Games</h3>
            <p className="text-gray-600">Browse through a vast collection of board games shared by our community members.</p>
          </div>
          <div>
            <div className="text-5xl mb-4">
              <i className="fas fa-user-friends"></i>
            </div>
            <h3 className="text-xl font-semibold mb-2">Connect with Players</h3>
            <p className="text-gray-600">Find and connect with other board game enthusiasts in your area.</p>
          </div>
          <div>
            <div className="text-5xl mb-4">
              <i className="fas fa-calendar-alt"></i>
            </div>
            <h3 className="text-xl font-semibold mb-2">Organize Events</h3>
            <p className="text-gray-600">Create and join board game events to play your favorite games with others.</p>
          </div>
        </div>
      </section>

      {/* Share Your Collection Section */}
      <section className="py-24 px-6 bg-gray-100 flex flex-col lg:flex-row items-center justify-between">
        <div className="max-w-lg mb-12 lg:mb-0">
          <h2 className="text-3xl font-bold mb-6">Share Your Collection</h2>
          <p className="text-gray-600 mb-6 text-lg">
            As a Game Owner, you can showcase your board game collection, lend games to other players, and build a reputation in the community.
          </p>
          <button
            onClick={() => navigate('/register')}
            className="bg-black text-white px-8 py-3 rounded text-lg"
          >
            Register as a Game Owner
          </button>
        </div>
        <div className="grid grid-cols-2 gap-6 w-full lg:w-1/2">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="bg-white rounded shadow h-36 flex items-center justify-center">
              <i className="fas fa-dice text-3xl text-gray-300"></i>
            </div>
          ))}
        </div>
      </section>

      {/* Footer section */}
      <footer className="text-center text-sm text-gray-500 py-6 border-t">
        <div className="flex items-center justify-center gap-2 font-semibold text-black">
        </div>
        <div>© 2025 BoardGameConnect. All rights reserved.</div>
      </footer>
    </div>
  );
}
