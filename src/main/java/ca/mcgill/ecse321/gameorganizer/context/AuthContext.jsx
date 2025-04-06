import Cookies from 'js-cookie';

// Before making any authenticated API call, check if the isAuthenticated cookie exists
const checkAuthentication = () => {
  const isAuthCookie = Cookies.get('isAuthenticated');
  return !!isAuthCookie; // Convert to boolean
};

// Update the fetchUserData function to check for the cookie first
const fetchUserData = async () => {
  try {
    // First check if we have the authentication cookie
    if (!checkAuthentication()) {
      console.debug('AuthContext: No authentication cookie found, skipping user data fetch');
      setLoading(false);
      setUser(null);
      setInitialized(true);
      return;
    }
    
    console.debug('AuthContext: Fetching user data');
    setLoading(true);
    const userData = await userApi.getCurrentUser();
    console.debug('AuthContext: User data received', userData);
    setUser(userData);
  } catch (error) {
    console.error('Error fetching user data:', error);
    setUser(null);
  } finally {
    setLoading(false);
    setInitialized(true);
  }
}; 