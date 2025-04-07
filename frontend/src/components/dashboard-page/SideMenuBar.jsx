"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Settings, User, KeyRound, RefreshCw, Crown } from "lucide-react"
import { updateUsernamePassword, upgradeAccountToGameOwner } from "@/service/update-account-info.js"

export default function SideMenuBar({ userType }) {
  const [settingsOpen, setSettingsOpen] = useState(false)
  const [ownerDialogOpen, setOwnerDialogOpen] = useState(false)
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [newPassword, setNewPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [showRefreshDialog, setShowRefreshDialog] = useState(false)

  // Game owner form states
  const [gameTitle, setGameTitle] = useState("")
  const [gameDescription, setGameDescription] = useState("")

  async function handleSettingsSubmit(e) {
    e.preventDefault()

    if (newPassword && newPassword !== confirmPassword) {
      alert("Passwords don't match")
      return
    }

    console.log("Updating account with:", { username, password, email: localStorage.getItem("userEmail") })

    setSettingsOpen(false)

    try {
      console.log(typeof localStorage.getItem("userEmail"))
      const response = await updateUsernamePassword({
        email: localStorage.getItem("userEmail"),
        username: username,
        password: password,
        newPassword: newPassword,
      })

      setShowRefreshDialog(true)

      // Reset form fields
      setUsername("")
      setPassword("")
      setNewPassword("")
      setConfirmPassword("")
    } catch (error) {
      console.error(error)
    }
  }

  async function handleOwnerSubmit(e) {
    e.preventDefault()

    console.log("Submitting game owner request:", { gameTitle, gameDescription })

    setOwnerDialogOpen(false)

    try {
     await upgradeAccountToGameOwner(localStorage.getItem("userEmail"))

      setShowRefreshDialog(true)

      // Reset form fields
      setGameTitle("")
      setGameDescription("")
    } catch (error) {
      console.error(error)
    }
  }

  function handleRefresh() {
    window.location.reload()
  }

  return (
    <>
      {userType !== "owner" && (
        <Dialog open={ownerDialogOpen} onOpenChange={setOwnerDialogOpen}>
          <DialogTrigger asChild>
            <Button variant="ghost" className="w-full justify-start gap-2">
              <Crown className="h-4 w-4" />
              Become a Game Owner
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-[425px]">
            <DialogHeader>
              <DialogTitle>Become a Game Owner</DialogTitle>
              <DialogDescription>
                By becoming a game owner, you will be able to add games and host events!
              </DialogDescription>
            </DialogHeader>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setOwnerDialogOpen(false)}>
                  Cancel
                </Button>
                <Button variant="positive" type="submit" onClick={handleOwnerSubmit}>Go!</Button>
              </DialogFooter>
          </DialogContent>
        </Dialog>
      )}

      <Dialog open={settingsOpen} onOpenChange={setSettingsOpen}>
        <DialogTrigger asChild>
          <Button variant="ghost" className="w-full justify-start gap-2">
            <Settings className="h-4 w-4" />
            Account Settings
          </Button>
        </DialogTrigger>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Account Settings</DialogTitle>
            <DialogDescription>Update your account information. Click save when you're done.</DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSettingsSubmit}>
            <div className="grid gap-4 py-4">
              <div className="grid gap-2">
                <Label htmlFor="username">
                  <User className="h-4 w-4 inline mr-2" />
                  Username
                </Label>
                <Input
                  id="username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Enter new username"
                />
              </div>

              <div className="grid gap-2">
                <Label htmlFor="password">
                  <KeyRound className="h-4 w-4 inline mr-2" />
                  Password
                </Label>
                <Input
                  id="password"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                />
              </div>

              <div className="grid gap-2">
                <Label htmlFor="newPassword">
                  <KeyRound className="h-4 w-4 inline mr-2" />
                  New Password
                </Label>
                <Input
                  id="newPassword"
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Enter new password"
                />
                <p className="text-xs text-muted-foreground">
                  Leave this field blank if you don't want to change your password
                </p>
              </div>

              {newPassword && (
                <div className="grid gap-2">
                  <Label htmlFor="confirmPassword">Confirm Password</Label>
                  <Input
                    id="confirmPassword"
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="Confirm new password"
                  />
                </div>
              )}
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setSettingsOpen(false)}>
                Cancel
              </Button>
              <Button type="submit">Save changes</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Refresh Dialog - Shared between both forms */}
      <Dialog open={showRefreshDialog} onOpenChange={setShowRefreshDialog}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Success!</DialogTitle>
            <DialogDescription> Please refresh the page for the changes to take
              effect.</DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button onClick={handleRefresh} className="gap-2">
              <RefreshCw className="h-4 w-4" />
              Refresh Page
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}

