package com.example.gameengine.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Move {
    private int row;
    private int col;
    private Long playerId;
    private int moveNumber;
}