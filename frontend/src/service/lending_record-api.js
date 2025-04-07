/**
 * LendingRecord API Module
 *
 * This file provides API functions for managing lending records.
 * Follows the application's established API patterns.
 */

const API_BASE_URL = "http://localhost:8080/api/v1";
const LENDING_RECORDS_ENDPOINT = `${API_BASE_URL}/lending-records`;

/**
 * Creates a new lending record
 * @param {Object} requestData - Contains requestId, ownerId, startDate, and endDate
 * @returns {Promise<Object>} - The created LendingRecord DTO from the server
 */
export async function createLendingRecord(requestData) {
    try {
        const response = await fetch(LENDING_RECORDS_ENDPOINT, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(requestData),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Failed to create lending record");
        }

        const data = await response.json();
        return data;
    } catch (error) {
        console.error("Error creating lending record:", error);
        throw error;
    }
}
