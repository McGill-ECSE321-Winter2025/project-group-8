import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import { getUserProfile, logoutUser } from '../service/user-api';
import { UnauthorizedError } from '../service/apiClient';

const AuthContext = createContext(null);

// Check if we either have a token in localStorage or an authenticated session cookie
const hasAuthToken = () => {
  return localStorage.getItem('authToken') !== null;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [authInitialized, setAuthInitialized] = useState(false);

  // Function to check authentication status on initial load
  const checkAuthStatus = useCallback(async () => {
    setLoading(true);
    try {
      // Always attempt to fetch user profile on initial load
      // The server uses cookies for authentication, so this will work if we have a valid cookie
      const currentUser = await getUserProfile();
      setUser(currentUser);
      setAuthInitialized(true);
      return true;
    } catch (error) {
      // Silent failure for auth errors
      if (error instanceof UnauthorizedError) {
        localStorage.removeItem('authToken'); // Clear token if it exists but is invalid
        setUser(null);
      } else {
        console.warn('Auth check failed with unexpected error:', error.message);
        setUser(null);
      }
      setAuthInitialized(true);
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    checkAuthStatus();
  }, [checkAuthStatus]);

  const login = async (userData, token) => {
    // Store token in localStorage if available
    if (token) {
      localStorage.setItem('authToken', token);
    }
    
    // Set user data
    setUser(userData);
    
    // Return true to indicate successful login
    return true;
  };

  const logout = async () => {
    try {
      // Call backend logout API to clear cookies
      await logoutUser();
    } catch (error) {
      console.error("Error during logout:", error);
    } finally {
      // Always clean up local state regardless of API success
      localStorage.removeItem('authToken');
      setUser(null);
    }
  };

  // Provide context values
  const value = { 
    user, 
    loading, 
    login, 
    logout, 
    checkAuthStatus,
    isAuthenticated: !!user,
    authInitialized
  };

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
  return context;
};