import { clsx } from "clsx"
import { twMerge } from "tailwind-merge"

/**
 * A utility function for conditionally joining class names together.
 * It uses clsx to conditionally merge classes and tailwind-merge to handle
 * Tailwind CSS class conflicts.
 *
 * @param {...any} inputs - Class names or conditional objects
 * @returns {string} - Merged class names
 */
export function cn(...inputs) {
  return twMerge(clsx(inputs))
} 