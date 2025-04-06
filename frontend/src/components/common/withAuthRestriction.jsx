import React from 'react';
import { useAuth } from '@/context/AuthContext';
import GuestGameOverlay from './GuestGameOverlay';

/**
 * Higher-order component that adds authentication restrictions to components.
 * If user is not authenticated, it shows an overlay prompting them to sign up.
 * 
 * @param {React.ComponentType} Component - The component to wrap
 * @param {Object} options - Configuration options
 * @param {boolean} options.interactive - Whether the component should be interactive for guests
 * @returns {React.FC} - The wrapped component with auth restrictions
 */
const withAuthRestriction = (Component, { interactive = false } = {}) => {
  // Return a new component with the same props
  return function WithAuthRestriction(props) {
    const { isAuthenticated, authInitialized } = useAuth();
    
    // If auth not initialized yet or component should be interactive, render normally
    if (!authInitialized || interactive) {
      return <Component {...props} />;
    }
    
    // For authenticated users, just render the component normally
    if (isAuthenticated) {
      return <Component {...props} />;
    }
    
    // For unauthenticated users, wrap with overlay and make non-interactive
    return (
      <div 
        className="relative" 
        onClick={(e) => {
          // Prevent click events from bubbling up to parent elements
          e.preventDefault();
          e.stopPropagation();
        }}
      >
        <div className="pointer-events-none">
          <Component {...props} />
        </div>
        <GuestGameOverlay title={props.title || props.name} />
      </div>
    );
  };
};

export default withAuthRestriction; 