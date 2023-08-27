package com.meteormsg.benchmarks;

import com.meteormsg.base.RpcOptions;
import com.meteormsg.base.defaults.LoopbackTransport;
import com.meteormsg.core.Meteor;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.All})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class ScoresWithMaps {

    private Meteor meteor;
    private PersonalScores scoreboardStub;

    @Param({"1", "10", "50"}) // Example parameter values
    private int workerThreads;

    @Setup
    public void setup() {
        System.out.println("workerThreads: " + workerThreads);
        RpcOptions rpcOptions = new RpcOptions();
        rpcOptions.setExecutorThreads(workerThreads);
        meteor = new Meteor(new LoopbackTransport(), rpcOptions);
        meteor.registerImplementation(new ScoreboardImplementation(), "parkour-leaderboard");
        scoreboardStub = meteor.registerProcedure(PersonalScores.class, "parkour-leaderboard");
    }

    @TearDown
    public void tearDown() throws IOException {
        meteor.stop();
    }

    @Benchmark
    public void benchmarkMethod() {
        scoreboardStub.incrementFor("player1");
    }

    public interface PersonalScores {
        HashMap<String, Integer> getScores();
        HashMap<String, Integer> incrementFor(String name);
    }

    public static class ScoreboardImplementation implements PersonalScores {
        private HashMap<String, Integer> scores = new HashMap<>();

        @Override
        public HashMap<String, Integer> getScores() {
            return scores;
        }

        @Override
        public HashMap<String, Integer> incrementFor(String name) {
            if (scores.containsKey(name)) {
                scores.put(name, scores.get(name) + 1);
            } else {
                scores.put(name, 1);
            }
            return scores;
        }
    }

}
