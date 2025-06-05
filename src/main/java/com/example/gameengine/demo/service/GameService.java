package com.example.gameengine.demo.service;

import com.example.gameengine.demo.model.Game;
import com.example.gameengine.demo.model.GameStatus;
import com.example.gameengine.demo.model.PlayerStats;
import com.example.gameengine.demo.repository.GameRepository;
import com.example.gameengine.demo.repository.PlayerStatsRepository;
import org.springframework.stereotype.Service;
import static com.example.gameengine.demo.util.GameUtils.countMoves;
import static com.example.gameengine.demo.util.GameUtils.checkWin;
import static com.example.gameengine.demo.util.GameUtils.isDraw;
import com.example.gameengine.demo.model.*;
import com.example.gameengine.demo.repository.GameRepository;
import com.example.gameengine.demo.repository.PlayerStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final PlayerStatsRepository playerStatsRepository;
    private final Map<Long, int[][]> gameBoardMap = new ConcurrentHashMap<>();

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

    public Game makeMove(Long gameId, int row, int col, Long playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalStateException("Game not found"));

        // Locking per-game to handle concurrency
        synchronized (gameId.toString().intern()) {

            if (!GameStatus.ONGOING.equals(game.getStatus())) {
                throw new IllegalStateException("Game is not active.");
            }

            if (!playerId.equals(game.getCurrentTurnPlayerId())) {
                throw new IllegalArgumentException("Not your turn.");
            }

            // Load or initialize grid from game (you can use a Map<Long, int[][]> to track game boards)
            int[][] grid = gameBoardMap.computeIfAbsent(gameId, id -> new int[3][3]);

            if (grid[row][col] != 0) {
                throw new IllegalArgumentException("Cell already occupied.");
            }

            // Make move
            grid[row][col] = playerId.intValue();
            game.setCurrentTurnPlayerId(
                    playerId.equals(game.getPlayer1Id()) ? game.getPlayer2Id() : game.getPlayer1Id()
            );

            // Count how many moves the player has made in this game
            int moveCount = countMoves(grid, playerId);

            if (checkWin(grid, playerId.intValue())) {
                game.setStatus(GameStatus.COMPLETE);
                game.setWinnerId(playerId);
                updatePlayerStats(playerId, moveCount);
            } else if (isDraw(grid)) {
                game.setStatus(GameStatus.COMPLETE);
            }

            return gameRepository.save(game);
        }
    }
    public Game getGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalStateException("Game not found."));
    }

    public int[][] getBoard(Long gameId) {
        return gameBoardMap.getOrDefault(gameId, new int[3][3]);
    }

}
