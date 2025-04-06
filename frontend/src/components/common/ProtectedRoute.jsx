import React, { useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext'; // Import useAuth hook
import { Loader2 } from 'lucide-react';

/**
 * A component that wraps routes requiring authentication.
 * Checks the authentication state from AuthContext.
 * If the user is authenticated, it renders the child component.
 * If the user is not authenticated, it redirects to the login page.
 * Handles the initial loading state while authentication status is being checked.
 *
 * @param {object} props - The component props.
 * @param {React.ReactNode} props.children - The child component to render if authenticated.
 */
const ProtectedRoute = ({ children }) => {
  const { user, isAuthenticated, loading, authInitialized, checkAuthStatus, connectionError } = useAuth();
  const location = useLocation();
  const [localLoading, setLocalLoading] = useState(true);
  const [retryCount, setRetryCount] = useState(0);
  
  // Maximum number of times to retry auth check on page load
  const MAX_RETRIES = 3;
  const RETRY_DELAY = 1000; // 1 second delay between retries

  // Effect to log state for debugging
  useEffect(() => {
    console.log('ProtectedRoute state:', { 
      path: location.pathname,
      isAuthenticated, 
      loading, 
      localLoading,
      authInitialized,
      hasUser: !!user,
      retryCount,
      connectionError,
      hasStoredUser: !!localStorage.getItem('boardgame_connect_user'),
      hasCookie: document.cookie.includes('accessToken')
    });
  }, [isAuthenticated, loading, localLoading, authInitialized, location.pathname, user, retryCount, connectionError]);

  // Effect to perform an extra auth check on initial component mount
  // This helps with page reloads
  useEffect(() => {
    let timeoutId;
    
    const verifyAuthentication = async () => {
      try {
        // Only retry if not yet authenticated and we haven't exceeded max retries
        if (!isAuthenticated && retryCount < MAX_RETRIES) {
          console.log(`ProtectedRoute: Verifying authentication (attempt ${retryCount + 1})`);
          const result = await checkAuthStatus();
          
          if (!result && retryCount < MAX_RETRIES - 1) {
            console.log(`ProtectedRoute: Auth check failed, scheduling retry in ${RETRY_DELAY}ms`);
            // Schedule next retry with a delay
            timeoutId = setTimeout(() => {
              setRetryCount(prev => prev + 1);
            }, RETRY_DELAY);
            return; // Exit early, we'll try again
          }
          
          // Either we succeeded or we're out of retries
          setRetryCount(prev => prev + 1);
        }
      } catch (error) {
        console.error('ProtectedRoute: Error checking auth status:', error);
      } finally {
        // Set local loading to false after auth check attempts are complete
        if (retryCount >= MAX_RETRIES - 1) {
          setLocalLoading(false);
        }
      }
    };

    // Check if we have a stored user but auth says we're not authenticated
    const hasStoredUser = !!localStorage.getItem('boardgame_connect_user');
    
    // If auth is initialized but no user, or we have a stored user but auth says we're not authenticated
    if ((authInitialized && !isAuthenticated) || (!isAuthenticated && hasStoredUser)) {
      verifyAuthentication();
    } else {
      setLocalLoading(false);
    }
    
    // Clean up timeout if component unmounts
    return () => {
      if (timeoutId) {
        clearTimeout(timeoutId);
      }
    };
  }, [isAuthenticated, authInitialized, checkAuthStatus, retryCount]);

  // Show loading state when either global or local loading is true
  if ((loading && !authInitialized) || localLoading) {
    console.log('ProtectedRoute: Still loading auth status');
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="flex flex-col items-center space-y-4">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
          <p className="text-lg text-muted-foreground">Verifying your session...</p>
        </div>
      </div>
    );
  }

  // Show connection error message
  if (connectionError) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="max-w-md p-6 bg-destructive/10 rounded-lg border border-destructive/20">
          <h2 className="text-xl font-semibold mb-2">Connection Error</h2>
          <p className="mb-4">Unable to connect to the server. Please check your internet connection and try again.</p>
          <button 
            className="px-4 py-2 bg-primary text-primary-foreground rounded-md"
            onClick={() => window.location.reload()}
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    // User not authenticated, redirect to login page
    console.log('ProtectedRoute: Not authenticated, redirecting to login');
    // Pass the current location to redirect back after login (optional)
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // User is authenticated, render the requested component
  console.log('ProtectedRoute: User authenticated, rendering protected content');
  return children;
};

export default ProtectedRoute;
