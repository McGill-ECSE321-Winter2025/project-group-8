const BASE_URL = "http://localhost:8080/api/v1";
const TOKEN = localStorage.getItem("token");

export async function updateUsernamePassword(request) {
  console.log("cock");
  console.log(TOKEN)
  try {
    fetch('http://localhost:8080/api/v1/account',
      { method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${TOKEN}`
        },
        body: JSON.stringify(request)
      }
    ).then(response => {
        if (!response.ok) {
          return response.text().then(errorText => {
            console.error('Error response:', errorText);
            // Handle the error text as needed
          });
        }
        return response.json(); // Use this if the response is valid JSON
      }
      ).catch(error => {
        console.error('Fetch error:', error);
      });
  }
  catch (e) {
    console.log(e);
  }
}

export async function upgradeAccountToGameOwner(email) {
  return fetch(`${BASE_URL}/account/${email}`,
    {
      method: "PUT",
      headers: {
        "Authorization": `Bearer ${TOKEN}`,
        "Content-Type": "application/json"
      },
    })
}
