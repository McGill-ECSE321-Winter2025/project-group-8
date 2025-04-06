import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import { getUserProfile, logoutUser } from '../service/user-api';
import { UnauthorizedError, ConnectionError } from '../service/apiClient';

const AuthContext = createContext(null);

// Key for storing user data in localStorage
const USER_STORAGE_KEY = 'boardgame_connect_user';

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    // Try to get stored user data on initial load
    try {
      const storedUser = localStorage.getItem(USER_STORAGE_KEY);
      return storedUser ? JSON.parse(storedUser) : null;
    } catch (e) {
      console.error('Error reading user from localStorage:', e);
      return null;
    }
  });
  const [loading, setLoading] = useState(true);
  const [authInitialized, setAuthInitialized] = useState(false);
  const [connectionError, setConnectionError] = useState(false);
  const [authCheckRetries, setAuthCheckRetries] = useState(0);
  
  const MAX_AUTH_CHECK_RETRIES = 3;
  const RETRY_DELAY_MS = 1000; // 1 second

  // Function to update user in state and localStorage
  const updateUser = useCallback((userData) => {
    setUser(userData);
    if (userData) {
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(userData));
    } else {
      localStorage.removeItem(USER_STORAGE_KEY);
    }
  }, []);

  // Function to check authentication status with retry capability
  const checkAuthStatus = useCallback(async (isRetry = false) => {
    if (!isRetry) {
      setLoading(true);
      setConnectionError(false);
    }
    
    console.log(`AuthContext: Checking auth status${isRetry ? ' (retry attempt)' : ''}`);
    
    // First check if the isAuthenticated cookie exists
    const isAuthCookie = document.cookie.includes('isAuthenticated=true');
    console.log('AuthContext: isAuthenticated cookie exists:', isAuthCookie);
    
    // If cookie doesn't exist, we're not authenticated
    if (!isAuthCookie && !isRetry) {
      console.log('AuthContext: No authentication cookie found, user is not authenticated');
      updateUser(null);
      setAuthInitialized(true);
      setLoading(false);
      return false;
    }
    
    try {
      // Attempt to fetch user profile to check if we're authenticated
      const currentUser = await getUserProfile();
      console.log('AuthContext: User is authenticated', currentUser);
      updateUser(currentUser);
      setAuthInitialized(true);
      setAuthCheckRetries(0); // Reset retry counter on success
      return true;
    } catch (error) {
      // Handle connection errors with retry logic
      if (error instanceof ConnectionError) {
        console.warn('AuthContext: Backend connection error:', error.message);
        setConnectionError(true);
        
        // Implement retry logic for connection errors
        if (authCheckRetries < MAX_AUTH_CHECK_RETRIES) {
          console.log(`AuthContext: Will retry auth check (attempt ${authCheckRetries + 1} of ${MAX_AUTH_CHECK_RETRIES})`);
          setAuthCheckRetries(prev => prev + 1);
          // Schedule retry after delay
          setTimeout(() => checkAuthStatus(true), RETRY_DELAY_MS);
          return false; // Don't complete auth flow yet
        } else {
          console.warn('AuthContext: Max retries reached, giving up auth check');
          // Keep existing user state on connection error after max retries
          // This helps prevent logout on temporary network issues
          setAuthInitialized(true);
        }
      }
      // Handle auth errors
      else if (error instanceof UnauthorizedError) {
        console.log('AuthContext: User is not authenticated');
        updateUser(null);
        setAuthInitialized(true);
      } 
      // Handle other errors
      else {
        console.warn('AuthContext: Auth check failed with unexpected error:', error.message);
        // Similar to connection errors, don't immediately clear user state
        // This helps prevent logout on temporary backend issues
        if (authCheckRetries < MAX_AUTH_CHECK_RETRIES) {
          console.log(`AuthContext: Will retry auth check for unexpected error (attempt ${authCheckRetries + 1} of ${MAX_AUTH_CHECK_RETRIES})`);
          setAuthCheckRetries(prev => prev + 1);
          setTimeout(() => checkAuthStatus(true), RETRY_DELAY_MS);
          return false;
        } else {
          console.warn('AuthContext: Max retries reached for unexpected error');
          // Only clear user state after exhausting retries
          updateUser(null);
          setAuthInitialized(true);
        }
      }
      return false;
    } finally {
      if (!isRetry || authCheckRetries >= MAX_AUTH_CHECK_RETRIES) {
        setLoading(false);
      }
    }
  }, [authCheckRetries, updateUser]);

  // Check auth status on initial load and handle network reconnection
  useEffect(() => {
    checkAuthStatus();
    
    // Add an event listener for when the network comes back online
    // This helps restore authentication after network disconnections
    const handleOnline = () => {
      console.log('AuthContext: Network is back online, rechecking auth status');
      checkAuthStatus();
    };
    
    window.addEventListener('online', handleOnline);
    
    // Clean up event listener on component unmount
    return () => {
      window.removeEventListener('online', handleOnline);
    };
  }, [checkAuthStatus]);

  const login = async (userData) => {
    // Log successful authentication
    console.log('AuthContext: Setting user data', userData);
    
    // Set user data in state and localStorage
    updateUser(userData);
    
    // Clear any previous connection errors
    setConnectionError(false);
    setAuthCheckRetries(0);
    
    // Return true to indicate successful login
    return true;
  };

  const logout = async () => {
    try {
      console.log('AuthContext: Logging out user');
      // Call backend logout API to clear cookies
      await logoutUser();
    } catch (error) {
      console.error("AuthContext: Error during logout:", error);
      // Continue with local logout even if API call fails
    } finally {
      // Always clean up local state regardless of API success
      updateUser(null);
      setConnectionError(false);
      setAuthCheckRetries(0);
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
    authInitialized,
    connectionError
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