"use client"

import { useState, useContext } from "react" // Added useContext
import { Button } from "@/components/ui/button";
import { useAuth } from "@/context/AuthContext.jsx";
import { upgradeAccountToGameOwner, updateUsernamePassword } from '@/service/dashboard-api.js'; // Added updateUsernamePassword
// import { toast } from 'react-toastify';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input.jsx"
import { Label } from "@/components/ui/label.jsx"
import { Settings, User, KeyRound } from "lucide-react"

export default function SideMenuBar({ userType }) { // userType prop might become redundant if context is always used
  const { user, isAuthenticated } = useAuth(); // Get user and auth status from context
  const [open, setOpen] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isUpgrading, setIsUpgrading] = useState(false);
  const [upgradeError, setUpgradeError] = useState(null);
  const [upgradeSuccess, setUpgradeSuccess] = useState(false);
  const [isSavingSettings, setIsSavingSettings] = useState(false); // Loading state for settings save
  const [settingsError, setSettingsError] = useState(null); // Error state for settings save

  // Renamed handleSubmit to handleSettingsSubmit to avoid naming conflict
  async function handleSettingsSubmit(e) {
    e.preventDefault();
    setSettingsError(null);

    if (newPassword && newPassword !== confirmPassword) {
      // alert("Passwords don't match"); // Replace alert with better feedback
      setSettingsError("Passwords don't match");
      // toast.error("Passwords don't match");
      return;
    }

    setIsSavingSettings(true);

    const updateData = {};
    updateData.email = localStorage.getItem("userEmail");
    if (username.trim()) {
      updateData.username = username.trim();
    }
    updateData.password = password;
    if (newPassword) {
      // Add password validation if needed (e.g., minimum length)
      updateData.newPassword = newPassword;
    }

    if (Object.keys(updateData).length === 0) {
      // toast.info("No changes detected.");
      setIsSavingSettings(false);
      setOpen(false); // Close dialog if no changes
      return;
    }

    try {
      await updateUsernamePassword(updateData);
      // toast.success("Account settings updated successfully!");

      // Clear form and close dialog on success
      setOpen(false);
      setUsername("");
      setPassword("");
      setConfirmPassword("");
      // Note: Context doesn't auto-refresh username. User might need to re-login to see username changes everywhere.

    } catch (err) {
      console.error("Failed to update account settings:", err);
      // toast.error(err.message || "Failed to update settings. Please try again.");
      setSettingsError(err.message || "Failed to update settings. Please try again.");
    } finally {
      setIsSavingSettings(false);
    }
  }

  const handleUpgrade = async () => {
    if (!isAuthenticated || !user?.email) {
      // toast.error("You must be logged in to perform this action.");
      setUpgradeError("You must be logged in to perform this action.");
      return;
    }

    setIsUpgrading(true);
    setUpgradeError(null);
    setUpgradeSuccess(false);

    try {
      await upgradeAccountToGameOwner(user.email);
      // toast.success("Account successfully upgraded to Game Owner! Please re-login for changes to take effect.");
      setUpgradeSuccess(true);
      // Ideally, we would refresh the user context here, but it lacks a refresh function.
      // Option: Trigger logout and redirect? -> logout(); navigate('/login');
      // Option: Force page reload? -> window.location.reload();
      // Option: Just inform user -> (Handled by success message)

    } catch (err) {
      console.error("Failed to upgrade account:", err);
      // toast.error(err.message || "Failed to upgrade account. Please try again.");
      setUpgradeError(err.message || "Failed to upgrade account. Please try again.");
    } finally {
      setIsUpgrading(false);
    }
  };

  return (
    <>
      {/* Use user role from context if available, otherwise fallback to prop */}
      {isAuthenticated && user?.role !== 'GAME_OWNER' && ( // Check context for role
        <>
          <Button variant="ghost" className="w-full justify-start gap-2" onClick={handleUpgrade} disabled={isUpgrading}>
            <User className="h-4 w-4" />
            {isUpgrading ? "Upgrading..." : "Become a Game Owner"}
          </Button>
          {upgradeError && <p className="text-red-500 text-xs px-4 py-1">{upgradeError}</p>}
          {upgradeSuccess && <p className="text-green-500 text-xs px-4 py-1">Upgrade successful! Please log out and log back in.</p>}
        </>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogTrigger asChild>
          <Button variant="ghost" className="w-full justify-start gap-2">
            <Settings className="h-4 w-4" />
            Account Settings
          </Button>
        </DialogTrigger>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Account Settings</DialogTitle>
            <DialogDescription>
              Update your account information. Leave fields blank to keep current values. Click save when you're done.
              {settingsError && <p className="text-red-500 text-sm mt-2">{settingsError}</p>}
            </DialogDescription>
          </DialogHeader>
          <form onSubmit={handleSettingsSubmit}> {/* Updated onSubmit handler */}
            <div className="grid gap-4 py-4">
              <div className="grid gap-2">
                <Label htmlFor="username">
                  <User className="h-4 w-4 inline mr-2"/>
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
                <Label htmlFor="username">
                  <KeyRound className="h-4 w-4 inline mr-2"/>
                  Password
                </Label>
                <Input
                  id="username"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter new username"
                />
              </div>

              <div className="grid gap-2">
                <Label htmlFor="password">
                  <KeyRound className="h-4 w-4 inline mr-2"/>
                  New Password
                </Label>
                <Input
                  id="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Enter new password"
                />
                <p className="text-xs text-muted-foreground">Leave blank if you don't want to change your password</p>
              </div>

              {password && (
                <div className="grid gap-2">
                  <Label htmlFor="confirmPassword">Confirm Password</Label>
                  <Input
                    id="confirmPassword"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="Confirm new password"
                  />
                </div>
              )}
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setOpen(false)} disabled={isSavingSettings}>
                Cancel
              </Button>
              <Button type="submit" disabled={isSavingSettings}>
                {isSavingSettings ? "Saving..." : "Save changes"}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  )
}

