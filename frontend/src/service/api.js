// eventService.js

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
  