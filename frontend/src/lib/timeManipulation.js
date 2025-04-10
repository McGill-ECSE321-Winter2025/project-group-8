/**
 * Hidden Time Manipulation Utility for Testing
 * Allows testers to manipulate the perceived current time by applying an offset
 */

// Import the formatDateTimeForDisplay function
import { formatDateTimeForDisplay } from './dateUtils';

// Time offset in milliseconds (0 means no offset - using real time)
let timeOffset = 0;

// Store the original Date constructor
const OriginalDate = window.Date;

// Store original fetch function
const originalFetch = window.fetch;

// Store original XMLHttpRequest prototype methods
const originalXHROpen = XMLHttpRequest.prototype.open;
const originalXHRSend = XMLHttpRequest.prototype.send;

/**
 * Apply the time offset to the current system
 * This overrides the Date constructor to return dates with the applied offset
 */
const applyTimeOffset = () => {
  // Override the global Date constructor
  window.Date = function(...args) {
    // If no arguments, use current time + offset
    if (args.length === 0) {
      const date = new OriginalDate();
      date.setTime(date.getTime() + timeOffset);
      return date;
    }
    // Otherwise use the provided arguments
    return new OriginalDate(...args);
  };

  // Copy all properties from the original Date constructor
  Object.setPrototypeOf(window.Date, OriginalDate);
  window.Date.prototype = OriginalDate.prototype;
  
  // Override Date instance methods to handle offset
  const originalGetTime = OriginalDate.prototype.getTime;
  Date.prototype.getTime = function() {
    // For dates created with no arguments (current time), add offset
    if (this._isCurrentTimeDate) {
      return originalGetTime.call(this) + timeOffset;
    }
    return originalGetTime.call(this);
  };
  
  // Make sure static methods like Date.now() are also affected
  window.Date.now = function() {
    return OriginalDate.now() + timeOffset;
  };
  
  // Keep UTC methods intact
  window.Date.UTC = OriginalDate.UTC;
  window.Date.parse = OriginalDate.parse;
  
  // Monkey patch the fetch API to inject our custom time header
  window.fetch = function(...args) {
    const [resource, config = {}] = args;
    
    // Create a new config object to avoid mutating the original
    const newConfig = { ...config };
    
    // Ensure headers exist
    if (!newConfig.headers) {
      newConfig.headers = {};
    } else if (newConfig.headers instanceof Headers) {
      // Convert Headers object to plain object for easier manipulation
      const headersObj = {};
      for (const [key, value] of newConfig.headers.entries()) {
        headersObj[key] = value;
      }
      newConfig.headers = headersObj;
    }
    
    // Add our custom time header
    newConfig.headers['X-Test-Time-Offset'] = timeOffset.toString();
    newConfig.headers['X-Test-Current-Time'] = new Date().toISOString();
    
    // Process JSON request body if it exists
    if (newConfig.body && typeof newConfig.body === 'string') {
      try {
        // Try to parse as JSON
        const jsonBody = JSON.parse(newConfig.body);
        // Modify any date fields in the JSON payload
        modifyDateFields(jsonBody);
        // Update the body with modified dates
        newConfig.headers['Content-Type'] = 'application/json';
        newConfig.body = JSON.stringify(jsonBody);
      } catch (e) {
        // Not JSON, ignore
        console.debug('[TEST UTILITY] Request body is not JSON, skipping date manipulation');
      }
    }
    
    // For diagnostic purposes, log API requests in development
    if (process.env.NODE_ENV !== 'production') {
      console.debug(
        `[TEST UTILITY] Fetch request to ${resource} with time offset ${timeOffset}ms`,
        { url: resource, headers: newConfig.headers, method: newConfig.method || 'GET' }
      );
    }
    
    // Call the original fetch with our modified config
    const fetchPromise = originalFetch.call(window, resource, newConfig);
    
    // Return a promise that adds some diagnostic information
    return fetchPromise.then(response => {
      // Check if there's a header to confirm time manipulation
      const timeHeader = response.headers.get('X-Test-Time-Processed');
      if (timeHeader && process.env.NODE_ENV !== 'production') {
        console.debug(
          `[TEST UTILITY] Server processed time manipulation for ${resource}`, 
          { processed: true, timeOffset }
        );
      }
      return response;
    }).catch(error => {
      // Log fetch errors with more context in development
      if (process.env.NODE_ENV !== 'production') {
        console.error(
          `[TEST UTILITY] Fetch error for ${resource}`, 
          { url: resource, error, timeOffset }
        );
      }
      throw error;
    });
  };
  
  // Monkey patch XMLHttpRequest to inject time headers
  XMLHttpRequest.prototype.open = function(...args) {
    // Store the method and URL for later use when sending
    this._method = args[0];
    this._url = args[1];
    
    // Store original headers for diagnostic purposes
    this._originalHeaders = new Map();
    
    // Adding custom properties to track if time headers are added
    this._timeHeadersAdded = false;
    
    // Call original open
    return originalXHROpen.apply(this, args);
  };
  
  // Store the original setRequestHeader method
  const originalSetRequestHeader = XMLHttpRequest.prototype.setRequestHeader;

  // Override setRequestHeader to track headers
  XMLHttpRequest.prototype.setRequestHeader = function(header, value) {
    // Store the original header for diagnostic purposes
    this._originalHeaders.set(header.toLowerCase(), value);
    
    // Call the original method
    return originalSetRequestHeader.apply(this, arguments);
  };

  XMLHttpRequest.prototype.send = function(body) {
    try {
      // Add custom time headers if not already added
      if (!this._timeHeadersAdded) {
        this.setRequestHeader('X-Test-Time-Offset', timeOffset.toString());
        this.setRequestHeader('X-Test-Current-Time', new Date().toISOString());
        this._timeHeadersAdded = true;
      }
      
      // Process JSON request body if it exists
      let modifiedBody = body;
      if (body && typeof body === 'string') {
        try {
          // Try to parse as JSON
          const jsonBody = JSON.parse(body);
          // Modify any date fields in the JSON payload
          modifyDateFields(jsonBody);
          // Update the body with modified dates
          modifiedBody = JSON.stringify(jsonBody);
          
          // Ensure content type is set for JSON
          if (!this._originalHeaders.has('content-type')) {
            this.setRequestHeader('Content-Type', 'application/json');
          }
        } catch (e) {
          // Not JSON, ignore
          console.debug('[TEST UTILITY] XHR body is not JSON, skipping date manipulation');
        }
      }
      
      // For diagnostic purposes, log API requests in development
      if (process.env.NODE_ENV !== 'production') {
        console.debug(
          `[TEST UTILITY] XHR request to ${this._url} with time offset ${timeOffset}ms`,
          { method: this._method, url: this._url, timeOffset }
        );
      }
      
      // Listen for load event to check response headers
      if (!this._loadListenerAdded) {
        this.addEventListener('load', () => {
          const timeHeader = this.getResponseHeader('X-Test-Time-Processed');
          if (timeHeader && process.env.NODE_ENV !== 'production') {
            console.debug(
              `[TEST UTILITY] Server processed time manipulation for XHR to ${this._url}`,
              { processed: true, timeOffset }
            );
          }
        });
        
        // Listen for error event for better diagnostics
        this.addEventListener('error', () => {
          if (process.env.NODE_ENV !== 'production') {
            console.error(
              `[TEST UTILITY] XHR error for ${this._url}`,
              { method: this._method, url: this._url, timeOffset, status: this.status }
            );
          }
        });
        
        this._loadListenerAdded = true;
      }
      
      // Call original send with potentially modified body
      return originalXHRSend.call(this, modifiedBody);
    } catch (error) {
      console.error('[TEST UTILITY] Error in XHR send override:', error);
      // Fallback to original behavior
      return originalXHRSend.call(this, body);
    }
  };
  
  console.log(`[TEST UTILITY] Time offset applied: ${formatTimeOffset(timeOffset)}`);
};

