package dev.pixelib.meteor.sender;

import dev.pixelib.meteor.base.defaults.LoopbackTransport;
import dev.pixelib.meteor.core.Meteor;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardExample {

    public static void main(String[] args) throws Exception{
        Meteor meteor = new Meteor(new LoopbackTransport());
        meteor.registerImplementation(new ScoreboardImplementation(), "parkour-leaderboard");

        Scoreboard scoreboard = meteor.registerProcedure(Scoreboard.class, "parkour-leaderboard");

        scoreboard.setScoreForPlayer("player1", 10);
        scoreboard.setScoreForPlayer("player2", 20);
        scoreboard.setScoreForPlayer("player3", 30);

        Map<String, Integer> scores = scoreboard.getAllScores();
        System.out.println("scores: " + scores);

        int player1Score = scoreboard.getScoreForPlayer("player1");
        System.out.println("player1 score: " + player1Score);

        meteor.stop();
    }

    public interface Scoreboard {
        int getScoreForPlayer(String player);
        void setScoreForPlayer(String player, int score);
        Map<String, Integer> getAllScores();
    }

    public static class ScoreboardImplementation implements Scoreboard {

        private final Map<String, Integer> scores = new HashMap<>();

        @Override
        public int getScoreForPlayer(String player) {
            return scores.getOrDefault(player, 0);
        }

        @Override
        public void setScoreForPlayer(String player, int score) {
            scores.put(player, score);
        }

        @Override
        public Map<String, Integer> getAllScores() {
            return scores;
        }
    }
}
