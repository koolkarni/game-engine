package com.example.gameengine.demo.service;

import com.example.gameengine.demo.model.Game;
import com.example.gameengine.demo.model.GameStatus;
import com.example.gameengine.demo.model.PlayerStats;
import com.example.gameengine.demo.repository.GameRepository;
import com.example.gameengine.demo.repository.PlayerStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.gameengine.demo.util.GameUtils.*;
@Slf4j
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
        Game saved = gameRepository.save(game);
        createStatsIfAbsent(playerId);
        log.info("Game created. ID={}, created by Player={}", saved.getId(), playerId);
        return saved;
    }

    public Game joinGame(Long gameId, Long playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalStateException("Game not found."));

        if (game.getStatus() != GameStatus.WAITING) {
            log.warn("Join request rejected. Game {} has already started or completed.", gameId);
            throw new IllegalStateException("Cannot join game.");
        }

        game.setPlayer2Id(playerId);
        game.setStatus(GameStatus.ONGOING);
        Game updated = gameRepository.save(game);
        createStatsIfAbsent(playerId);
        log.info("Player {} successfully joined Game {}", playerId, gameId);
        return updated;
    }

    private void updatePlayerStats(Long winnerId, int moveCount) {
        PlayerStats stats = playerStatsRepository.findById(winnerId)
                .orElse(new PlayerStats());
        stats.setPlayerId(winnerId);
        stats.setWins(stats.getWins() + 1);
        stats.setTotalMovesInWins(stats.getTotalMovesInWins() + moveCount);
        playerStatsRepository.save(stats);
        log.info("Player {} stats updated: wins = {}, totalMovesInWins = {}",
                winnerId, stats.getWins(), stats.getTotalMovesInWins());
    }

    public Game makeMove(Long gameId, int row, int col, Long playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalStateException("Game not found."));

        synchronized (gameId.toString().intern()) {
            log.info("Player {} is attempting a move in Game {} at cell ({}, {})", playerId, gameId, row, col);

            if (!GameStatus.ONGOING.equals(game.getStatus())) {
                log.warn("Invalid move attempt. Game {} is not active.", gameId);
                throw new IllegalStateException("Game is not active.");
            }

            if (!playerId.equals(game.getCurrentTurnPlayerId())) {
                log.warn("Player {} attempted to move out of turn in Game {}", playerId, gameId);
                throw new IllegalArgumentException("Not your turn.");
            }

            int[][] grid = gameBoardMap.computeIfAbsent(gameId, id -> new int[3][3]);

            if (grid[row][col] != 0) {
                log.warn("Cell ({}, {}) in Game {} is already occupied", row, col, gameId);
                throw new IllegalArgumentException("Cell already occupied.");
            }

            grid[row][col] = playerId.intValue();
            game.setCurrentTurnPlayerId(
                    playerId.equals(game.getPlayer1Id()) ? game.getPlayer2Id() : game.getPlayer1Id()
            );

            int moveCount = countMoves(grid, playerId);

            if (checkWin(grid, playerId.intValue())) {
                game.setStatus(GameStatus.COMPLETE);
                game.setWinnerId(playerId);
                updatePlayerStats(playerId, moveCount);
                log.info("Player {} won Game {}", playerId, gameId);
            } else if (isDraw(grid)) {
                game.setStatus(GameStatus.COMPLETE);
                log.info("Game {} ended in a draw.", gameId);
            } else {
                log.info("Move recorded for Player {} in Game {}. Next turn: Player {}",
                        playerId, gameId, game.getCurrentTurnPlayerId());
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

    public Map<String, List<PlayerStats>> getLeaderboard() {
        List<PlayerStats> all = playerStatsRepository.findAll();

        List<PlayerStats> topByWins = all.stream()
                .sorted((a, b) -> Integer.compare(b.getWins(), a.getWins()))
                .limit(3)
                .toList();

        List<PlayerStats> topByEfficiency = all.stream()
                .filter(p -> p.getWins() > 0)
                .sorted(Comparator.comparingDouble(p -> (double) p.getTotalMovesInWins() / p.getWins()))
                .limit(3)
                .toList();

        Map<String, List<PlayerStats>> result = new HashMap<>();
        result.put("topByWins", topByWins);
        result.put("topByEfficiency", topByEfficiency);
        return result;
    }

    private void createStatsIfAbsent(Long playerId) {
        playerStatsRepository.findById(playerId).orElseGet(() -> {
            PlayerStats stats = new PlayerStats();
            stats.setPlayerId(playerId);
            return playerStatsRepository.save(stats);
        });
    }
}
