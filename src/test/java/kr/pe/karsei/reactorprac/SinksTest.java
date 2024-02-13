package kr.pe.karsei.reactorprac;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.util.function.ThrowingConsumer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.stream.IntStream;

@Slf4j
public class SinksTest {
    @SneakyThrows
    @Test
    void createOperator() {
        Flux
                .create((ThrowingConsumer<FluxSink<String>>) fluxSink -> IntStream
                        .range(1, 6)
                        .forEach(n -> fluxSink.next(doTasks(n))))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(n -> log.info("# create(): {}", n))

                .publishOn(Schedulers.parallel())
                .map(result -> result + " success!")
                .doOnNext(n -> log.info("# map(): {}", n))

                .publishOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }

    @SneakyThrows
    @Test
    void unicastSink() {
        Sinks.Many<String> unicastSink = Sinks.many().unicast().onBackpressureBuffer();
        Flux<String> sinkFlux = unicastSink.asFlux();

        IntStream
                .range(1, 6)
                .forEach(n -> {
                    try {
                        new Thread(() -> {
                            unicastSink.emitNext(doTasks(n), Sinks.EmitFailureHandler.FAIL_FAST);
                            log.info("# emitted: {}", n);
                        }).start();
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                    }
                });

        sinkFlux
                .publishOn(Schedulers.parallel())
                .map(result -> result + " success!")
                .doOnNext(n -> log.info("# map(): {}", n))

                .publishOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext: {}", data))
                ;

        Thread.sleep(200L);
    }

    private String doTasks(int taskNumber) {
        return "task " + taskNumber + " result";
    }

    @Test
    void sinkOne() {
        Sinks.One<String> sinkOne = Sinks.one();
        Mono<String> mono = sinkOne.asMono();

        sinkOne.emitValue("Hello Reactor", Sinks.EmitFailureHandler.FAIL_FAST);
        // sinkOne.emitValue("Hi Reactor", Sinks.EmitFailureHandler.FAIL_FAST);

        mono.subscribe(data -> log.info("# subscriber1 : {}", data));
        mono.subscribe(data -> log.info("# subscriber2 : {}", data));
    }

    @Test
    void sinkMany() {
        Sinks.Many<Integer> unicastSink = Sinks.many().unicast().onBackpressureBuffer();
        Flux<Integer> sinkFlux = unicastSink.asFlux();

        unicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        unicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        sinkFlux.subscribe(data -> log.info("# subscriber1 : {}", data));
        unicastSink.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);
        // sinkFlux.subscribe(data -> log.info("# subscriber2 : {}", data));
    }
}
