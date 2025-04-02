import React from 'react';

/**
 * A reusable Tag component for displaying styled text labels.
 * @param {object} props - The component props.
 * @param {string} props.text - The text to display inside the tag.
 * @param {string} [props.color] - Optional prop to control color variants (not implemented yet).
 */
const Tag = ({ text, color }) => {
  // TODO: Implement color variants based on the 'color' prop
  const baseClasses = "inline-block px-2 py-0.5 rounded-full text-xs font-medium";
  const colorClasses = "bg-gray-200 text-gray-900"; // Default gray (updated)

  // Example of how color variants could be handled (currently commented out)
  // let colorClasses = "bg-gray-100 text-gray-800"; // Default
  // if (color === 'green') {
  //   colorClasses = "bg-green-100 text-green-800";
  // } else if (color === 'blue') {
  //   colorClasses = "bg-blue-100 text-blue-800";
  // } // Add more colors as needed

  return (
    <span className={`${baseClasses} ${colorClasses}`}>
      {text}
    </span>
  );
};

export default Tag;