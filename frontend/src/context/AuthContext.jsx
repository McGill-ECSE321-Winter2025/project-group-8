import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import { getUserProfile } from '../service/user-api'; // Assuming this function exists or will be created

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true); // Add loading state

  // Function to check authentication status on initial load
  const checkAuthStatus = useCallback(async () => {
    setLoading(true);
    try {
      // Attempt to fetch user profile - relies on the HttpOnly cookie
      const currentUser = await getUserProfile(); // Use '/api/users/me' or similar
      setUser(currentUser);
    } catch (error) {
      // If fetching fails (e.g., 401 Unauthorized), assume not logged in
      console.warn('Initial auth check failed:', error.message);
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    checkAuthStatus();
  }, [checkAuthStatus]);

  const login = (userData) => {
    setUser(userData);
  };

  const logout = () => {
    setUser(null);
    // Optional: Add API call here to backend /auth/logout endpoint if it exists
    // Example: await logoutUserApi(); // Assuming logoutUserApi exists
  };

  // Provide checkAuthStatus in context if needed elsewhere, e.g., for manual refresh
  const value = { user, loading, login, logout, checkAuthStatus };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  // Return context directly, components can destructure { user, loading, login, logout }
  return context;
};