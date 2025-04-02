import React from 'react';
import { cn } from '../../../src/lib/utils'; // Assuming you have the cn utility from shadcn/ui

/**
 * A reusable Tag component for displaying styled text labels with animations and color variants.
 * @param {object} props - The component props.
 * @param {string} props.text - The text to display inside the tag.
 * @param {string} [props.variant] - Color variant: 'default', 'primary', 'secondary', 'success', 'warning', 'danger'.
 * @param {string} [props.className] - Additional classes to apply.
 * @param {boolean} [props.interactive] - Whether the tag should have hover/focus states.
 */
const Tag = ({ 
  text, 
  variant = 'default', 
  className,
  interactive = false,
  ...props 
}) => {
  const baseClasses = "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium transition-all duration-200";
  
  const variantClasses = {
    default: "bg-gray-100 text-gray-800 hover:bg-gray-200",
    primary: "bg-primary/10 text-primary hover:bg-primary/20",
    secondary: "bg-secondary/10 text-secondary hover:bg-secondary/20",
    success: "bg-green-100 text-green-800 hover:bg-green-200",
    warning: "bg-amber-100 text-amber-800 hover:bg-amber-200",
    danger: "bg-red-100 text-red-800 hover:bg-red-200",
    owner: "bg-purple-100 text-purple-800 hover:bg-purple-200",
  };

  // Apply the appropriate variant class, and add scale effect if interactive
  const classes = cn(
    baseClasses,
    variantClasses[variant] || variantClasses.default,
    interactive && "transform hover:scale-105 active:scale-95",
    className
  );

  return (
    <span className={classes} {...props}>
      {text}
    </span>
  );
};

export default Tag;