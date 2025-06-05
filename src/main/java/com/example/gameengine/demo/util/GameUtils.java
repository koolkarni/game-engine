package com.example.gameengine.demo.util;

public class GameUtils {
    public static boolean checkWin(int[][] grid, int pid) {
        for (int i = 0; i < 3; i++) {
            if (grid[i][0] == pid && grid[i][1] == pid && grid[i][2] == pid) return true; // row
            if (grid[0][i] == pid && grid[1][i] == pid && grid[2][i] == pid) return true; // column
        }
        // diagonals
        return (grid[0][0] == pid && grid[1][1] == pid && grid[2][2] == pid) ||
                (grid[0][2] == pid && grid[1][1] == pid && grid[2][0] == pid);
    }

    public static boolean isDraw(int[][] grid) {
        for (int[] row : grid) {
            for (int cell : row) {
                if (cell == 0) return false;
            }
        }
        return true;
    }

    public static int countMoves(int[][] grid, Long playerId) {
        int count = 0;
        for (int[] row : grid) {
            for (int cell : row) {
                if (cell == playerId.intValue()) count++;
            }
        }
        return count;
    }
}