/**
 * Recursively search for date strings in an object and modify them
 * @param {Object} obj - The object to modify
 */
const modifyDateFields = (obj) => {
  if (!obj || typeof obj !== 'object') return;
  
  // ISO date regex pattern
  const isoDatePattern = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(\.\d{3})?Z$/;
  
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      const value = obj[key];
      
      // If it's a string that looks like an ISO date
      if (typeof value === 'string' && isoDatePattern.test(value)) {
        // Parse the date, add the offset, and convert back to ISO string
        const date = new OriginalDate(value);
        date.setTime(date.getTime() + timeOffset);
        obj[key] = date.toISOString();
      } 
      // If it's an array or object, recurse
      else if (typeof value === 'object' && value !== null) {
        modifyDateFields(value);
      }
    }
  }
};

/**
 * Reset the time offset and restore the original Date implementation
 */
const resetTimeOffset = () => {
  timeOffset = 0;
  window.Date = OriginalDate;
  window.fetch = originalFetch;
  XMLHttpRequest.prototype.open = originalXHROpen;
  XMLHttpRequest.prototype.send = originalXHRSend;
  console.log("[TEST UTILITY] Time offset reset - using real time");
};

/**
 * Set a specific time offset in milliseconds
 * @param {number} offsetMs - Offset in milliseconds
 */
