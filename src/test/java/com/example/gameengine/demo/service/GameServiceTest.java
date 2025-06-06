package com.example.gameengine.demo.service;

import com.example.gameengine.demo.model.Game;
import com.example.gameengine.demo.model.GameStatus;
import com.example.gameengine.demo.repository.GameRepository;
import com.example.gameengine.demo.repository.PlayerStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerStatsRepository playerStatsRepository;

    @InjectMocks
    private GameService gameService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateGame_ShouldInitializeCorrectly() {
        Long playerId = 1L;
        when(gameRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Game game = gameService.createGame(playerId);

        assertNotNull(game);
        assertEquals(playerId, game.getPlayer1Id());
        assertEquals(playerId, game.getCurrentTurnPlayerId());
        assertEquals(GameStatus.WAITING, game.getStatus());
    }

    @Test
    void testJoinGame_ShouldUpdateGameStatusAndPlayer2() {
        Long gameId = 1L;
        Long player1 = 11L;
        Long player2 = 22L;
        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setPlayer1Id(player1);
        mockGame.setStatus(GameStatus.WAITING);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(gameRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Game updated = gameService.joinGame(gameId, player2);

        assertEquals(player2, updated.getPlayer2Id());
        assertEquals(GameStatus.ONGOING, updated.getStatus());
    }

    @Test
    void testJoinGame_ShouldThrowIfGameNotWaiting() {
        Long gameId = 2L;
        Game mockGame = new Game();
        mockGame.setId(gameId);
        mockGame.setStatus(GameStatus.COMPLETE);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));

        assertThrows(IllegalStateException.class, () -> gameService.joinGame(gameId, 99L));
    }

    @Test
    void testGetGame_ShouldReturnIfExists() {
        Long gameId = 3L;
        Game mockGame = new Game();
        mockGame.setId(gameId);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));

        Game result = gameService.getGame(gameId);
        assertEquals(gameId, result.getId());
    }

    @Test
    void testGetGame_ShouldThrowIfNotExists() {
        when(gameRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> gameService.getGame(123L));
    }
}