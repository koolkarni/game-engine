package com.example.gameengine.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "player_stats")
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStats {
    @Id
    private Long playerId;
    private int wins;
    private int totalMovesInWins;

    public double getEfficiency() {
        return wins == 0 ? Double.MAX_VALUE : (double) totalMovesInWins / wins;
    }
}