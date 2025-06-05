package com.example.gameengine.demo.service;

import com.example.gameengine.demo.model.Game;
import com.example.gameengine.demo.model.GameStatus;
import com.example.gameengine.demo.model.PlayerStats;
import com.example.gameengine.demo.repository.GameRepository;
import com.example.gameengine.demo.repository.PlayerStatsRepository;
import org.springframework.stereotype.Service;

import com.example.gameengine.demo.model.*;
import com.example.gameengine.demo.repository.GameRepository;
import com.example.gameengine.demo.repository.PlayerStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerStatsRepository playerStatsRepository;

    public Game createGame(Long playerId) {
        Game game = new Game();
        game.setPlayer1Id(playerId);
        game.setCurrentTurnPlayerId(playerId);
        game.setStatus(GameStatus.WAITING);
        return gameRepository.save(game);
    }

    public Game joinGame(Long gameId, Long playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalStateException("Game not found."));

        if (game.getStatus() != GameStatus.WAITING) {
            throw new IllegalStateException("Cannot join game.");
        }

        game.setPlayer2Id(playerId);
        game.setStatus(GameStatus.ONGOING);
        return gameRepository.save(game);
    }

    private void updatePlayerStats(Long winnerId, int moveCount) {
        PlayerStats stats = playerStatsRepository.findById(winnerId)
                .orElse(new PlayerStats());
        stats.setPlayerId(winnerId);
        stats.setWins(stats.getWins() + 1);
        stats.setTotalMovesInWins(stats.getTotalMovesInWins() + moveCount);
        playerStatsRepository.save(stats);
    }
}