const setTimeOffset = (offsetMs) => {
  timeOffset = offsetMs;
  applyTimeOffset();
};

/**
 * Format a time offset in milliseconds to a human-readable string
 * @param {number} offsetMs - Offset in milliseconds
 * @returns {string} Formatted time offset
 */
const formatTimeOffset = (offsetMs) => {
  const seconds = Math.floor(Math.abs(offsetMs) / 1000) % 60;
  const minutes = Math.floor(Math.abs(offsetMs) / (1000 * 60)) % 60;
  const hours = Math.floor(Math.abs(offsetMs) / (1000 * 60 * 60)) % 24;
  const days = Math.floor(Math.abs(offsetMs) / (1000 * 60 * 60 * 24));
  
  let result = '';
  if (offsetMs < 0) result += '-';
  if (days !== 0) result += `${days}d `;
  if (hours !== 0) result += `${hours}h `;
  if (minutes !== 0) result += `${minutes}m `;
  result += `${seconds}s`;
  
  return result;
};

// Initialize the keyboard shortcut listener for time manipulation
const initTimeManipulation = () => {
  if (typeof window === 'undefined') return;
  
  // Always enable in localhost environments, regardless of NODE_ENV
  const isLocalEnvironment = window.location.hostname === 'localhost' || 
                            window.location.hostname === '127.0.0.1' ||
                            window.location.hostname === '';
  
  // Skip initialization in production environments that aren't localhost
  if (process.env.NODE_ENV === 'production' && !isLocalEnvironment) {
    console.log('[TEST UTILITY] Time manipulation disabled in production');
    return;
  }
  
  // Initialize with a zero offset to make sure our Date override is active
  applyTimeOffset();
  
  // Use direct key detection on keydown instead of tracking modifier states
  window.addEventListener('keydown', (e) => {
    // Check if Ctrl+Shift+Alt+T is pressed
    if (e.ctrlKey && e.shiftKey && e.altKey && (e.key === 't' || e.key === 'T')) {
      console.log('[TEST UTILITY] Opening time manipulation panel');
      toggleTimeManipulationPanel();
      // Prevent default browser behavior
      e.preventDefault();
    }
  });
  
  // Add a global message to make the shortcut visible
  console.log('[TEST UTILITY] Time manipulation available via Ctrl+Shift+Alt+T');
  
  // Wait for DOM to be ready before adding UI elements
  const addTimeControlUI = () => {
    // Create a small button that is more visible than just an indicator
    const toggleButton = document.createElement('button');
    toggleButton.id = 'time-manipulation-toggle';
    toggleButton.textContent = '🕒';
    toggleButton.style.position = 'fixed';
    toggleButton.style.bottom = '10px';
    toggleButton.style.right = '10px';
    toggleButton.style.width = '30px';
    toggleButton.style.height = '30px';
    toggleButton.style.background = '#2563eb';
    toggleButton.style.color = 'white';
    toggleButton.style.border = 'none';
    toggleButton.style.borderRadius = '50%';
    toggleButton.style.zIndex = '10000';
    toggleButton.style.cursor = 'pointer';
    toggleButton.style.fontSize = '16px';
    toggleButton.style.display = 'flex';
    toggleButton.style.alignItems = 'center';
    toggleButton.style.justifyContent = 'center';
    toggleButton.style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.2)';
    toggleButton.title = 'Toggle Time Manipulation Panel (Ctrl+Shift+Alt+T)';
    
    // Add click event to toggle the panel
    toggleButton.addEventListener('click', toggleTimeManipulationPanel);
    
    document.body.appendChild(toggleButton);
    
    // Create a small indicator light to show current status
    const indicator = document.createElement('div');
    indicator.id = 'time-manipulation-indicator';
    indicator.style.position = 'fixed';
    indicator.style.bottom = '43px';
    indicator.style.right = '10px';
    indicator.style.width = '10px';
    indicator.style.height = '10px';
    indicator.style.background = timeOffset !== 0 ? '#dc2626' : '#10b981'; // Red when offset, green when real time
    indicator.style.borderRadius = '50%';
    indicator.style.zIndex = '10000';
    indicator.style.opacity = '0.9';
    
    document.body.appendChild(indicator);
  };
  
  // Wait for DOM to be fully loaded
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', addTimeControlUI);
  } else {
    addTimeControlUI();
  }
};

