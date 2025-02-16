package ca.mcgill.ecse321.gameorganizer.models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.NoArgsConstructor;
import java.util.List;


@Entity
@NoArgsConstructor
public class GameOwner extends Account {

    public GameOwner(String aName, String aEmail, String aPassword) {
        super(aName, aEmail, aPassword);
    }

    // Associations

    @OneToMany
    private List<Game> gamesOwned;

    @OneToMany
    private List<BorrowRequest> requestsReceived;

    @OneToMany
    private List<LendingRecord> history;


    public void delete() {
        super.delete();
    }

}
