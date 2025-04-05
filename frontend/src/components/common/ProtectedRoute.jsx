import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext'; // Import useAuth hook

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
  const { user, loading } = useAuth(); // Get user state and loading status
  const location = useLocation(); // Get current location

  if (loading) {
    // Show a loading indicator while checking auth status
    // TODO: Replace with a proper loading spinner/component
    return <div>Loading...</div>;
  }

  if (!user) {
    // User not authenticated, redirect to login page
    // Pass the current location to redirect back after login (optional)
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // User is authenticated, render the requested component
  return children;
};

export default ProtectedRoute;
