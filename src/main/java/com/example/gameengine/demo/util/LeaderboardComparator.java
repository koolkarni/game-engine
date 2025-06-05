package com.example.gameengine.demo.util;

import com.example.gameengine.demo.model.PlayerStats;

import java.util.Comparator;

public class LeaderboardComparator implements Comparator<PlayerStats> {
    @Override
    public int compare(PlayerStats a, PlayerStats b) {
        if (a.getWins() != b.getWins()) {
            return Integer.compare(b.getWins(), a.getWins()); // more wins first
        }
        return Double.compare(a.getEfficiency(), b.getEfficiency()); // lower avg moves per win
    }
}
