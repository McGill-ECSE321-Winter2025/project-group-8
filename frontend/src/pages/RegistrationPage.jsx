"use client"

import { useState } from "react"
import { Link } from "react-router-dom"
import { useNavigate } from "react-router-dom"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { GamepadIcon as GameController } from "lucide-react"

export default function RegistrationPage() {
  const navigate = useNavigate()
  const [isLoading, setIsLoading] = useState(false)
  const [accountType, setAccountType] = useState("player")

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsLoading(true)

    const firstName = document.getElementById("first-name").value
    const lastName = document.getElementById("last-name").value
    const email = document.getElementById("email").value
    const password = document.getElementById("password").value

    try {
      // Step 1: Register the user
      const registrationResponse = await fetch("http://localhost:8080/api/v1/account", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username: `${firstName} ${lastName}`,
          email,
          password,
          gameOwner: accountType === "owner",
        }),
      })

      console.log('Registration response status:', registrationResponse.status);

      if (registrationResponse.ok) {
        console.log("Registration successful")

        // Step 2: Log the user in
        const loginResponse = await fetch("http://localhost:8080/api/v1/auth/login", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ email, password }),
        })

        if (loginResponse.ok) {
          const loginData = await loginResponse.json()
          console.log("Login successful:", loginData)

          // Store user ID, token, and email in localStorage
          localStorage.setItem("userId", loginData.userId); // Use field name from JwtAuthenticationResponse
          localStorage.setItem("token", loginData.token);
          localStorage.setItem("userEmail", loginData.email); // Store email

          // Redirect to dashboard
          navigate("/dashboard")
        } else {
          alert("Login failed after registration. Please try logging in manually.")
        }
      } else {
        const errorMessage = await registrationResponse.text()
        alert(`Registration failed: ${errorMessage}`)
      }
    } catch (error) {
      console.error("Error during registration or login:", error)
      alert("Failed to connect to the server")
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
          <CardTitle className="text-2xl font-bold text-center">Create an account</CardTitle>
          <CardDescription className="text-center">Join our community of board game enthusiasts</CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="first-name">First name</Label>
                <Input id="first-name" required />
              </div>
              <div className="space-y-2">
                <Label htmlFor="last-name">Last name</Label>
                <Input id="last-name" required />
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" placeholder="your.email@example.com" required />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" required />
            </div>
            <div className="space-y-2">
              <Label>Account type</Label>
              <RadioGroup
                defaultValue="player"
                value={accountType}
                onValueChange={setAccountType}
                className="flex flex-col space-y-1"
              >
                <div className="flex items-center space-x-2">
                  <RadioGroupItem value="player" id="player" />
                  <Label htmlFor="player" className="font-normal cursor-pointer">
                    Player (join events and borrow games)
                  </Label>
                </div>
                <div className="flex items-center space-x-2">
                  <RadioGroupItem value="owner" id="owner" />
                  <Label htmlFor="owner" className="font-normal cursor-pointer">
                    Game Owner (share your collection and organize events)
                  </Label>
                </div>
              </RadioGroup>
            </div>
          </CardContent>
          <CardFooter className="flex flex-col space-y-4 mt-4">
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? "Creating account..." : "Create account"}
            </Button>
            <div className="text-center text-sm">
              Already have an account?{" "}
              <Link to="/login" className="text-primary hover:underline">
                Sign in
              </Link>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  )
}
