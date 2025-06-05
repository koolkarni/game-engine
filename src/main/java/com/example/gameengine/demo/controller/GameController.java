package com.example.gameengine.demo.controller;

import com.example.gameengine.demo.model.Game;
import com.example.gameengine.demo.model.GameViewDTO;
import com.example.gameengine.demo.model.Move;
import com.example.gameengine.demo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/create")
    public ResponseEntity<Game> createGame(@RequestParam Long playerId) {
        Game game = gameService.createGame(playerId);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/join")
    public ResponseEntity<Game> joinGame(
            @PathVariable Long gameId,
            @RequestParam Long playerId) {
        Game game = gameService.joinGame(gameId, playerId);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameViewDTO> makeMove(@PathVariable Long gameId, @RequestBody Move move) {
        Game updatedGame = gameService.makeMove(gameId, move.getRow(), move.getCol(), move.getPlayerId());
        int[][] board = gameService.getBoard(gameId); // from gameBoardMap
        return ResponseEntity.ok(new GameViewDTO(updatedGame, board));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameViewDTO> getGame(@PathVariable Long gameId) {
        Game game = gameService.getGame(gameId);
        int[][] board = gameService.getBoard(gameId);
        return ResponseEntity.ok(new GameViewDTO(game, board));
    }

}