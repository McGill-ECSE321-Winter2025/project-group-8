package ca.mcgill.ecse321.gameorganizer.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class DeleteMe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

}
