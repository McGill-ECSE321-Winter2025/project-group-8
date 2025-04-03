// eventService.js

export async function registerForEvent(attendeeId, eventId) {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        const responses = [
          { status: 200, ok: true, json: () => Promise.resolve({ message: "Success" }) },
          { status: 400, ok: false, json: () => Promise.resolve({ message: "Event is at full capacity!" }) },
          { status: 403, ok: false, json: () => Promise.resolve({ message: "You are already registered for this event!" }) }
        ];
  
        // Simulate random API response
        const randomResponse = responses[Math.floor(Math.random() * responses.length)];
  
        if (randomResponse.ok) {
          resolve(randomResponse);
        } else {
          reject(randomResponse);
        }
      }, 500); 
    });
  }
  
export const createEvent = async (eventData) => {
    const formattedData = {
      ...eventData,
      dateTime: new Date(eventData.dateTime).toISOString(),
      maxParticipants: parseInt(eventData.maxParticipants, 10),
    };
  
    const response = await fetch("http://localhost:8080/api/v1/events", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(formattedData),
    });
  
    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || "Failed to create event");
    }
  
    return response.json();
  };


export const searchEventsByTitle = async (title) => {
    const response = await fetch(`http://localhost:8080/api/v1/events/by-title?title=${encodeURIComponent(title)}`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });
  
    if (!response.ok) {
      throw new Error("Failed to search events");
    }
  
    return response.json();
  };

  /*
  export const registerForEvent = async (attendeeId, eventId) => {
    const registrationData = {
      registrationDate: new Date().toISOString(),
      attendeeId,
      eventId,
    };
  
    const response = await fetch("http://localhost:8080/api/v1/registrations", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(registrationData),
    });
  
    // Parse response JSON
    let responseData;
    try {
      responseData = await response.json();
    } catch (error) {
      throw new Error("Unexpected server error. Please try again.");
    }
  
    if (!response.ok) {
      let errorMessage = responseData.message || "Failed to register for event.";
  
      // Handle specific error cases based on backend responses
      if (response.status === 400) {
        errorMessage = "Invalid request. Please check your registration details.";
      } else if (response.status === 409) {
        errorMessage = "You are already registered for this event.";
      } else if (response.status === 403) {
        errorMessage = "Event is at full capacity.";
      } else if (response.status === 500) {
        errorMessage = "Server error. Please try again later.";
      }
  
      throw new Error(errorMessage);
    }
  
    return responseData;
  };*/
  
  