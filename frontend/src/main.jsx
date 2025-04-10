import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import { BrowserRouter } from "react-router-dom";
import '@fortawesome/fontawesome-free/css/all.min.css';
import './index.css'

// Import and initialize the time manipulation utility for testing
// This makes the utility accessible via Ctrl+Shift+Alt+T or by clicking the clock button
import { applyTimeOffset, initTimeManipulation } from './lib/timeManipulation.js';

// Initialize immediately to ensure it affects all date operations
if (typeof window !== 'undefined') {
  console.log('💡 TESTING UTILITY: Initializing time manipulation...');
  // First apply the time offset to all Date operations
  applyTimeOffset();
  
  // Then wait for DOM to be ready to add UI components
  if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', () => {
      console.log('💡 TESTING UTILITY: DOM ready, initializing time controls');
      initTimeManipulation();
    });
  } else {
    console.log('💡 TESTING UTILITY: DOM already ready, initializing time controls');
    initTimeManipulation();
  }
  
  // Add a small notification about how to use the utility
  setTimeout(() => {
    console.log('%c⏰ Time Manipulation Available', 'background: #2563eb; color: white; padding: 2px 5px; border-radius: 4px;');
    console.log('%c👉 Press Ctrl+Shift+Alt+T or click the clock button to open', 'font-style: italic; color: #555;');
  }, 1000);
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
)