// Create a simple UI panel for time manipulation
const toggleTimeManipulationPanel = () => {
  let panel = document.getElementById('time-manipulation-panel');
  
  if (panel) {
    panel.remove();
    return;
  }
  
  panel = document.createElement('div');
  panel.id = 'time-manipulation-panel';
  panel.style.position = 'fixed';
  panel.style.bottom = '50px';
  panel.style.right = '20px';
  panel.style.width = '300px';
  panel.style.background = '#1e293b';
  panel.style.padding = '15px';
  panel.style.borderRadius = '8px';
  panel.style.color = 'white';
  panel.style.fontFamily = 'system-ui, sans-serif';
  panel.style.zIndex = '10000';
  panel.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.5)';
  
  // Create a header with close button
  const header = document.createElement('div');
  header.style.display = 'flex';
  header.style.justifyContent = 'space-between';
  header.style.alignItems = 'center';
  header.style.marginBottom = '10px';
  
  const title = document.createElement('h3');
  title.textContent = 'Time Manipulation';
  title.style.margin = '0';
  title.style.fontSize = '16px';
  title.style.fontWeight = 'bold';
  
  const closeButton = document.createElement('button');
  closeButton.textContent = '×';
  closeButton.style.background = 'transparent';
  closeButton.style.border = 'none';
  closeButton.style.color = 'white';
  closeButton.style.fontSize = '20px';
  closeButton.style.cursor = 'pointer';
  closeButton.style.padding = '0 5px';
  closeButton.style.lineHeight = '1';
  
  closeButton.addEventListener('click', () => {
    panel.remove();
  });
  
  header.appendChild(title);
  header.appendChild(closeButton);
  panel.appendChild(header);
  
  // Current time display
  const currentTimeDisplay = document.createElement('div');
  currentTimeDisplay.style.marginBottom = '10px';
  currentTimeDisplay.style.fontSize = '12px';
  panel.appendChild(currentTimeDisplay);
  
  // Current offset display
  const offsetDisplay = document.createElement('div');
  offsetDisplay.textContent = `Current offset: ${formatTimeOffset(timeOffset)}`;
  offsetDisplay.style.marginBottom = '10px';
  offsetDisplay.style.fontSize = '12px';
  panel.appendChild(offsetDisplay);
  
  // Backend status indicator
  const backendStatus = document.createElement('div');
  backendStatus.style.display = 'flex';
  backendStatus.style.alignItems = 'center';
  backendStatus.style.gap = '5px';
  backendStatus.style.marginBottom = '10px';
  backendStatus.style.fontSize = '12px';
  
  const backendStatusDot = document.createElement('span');
  backendStatusDot.style.width = '8px';
  backendStatusDot.style.height = '8px';
  backendStatusDot.style.borderRadius = '50%';
  backendStatusDot.style.background = '#999';
  backendStatusDot.style.display = 'inline-block';
  
  const backendStatusText = document.createElement('span');
  backendStatusText.textContent = 'Backend status: Unknown';
  
  backendStatus.appendChild(backendStatusDot);
  backendStatus.appendChild(backendStatusText);
  panel.appendChild(backendStatus);
  
  // Show warning for authentication issues when changing time
  const authWarning = document.createElement('div');
  authWarning.style.marginTop = '10px';
  authWarning.style.marginBottom = '10px';
  authWarning.style.fontSize = '11px';
  authWarning.style.padding = '8px';
  authWarning.style.background = 'rgba(234, 179, 8, 0.2)';
  authWarning.style.borderRadius = '4px';
  authWarning.style.color = '#eab308';
  authWarning.style.display = 'none';
  authWarning.innerHTML = `
    <strong>⚠️ Auth Warning:</strong> Time changes may affect authentication tokens. 
    If API calls fail, refresh your auth below.
  `;
  panel.appendChild(authWarning);

  // Add refresh auth button
  const refreshAuthButton = document.createElement('button');
  refreshAuthButton.textContent = '🔄 Refresh Authentication';
  refreshAuthButton.style.background = '#eab308';
  refreshAuthButton.style.border = 'none';
  refreshAuthButton.style.color = 'white';
  refreshAuthButton.style.padding = '8px';
  refreshAuthButton.style.borderRadius = '4px';
  refreshAuthButton.style.cursor = 'pointer';
  refreshAuthButton.style.width = '100%';
  refreshAuthButton.style.marginTop = '10px';
  refreshAuthButton.style.display = 'none';

  refreshAuthButton.addEventListener('click', () => {
    // Try to find the auth refresh function - common names in auth systems
    let authRefreshFunction = null;
    
    // Check for common auth refresh patterns
    if (window.authService && typeof window.authService.refreshToken === 'function') {
      authRefreshFunction = window.authService.refreshToken;
    } else if (window.auth && typeof window.auth.refresh === 'function') {
      authRefreshFunction = window.auth.refresh;
    } else if (window.refreshAuth && typeof window.refreshAuth === 'function') {
      authRefreshFunction = window.refreshAuth;
    } else {
      // Look for refresh token in localStorage
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        // Try to construct a basic refresh request
        fetch('/api/auth/refresh', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'x-refresh-token': refreshToken
          }
        })
        .then(response => {
          if (response.ok) {
            console.log('[TEST UTILITY] Auth refresh successful');
            backendStatusText.textContent = 'Auth refreshed successfully';
            // Reload the page to ensure clean state
            setTimeout(() => window.location.reload(), 1000);
          } else {
            console.error('[TEST UTILITY] Auth refresh failed');
            backendStatusText.textContent = 'Auth refresh failed - try logging out/in';
          }
        })
        .catch(error => {
          console.error('[TEST UTILITY] Auth refresh error:', error);
          backendStatusText.textContent = 'Auth refresh error - try logging out/in';
        });
        return;
      }
      
      // Fallback - attempt to reload the page to force reauthentication
      console.log('[TEST UTILITY] No auth refresh function found, reloading page');
      window.location.reload();
      return;
    }
    
    // Execute the found refresh function
    try {
      authRefreshFunction()
        .then(() => {
          console.log('[TEST UTILITY] Auth refresh successful');
          backendStatusText.textContent = 'Auth refreshed successfully';
        })
        .catch(error => {
          console.error('[TEST UTILITY] Auth refresh error:', error);
          backendStatusText.textContent = 'Auth refresh error - try logging out/in';
        });
    } catch (error) {
      console.error('[TEST UTILITY] Auth refresh execution error:', error);
      backendStatusText.textContent = 'Auth refresh error - try logging out/in';
    }
  });

  panel.appendChild(refreshAuthButton);

  // Show warning if time offset is more than 5 minutes
  const updateAuthWarning = (offset) => {
    const offsetAbs = Math.abs(offset);
    if (offsetAbs > 5 * 60 * 1000) { // 5 minutes
      authWarning.style.display = 'block';
      refreshAuthButton.style.display = 'block';
    } else {
      authWarning.style.display = 'none';
      refreshAuthButton.style.display = 'none';
    }
  };

  // Initial check
  updateAuthWarning(timeOffset);
  
  // Check backend time handling function
  const checkBackendTimeHandling = () => {
    // Make a small test request to see if time headers are being processed
    fetch('/api/server-time', {
      method: 'GET',
      headers: {
        'X-Test-Time-Offset': timeOffset.toString(),
        'X-Test-Current-Time': new Date().toISOString()
      }
    })
    .then(response => {
      if (response.ok) {
        // Check if there's a header to confirm time manipulation
        const timeHeader = response.headers.get('X-Test-Time-Processed');
        if (timeHeader) {
          backendStatusDot.style.background = '#10b981'; // Green
          backendStatusText.textContent = 'Backend: Time manipulation active';
        } else {
          // Backend is accessible but not handling time headers
          backendStatusDot.style.background = '#eab308'; // Yellow
          backendStatusText.textContent = 'Backend: Connected, not handling time';
        }
        return response.text();
      } else {
        throw new Error('Failed to connect to backend');
      }
    })
    .catch(error => {
      // Red to indicate errors
      backendStatusDot.style.background = '#dc2626';
      backendStatusText.textContent = 'Backend: Not connected';
      console.log('[TEST UTILITY] Backend connectivity error:', error);
    });
  };
  
  // Run the check immediately and then periodically
  checkBackendTimeHandling();
  const backendCheckInterval = setInterval(checkBackendTimeHandling, 10000);
  
  // Preset buttons container
  const presetContainer = document.createElement('div');
  presetContainer.style.display = 'flex';
  presetContainer.style.flexWrap = 'wrap';
  presetContainer.style.gap = '5px';
  presetContainer.style.marginBottom = '10px';
  panel.appendChild(presetContainer);
  
  // Create preset buttons
  const presets = [
    { label: 'Now', offset: 0 },
    { label: '+1h', offset: 60 * 60 * 1000 },
    { label: '+1d', offset: 24 * 60 * 60 * 1000 },
    { label: '+1w', offset: 7 * 24 * 60 * 60 * 1000 },
    { label: '-1h', offset: -60 * 60 * 1000 },
    { label: '-1d', offset: -24 * 60 * 60 * 1000 }
  ];
  
  presets.forEach(preset => {
    const button = document.createElement('button');
    button.textContent = preset.label;
    button.style.background = '#2563eb';
    button.style.border = 'none';
    button.style.color = 'white';
    button.style.padding = '5px 8px';
    button.style.borderRadius = '4px';
    button.style.cursor = 'pointer';
    button.style.fontSize = '12px';
    
    button.addEventListener('click', () => {
      setTimeOffset(preset.offset);
      offsetDisplay.textContent = `Current offset: ${formatTimeOffset(timeOffset)}`;
      
      // Update indicator color based on offset
      const indicator = document.getElementById('time-manipulation-indicator');
      if (indicator) {
        indicator.style.background = timeOffset !== 0 ? '#dc2626' : '#10b981';
      }
      
      // Check if auth warning should be shown
      updateAuthWarning(preset.offset);
      
      // Check backend status after changing time
      checkBackendTimeHandling();
    });
    
    presetContainer.appendChild(button);
  });
  
  // Custom input
  const inputGroup = document.createElement('div');
  inputGroup.style.display = 'flex';
  inputGroup.style.alignItems = 'center';
  inputGroup.style.gap = '5px';
  
  const timeInput = document.createElement('input');
  timeInput.type = 'datetime-local';
  timeInput.style.flex = '1';
  timeInput.style.padding = '5px';
  timeInput.style.borderRadius = '4px';
  timeInput.style.border = '1px solid #ccc';
  
  const setButton = document.createElement('button');
  setButton.textContent = 'Set';
  setButton.style.background = '#2563eb';
  setButton.style.border = 'none';
  setButton.style.color = 'white';
  setButton.style.padding = '5px 8px';
  setButton.style.borderRadius = '4px';
  setButton.style.cursor = 'pointer';
  
  setButton.addEventListener('click', () => {
    if (timeInput.value) {
      const targetTime = new OriginalDate(timeInput.value).getTime();
      const currentTime = OriginalDate.now();
      const newOffset = targetTime - currentTime;
      setTimeOffset(newOffset);
      offsetDisplay.textContent = `Current offset: ${formatTimeOffset(timeOffset)}`;
      
      // Update indicator color
      const indicator = document.getElementById('time-manipulation-indicator');
      if (indicator) {
        indicator.style.background = '#dc2626'; // Always red when custom time set
      }
      
      // Check if auth warning should be shown
      updateAuthWarning(newOffset);
      
      // Check backend status after changing time
      checkBackendTimeHandling();
    }
  });
  
  inputGroup.appendChild(timeInput);
  inputGroup.appendChild(setButton);
  panel.appendChild(inputGroup);
  
  // Reset button
  const resetButton = document.createElement('button');
  resetButton.textContent = 'Reset Time (Use Real Time)';
  resetButton.style.background = '#dc2626';
  resetButton.style.border = 'none';
  resetButton.style.color = 'white';
  resetButton.style.padding = '8px';
  resetButton.style.borderRadius = '4px';
  resetButton.style.cursor = 'pointer';
  resetButton.style.width = '100%';
  resetButton.style.marginTop = '10px';
  
  resetButton.addEventListener('click', () => {
    resetTimeOffset();
    offsetDisplay.textContent = `Current offset: ${formatTimeOffset(timeOffset)}`;
    
    // Update indicator color
    const indicator = document.getElementById('time-manipulation-indicator');
    if (indicator) {
      indicator.style.background = '#10b981'; // Green when reset to real time
    }
    
    // Hide auth warning when reset
    updateAuthWarning(0);
    
    // Check backend status after resetting time
    checkBackendTimeHandling();
  });
  
  panel.appendChild(resetButton);
  
  // Update current time display every second
  const updateCurrentTime = () => {
    const now = new Date();
    currentTimeDisplay.textContent = `Current time: ${now.toLocaleString()}`;
  };
  
  updateCurrentTime();
  const timeInterval = setInterval(updateCurrentTime, 1000);
  
  // Clean up interval when panel is closed
  panel.addEventListener('remove', () => {
    clearInterval(timeInterval);
    clearInterval(backendCheckInterval);
  });
  
  document.body.appendChild(panel);
};

// Initialize time manipulation immediately for browser environments
if (typeof window !== 'undefined') {
  // Wait for DOM to be ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initTimeManipulation);
  } else {
    initTimeManipulation();
  }
}

export {
  timeOffset,
  setTimeOffset,
  resetTimeOffset,
  applyTimeOffset,
  toggleTimeManipulationPanel,
  initTimeManipulation
}; 