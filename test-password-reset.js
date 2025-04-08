// Simple test script to check the password reset API
// Run with Node.js: node test-password-reset.js

const fetch = require('node-fetch');

// Configuration
const API_URL = 'http://localhost:8080';
const TEST_EMAIL = 'test@example.com'; // Replace with a real email for testing

// Test the password reset request endpoint
async function testPasswordReset() {
  try {
    console.log(`Testing password reset for email: ${TEST_EMAIL}`);
    
    const response = await fetch(`${API_URL}/auth/request-password-reset`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email: TEST_EMAIL }),
    });
    
    const data = await response.text();
    console.log(`Status: ${response.status}`);
    console.log(`Response: ${data}`);
    
    if (response.ok) {
      console.log('Password reset request was successful');
    } else {
      console.error('Password reset request failed');
    }
  } catch (error) {
    console.error('Error making request:', error.message);
  }
}

// Test the diagnostic endpoint
async function testDiagnostics() {
  try {
    console.log('Testing email diagnostics endpoint');
    
    const response = await fetch(`${API_URL}/dev/email/diagnose`, {
      method: 'POST',
    });
    
    const data = await response.text();
    console.log(`Status: ${response.status}`);
    console.log(`Diagnostics: ${data}`);
  } catch (error) {
    console.error('Error making diagnostics request:', error.message);
  }
}

// Test the email test endpoint
async function testEmailSending() {
  try {
    console.log(`Testing direct email sending to: ${TEST_EMAIL}`);
    
    const response = await fetch(`${API_URL}/dev/email/test?email=${encodeURIComponent(TEST_EMAIL)}`, {
      method: 'POST',
    });
    
    const data = await response.text();
    console.log(`Status: ${response.status}`);
    console.log(`Response: ${data}`);
  } catch (error) {
    console.error('Error making email test request:', error.message);
  }
}

// Run all tests
async function runTests() {
  console.log('==== Testing Password Reset API ====');
  await testDiagnostics();
  await testEmailSending();
  await testPasswordReset();
  console.log('==== Testing Complete ====');
}

runTests(); 