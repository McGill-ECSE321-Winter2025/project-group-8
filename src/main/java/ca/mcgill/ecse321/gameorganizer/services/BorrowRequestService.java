package ca.mcgill.ecse321.gameorganizer.services;

import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class BorrowRequestService {

    public ResponseEntity<String> createBorrowRequest(BorrowRequest newBorrowRequest) {
        return null;
    }

    public BorrowRequest getBorrowRequestById(int id) {
        return null;
    }

    public ResponseEntity<String> updateBorrowRequestStatus(int id, String newStatus) {
        return null;
    }

    public ResponseEntity<String> deleteBorrowRequest(int id) {
        return null;
    }
}
