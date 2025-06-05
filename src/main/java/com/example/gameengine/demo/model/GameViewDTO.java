package com.example.gameengine.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameViewDTO {
    private Game game;
    private int[][] board;
}