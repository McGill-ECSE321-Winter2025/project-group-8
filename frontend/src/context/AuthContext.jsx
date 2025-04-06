import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import { checkAuthStatus, getUserProfile, logoutUser } from '../service/auth-api';

// Create the auth context
const AuthContext = createContext(null);

// Inactive timeout - 30 minutes
const INACTIVE_TIMEOUT = 30 * 60 * 1000;

// Auth context provider component
export const AuthProvider = ({ children }) => {
  // User state - contains user data when authenticated
  const [user, setUser] = useState(null);
  // Loading state for async operations
  const [loading, setLoading] = useState(true);
  // Authentication state
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  // Error state
  const [error, setError] = useState(null);
  // Session expired flag
  const [isSessionExpired, setIsSessionExpired] = useState(false);
  // Remember me state
  const [rememberMe, setRememberMe] = useState(false);
  // Last activity timestamp
  const [lastActivity, setLastActivity] = useState(Date.now());

  // Function to update last activity timestamp
  const updateActivity = useCallback(() => {
    setLastActivity(Date.now());
    // If session was expired but user is active, attempt to revalidate
    if (isSessionExpired) {
      checkAuthStatus()
        .then(isAuth => {
          if (isAuth) {
            setIsSessionExpired(false);
          }
        })
        .catch(() => {
          // Ignore errors - will remain in expired state
        });
    }
  }, [isSessionExpired]);

  // Initialize authentication state
  const initAuth = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      // Check if user is authenticated
      const isAuth = await checkAuthStatus();

      if (isAuth) {
        // Fetch user profile if authenticated
        const userProfile = await getUserProfile();
        setUser(userProfile);
        setIsAuthenticated(true);
        setIsSessionExpired(false);

        // Check if remember me was set
        const savedRememberMe = localStorage.getItem('rememberMe') === 'true';
        setRememberMe(savedRememberMe);
      } else {
        // Clear authentication state
        setUser(null);
        setIsAuthenticated(false);
        localStorage.removeItem('rememberMe');
      }
    } catch (err) {
      console.error('Error initializing auth:', err);
      setError('Failed to initialize authentication');
      setUser(null);
      setIsAuthenticated(false);
    } finally {
      setLoading(false);
    }
  }, []);

  // Initialize auth when component mounts
  useEffect(() => {
    initAuth();
  }, [initAuth]);

  // Setup inactivity monitoring based on remember me
  useEffect(() => {
    // Only monitor inactivity if user is authenticated and remember me is not set
    if (!isAuthenticated || rememberMe) {
      return;
    }

    // Check for inactivity
    const checkInactivity = () => {
      const now = Date.now();
      const inactiveTime = now - lastActivity;

      // If user has been inactive for too long, expire session
      if (inactiveTime >= INACTIVE_TIMEOUT) {
        setIsSessionExpired(true);
      }
    };

    // Set up interval to check inactivity
    const intervalId = setInterval(checkInactivity, 60000); // Check every minute

    // Listen for user activity
    const activityEvents = ['mousedown', 'keypress', 'scroll', 'touchstart'];
    
    const handleActivity = () => {
      updateActivity();
    };

    // Add event listeners for user activity
    activityEvents.forEach(event => {
      window.addEventListener(event, handleActivity);
    });

    // Cleanup function
    return () => {
      clearInterval(intervalId);
      activityEvents.forEach(event => {
        window.removeEventListener(event, handleActivity);
      });
    };
  }, [isAuthenticated, lastActivity, rememberMe, updateActivity]);

  // Login function
  const login = useCallback(async (userData, remember = false) => {
    setUser(userData);
    setIsAuthenticated(true);
    setIsSessionExpired(false);
    setRememberMe(remember);
    setLastActivity(Date.now());
    
    // Save remember me preference
    localStorage.setItem('rememberMe', remember ? 'true' : 'false');
    
    return userData;
  }, []);

  // Logout function
  const logout = useCallback(async () => {
    try {
      await logoutUser();
    } catch (err) {
      // Log error but proceed with client-side logout
      console.error('Error during logout:', err);
    } finally {
      // Clear auth state
      setUser(null);
      setIsAuthenticated(false);
      setIsSessionExpired(false);
      setRememberMe(false);
      localStorage.removeItem('rememberMe');
    }
  }, []);

  // Handle session expiration
  const handleSessionExpired = useCallback(() => {
    setIsSessionExpired(true);
  }, []);

  // Context value to be provided
  const value = {
    user,
    loading,
    error,
    isAuthenticated,
    isSessionExpired,
    rememberMe,
    login,
    logout,
    handleSessionExpired,
    updateActivity,
    refreshUser: initAuth,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// Hook to use the auth context
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === null) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext; 