import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import { BrowserRouter } from "react-router-dom";
import { AuthProvider } from './context/AuthContext.jsx'; // Import AuthProvider
import '@fortawesome/fontawesome-free/css/all.min.css';


createRoot(document.getElementById('root')).render(
  <StrictMode>
      <BrowserRouter>
        <AuthProvider> {/* Wrap App with AuthProvider */}
          <App />
        </AuthProvider>
      </BrowserRouter>
  </StrictMode>,
)
