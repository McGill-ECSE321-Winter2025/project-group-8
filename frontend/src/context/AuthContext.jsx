import React, { createContext, useState, useContext, useEffect, useCallback } from 'react';
import { checkAuthStatus, getUserProfile, logoutUser } from '../service/auth-api';
import { setAuthInProgress, getCookieAuthState } from '../service/apiClient';

// Create the auth context
const AuthContext = createContext(null);

// Public paths that don't need authenticated state
const PUBLIC_PATHS = ['/', '/login', '/register', '/about', '/contact'];

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
  // Auth ready state - indicates when authentication process is fully complete
  const [authReady, setAuthReady] = useState(false);
  // Error state
  const [error, setError] = useState(null);
  // Session expired flag
  const [isSessionExpired, setIsSessionExpired] = useState(false);
  // Remember me state
  const [rememberMe, setRememberMe] = useState(false);
  // Last activity timestamp
  const [lastActivity, setLastActivity] = useState(Date.now());
  // Current path
  const [currentPath, setCurrentPath] = useState(window.location.pathname);

  // Update current path when location changes
  useEffect(() => {
    const handleLocationChange = () => {
      setCurrentPath(window.location.pathname);
    };
    
    window.addEventListener('popstate', handleLocationChange);
    
    return () => {
      window.removeEventListener('popstate', handleLocationChange);
    };
  }, []);

  // Check if the current path is public
  const isPublicPath = PUBLIC_PATHS.includes(currentPath);

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
      setAuthReady(false);
      setAuthInProgress(true); // Set auth in progress flag
      setError(null);

      // First check the cookie auth state
      const cookieAuth = getCookieAuthState();
      console.log('Cookie auth state on init:', cookieAuth);
      
      // Fix inconsistent cookie state if found
      if ((cookieAuth.isAuthenticated && !cookieAuth.hasAccessToken) || 
          (!cookieAuth.isAuthenticated && cookieAuth.hasAccessToken)) {
        // Attempt to repair inconsistent cookie state
        console.log('Fixing inconsistent cookie state');
        if (cookieAuth.isAuthenticated || cookieAuth.hasAccessToken) {
          const cookieOptions = 'path=/; max-age=86400; SameSite=Lax'; // 24 hours
          document.cookie = `isAuthenticated=true; ${cookieOptions}`;
          document.cookie = `hasAccessToken=true; ${cookieOptions}`;
        }
      }
      
      // Check if user data exists in localStorage
      const userId = localStorage.getItem('userId');
      const userEmail = localStorage.getItem('userEmail');
      const userJson = localStorage.getItem('user');
      
      // Check if we have cookies for authentication or localStorage data
      if ((cookieAuth.isAuthenticated || cookieAuth.hasAccessToken) || (userId && userEmail && userJson)) {
        let userData = null;
        
        // Try to parse user data from localStorage if it exists
        if (userJson) {
          try {
            userData = JSON.parse(userJson);
          } catch (e) {
            console.error('Error parsing user data from localStorage', e);
            // We'll continue and fetch user data from the API if needed
          }
        }
        
        // Verify token with server
        try {
          const isAuth = await checkAuthStatus();
          if (isAuth) {
            // If we don't have userData or it's incomplete, fetch user profile
            if (!userData || !userData.id || !userData.email) {
              try {
                userData = await getUserProfile();
                
                // Update localStorage with fresh data
                if (userData && userData.id && userData.email) {
                  localStorage.setItem('userId', userData.id);
                  localStorage.setItem('userEmail', userData.email);
                  localStorage.setItem('user', JSON.stringify(userData));
                }
              } catch (profileError) {
                console.error('Error fetching user profile:', profileError);
                // Continue with whatever user data we have
              }
            }
            
            // Token is valid, set user as authenticated
            setUser(userData);
            setIsAuthenticated(true);
            setIsSessionExpired(false);
            
            // Check if remember me was set
            const savedRememberMe = localStorage.getItem('rememberMe') === 'true';
            setRememberMe(savedRememberMe);
            
            // If cookie auth but no local storage data, update localStorage
            if (cookieAuth.isAuthenticated && userData && (!userId || !userEmail)) {
              localStorage.setItem('userId', userData.id);
              localStorage.setItem('userEmail', userData.email);
              localStorage.setItem('user', JSON.stringify(userData));
            }
          } else {
            // If server says not authenticated, clear state
            clearAuthData();
          }
        } catch (error) {
          console.error('Error checking auth status:', error);
          // For 401 errors, clear auth
          if (error.status === 401) {
            clearAuthData();
          } else if (cookieAuth.isAuthenticated && userData) {
            // For network errors with cookie auth, keep user logged in
            setUser(userData);
            setIsAuthenticated(true);
          } else {
            // For other errors without cookie auth, clear auth
            clearAuthData();
          }
        }
      } else {
        // No auth data in localStorage or cookies, check with server
        try {
          const isAuth = await checkAuthStatus();
          
          if (isAuth) {
            // Fetch user profile if authenticated
            const userProfile = await getUserProfile();
            setUser(userProfile);
            setIsAuthenticated(true);
            setIsSessionExpired(false);

            // Store user data in localStorage
            if (userProfile && userProfile.id && userProfile.email) {
              localStorage.setItem('userId', userProfile.id);
              localStorage.setItem('userEmail', userProfile.email);
              localStorage.setItem('user', JSON.stringify(userProfile));
            }

            // Check if remember me was set
            const savedRememberMe = localStorage.getItem('rememberMe') === 'true';
            setRememberMe(savedRememberMe);
          } else {
            // Clear authentication state
            clearAuthData();
          }
        } catch (error) {
          console.error('Error checking auth status during init:', error);
          clearAuthData();
        }
      }
    } catch (err) {
      console.error('Error initializing auth:', err);
      setError('Failed to initialize authentication');
      clearAuthData();
    } finally {
      setLoading(false);
      
      // Add a small delay before setting authReady to true
      // This gives the browser time to process state updates
      setTimeout(() => {
        setAuthReady(true); // Authentication flow is now complete
        setAuthInProgress(false); // Auth is no longer in progress
      }, 10); // Reduced delay
    }
  }, []);
  
  // Helper function to clear auth data
  const clearAuthData = useCallback(() => {
    setUser(null);
    setIsAuthenticated(false);
    localStorage.removeItem('rememberMe');
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('user');
    // Also set the isAuthenticated cookie to false
    document.cookie = "isAuthenticated=false; path=/";
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
    try {
      setLoading(true);
      setAuthReady(false);
      setAuthInProgress(true); // Set auth in progress flag
      
      setUser(userData);
      setIsAuthenticated(true);
      setIsSessionExpired(false);
      setRememberMe(remember);
      setLastActivity(Date.now());
      
      // Check if we have a token in the userData, which should come from the API response
      const token = userData.token || userData.accessToken;
      
      // Save user data and token to localStorage
      if (userData) {
        if (token) {
          localStorage.setItem('token', token);
        }
        localStorage.setItem('userId', userData.id);
        localStorage.setItem('userEmail', userData.email);
        localStorage.setItem('user', JSON.stringify(userData));
      }
      
      // Save remember me preference
      localStorage.setItem('rememberMe', remember ? 'true' : 'false');
      
      // Verify authentication status after login
      // Removed redundant checkAuthStatus() call after successful login
      
      // Add a small delay before setting authReady to true
      // This ensures the browser has time to process the login
      return new Promise(resolve => {
        setTimeout(() => {
          setLoading(false);
          setAuthReady(true); // Authentication flow is now complete
          setAuthInProgress(false); // Auth is no longer in progress
          resolve(userData);
        }, 10); // Reduced delay
      });
    } catch (error) {
      console.error('Error during login:', error);
      setLoading(false);
      setAuthReady(true);
      setAuthInProgress(false);
      throw error;
    }
  }, []);

  // Logout function
  const logout = useCallback(async () => {
    try {
      setAuthReady(false);
      setAuthInProgress(true); // Set auth in progress flag
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
      
      // Clear all localStorage items
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('user');
      localStorage.removeItem('rememberMe');
      
      // Add a small delay for state updates to propagate
      setTimeout(() => {
        setAuthReady(true);
        setAuthInProgress(false); // Auth is no longer in progress
      }, 10); // Reduced delay
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
    isAuthenticated,
    isSessionExpired,
    error,
    authReady,
    login,
    logout,
    handleSessionExpired,
    rememberMe,
    setRememberMe,
    updateActivity,
    isPublicPath
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