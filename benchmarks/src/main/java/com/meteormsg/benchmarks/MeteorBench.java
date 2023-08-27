package com.meteormsg.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.All})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class MeteorBench {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(SimpleIncrement.class.getSimpleName())
                .include(ScoresWithMaps.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(options).run();
    }
}
