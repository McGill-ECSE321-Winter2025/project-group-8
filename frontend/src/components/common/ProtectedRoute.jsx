import React from 'react';
import { Navigate } from 'react-router-dom';

/**
 * A component that wraps routes requiring authentication.
 * Checks for the presence of an authentication token in localStorage.
 * If the token exists, it renders the child component (the protected page).
 * If the token does not exist, it redirects the user to the login page.
 *
 * @param {object} props - The component props.
 * @param {React.ReactNode} props.children - The child component to render if authenticated.
 */
const ProtectedRoute = ({ children }) => {
  const token = localStorage.getItem('token');

  if (!token) {
    // User not authenticated, redirect to login page
    // 'replace' prevents adding the login route to the history stack
    return <Navigate to="/login" replace />;
  }

  // User is authenticated, render the requested component
  return children;
};

export default ProtectedRoute;
