package ca.mcgill.ecse321.gameorganizer.controllers;

import ca.mcgill.ecse321.gameorganizer.dtos.GameCreationDto;
import ca.mcgill.ecse321.gameorganizer.dtos.GameResponseDto;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.services.AccountService;
import ca.mcgill.ecse321.gameorganizer.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ca.mcgill.ecse321.gameorganizer.models.Game;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class GameController {
    @Autowired
    private GameService service;

    @Autowired
    private AccountService accountService;

    @GetMapping("/games")
    public ResponseEntity<List<GameResponseDto>> getAllGames(){
        List<Game> games = service.getAllGames();

        List<GameResponseDto> gameResponseDtos = games.stream().map(game -> new GameResponseDto(game)).collect(Collectors.toList());

        return ResponseEntity.ok(gameResponseDtos);

    }

    @GetMapping("/games/{id}")
    public ResponseEntity<GameResponseDto> findGameById(@PathVariable int id){
        Game game = service.getGameById(id);
        return ResponseEntity.ok(new GameResponseDto(game));
    }

    @PostMapping("/games")
    public ResponseEntity<GameResponseDto> createGame(@RequestBody GameCreationDto gameCreationDto){



        GameResponseDto createdGame = service.createGame(gameCreationDto);

        return new ResponseEntity<>(createdGame, HttpStatus.CREATED);

    }

    @PutMapping("/games/{id}")
    public ResponseEntity<GameResponseDto> updateGme(@PathVariable int id, @RequestBody GameCreationDto gameDto){


        GameResponseDto updatedGame = service.updateGame(id, gameDto);
        return ResponseEntity.ok(updatedGame);

    }

    @DeleteMapping("/games/{id}")
    public ResponseEntity<String> deleteGame(@PathVariable int id){
        return service.deleteGame(id);
    }

    // /reviews/{id}

    // /reviews/{id} get ? or like some sql query get all review for game name
    


}
