"use client"

import { useState, useEffect } from "react" // Keep useEffect for now, might need later or remove if AuthProvider handles all redirects
import { Link, useNavigate } from "react-router-dom"
import { useAuth } from "@/context/AuthContext"; // Import useAuth
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { GamepadIcon as GameController } from "lucide-react"

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, user } = useAuth(); // Get login function and user state from context
  const [isLoading, setIsLoading] = useState(false);
  const [email, setEmail] = useState(""); // Use state for form inputs
  const [password, setPassword] = useState(""); // Use state for form inputs
  const [error, setError] = useState(""); // State for login errors
  
  // Redirect if already logged in (user state is populated by AuthProvider)
  useEffect(() => {
    if (user) {

      navigate("/dashboard");
    }
  }, [user, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError(""); // Clear previous errors

    try {
      // TODO: Replace with environment variable for API URL
      const response = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
        credentials: 'include', // Send and receive cookies
      });

      if (response.ok) {
        // Backend now sets HttpOnly cookie automatically.
        // The response body should contain the user summary.
        const userData = await response.json();
        
        // Check for Authorization header that contains the JWT token
        const authHeader = response.headers.get('Authorization');
        let token = null;
        
        if (authHeader && authHeader.startsWith('Bearer ')) {
          token = authHeader.substring(7); // Remove 'Bearer ' prefix
        } else if (userData.token) {
          // If token is in the response body
          token = userData.token;
        } else if (userData.accessToken) {
          // Alternative name for token
          token = userData.accessToken;
        }
        
        // Pass both user data and token to login function
        login(userData, token);
        
        // Test that the authentication is working
        console.log("Login successful, redirecting to dashboard...");
        navigate("/dashboard");
      } else if (response.status === 401) {
        setError("Invalid email or password");
      } else {
        const errorData = await response.text(); // Or response.json() if backend sends structured errors
        console.error("Login failed:", response.status, errorData);
        setError(`An unexpected error occurred: ${response.status}`);
      }
    } catch (error) {
      console.error("Error during login:", error);
      setError("Failed to connect to the server. Please try again later.");
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="container flex items-center justify-center min-h-screen py-12">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1">
          <div className="flex justify-center mb-4">
            <div className="flex items-center gap-2">
              <GameController className="h-6 w-6 text-primary" />
              <span className="text-xl font-bold">BoardGameConnect</span>
            </div>
          </div>
          <CardTitle className="text-2xl font-bold text-center">Welcome back</CardTitle>
          <CardDescription className="text-center">Enter your credentials to access your account</CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" placeholder="your.email@example.com" required value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label htmlFor="password">Password</Label>
                <Link to="/forgot-password" className="text-sm text-primary hover:underline">
                  Forgot password?
                </Link>
              </div>
              <Input id="password" type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4 mt-6"> {/* Reduced space */}
            {error && <p className="text-red-500 text-sm text-center">{error}</p>} {/* Display error message */}
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? "Signing in..." : "Sign in"}
            </Button>
            <div className="text-center text-sm">
              Don&apos;t have an account?{" "}
              <Link to="/register" className="text-primary hover:underline">
                Sign up
              </Link>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  )
}
