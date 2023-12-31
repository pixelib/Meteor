package dev.pixelib.meteor.benchmarks;

import dev.pixelib.meteor.base.RpcOptions;
import dev.pixelib.meteor.base.defaults.LoopbackTransport;
import dev.pixelib.meteor.core.Meteor;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.All})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class SimpleIncrement {

    private Meteor meteor;
    private Scoreboard scoreboardStub;

    @Param({"1", "10", "50"}) // Example parameter values
    private int workerThreads;

    @Setup
    public void setup() {
        System.out.println("workerThreads: " + workerThreads);
        RpcOptions rpcOptions = new RpcOptions();
        rpcOptions.setExecutorThreads(workerThreads);
        meteor = new Meteor(new LoopbackTransport(), rpcOptions);
        meteor.registerImplementation(new ScoreboardImplementation(), "parkour-leaderboard");
        scoreboardStub = meteor.registerProcedure(Scoreboard.class, "parkour-leaderboard");
    }

    @TearDown
    public void tearDown() throws IOException {
        meteor.stop();
    }

    @Benchmark
    public void benchmarkMethod() {
        scoreboardStub.incrementScore();
    }

    public interface Scoreboard {
        int incrementScore();
        int getScore();
    }

    public static class ScoreboardImplementation implements Scoreboard {
        private Integer score = 0;

        @Override
        public int incrementScore() {
            return ++score;
        }

        @Override
        public int getScore() {
            return score;
        }
    }

}
