package com.example.gameengine.demo.controller;

import com.example.gameengine.demo.model.Game;
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
}