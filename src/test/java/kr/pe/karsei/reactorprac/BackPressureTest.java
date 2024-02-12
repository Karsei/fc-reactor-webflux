package kr.pe.karsei.reactorprac;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.function.Consumer;

@Slf4j
public class BackPressureTest {
    @Test
    @DisplayName("데이터 개수 제어")
    void controlRequestDataCount() {
        Flux
                .range(1, 5)
                .doOnRequest(value -> log.info("# doOnRequest: {}", value))
                // Subscriber가 적절히 처리할 수 있는 수준의 데이터 개수를 Publisher에게 요청
                .subscribe(new BaseSubscriber<>() {
                    @Override
                    // 구독 시점에 최초 데이터 요청 개수 제어하는 역할
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @SneakyThrows
                    @Override
                    // Publisher가 emit 한 데이터를 전달받아 처리한 후 Publisher에게 또 다시 데이터를 요청하는 역할 
                    protected void hookOnNext(Integer value) {
                        Thread.sleep(2000L);
                        log.info("# hookOnNext: {}", value);
                        request(1);
                    }
                });
    }

    @SneakyThrows
    @Test
    void usingBackPressureStrategiesOnError() {
        Flux
                // 1씩 증가하면서 0.001초에 한 번씩 증가하도록 설정
                .interval(Duration.ofMillis(1L))
                // ERROR 전략 사용 - Exception 발생 시킴
                .onBackpressureError()
                // Publisher가 emit한 데이터 확인
                .doOnNext(data -> log.info("# doOnNext: {}", data))
                // 별도의 스레드로 진행
                .publishOn(Schedulers.parallel())
                // Subscriber 에서 0.005초 마다 처리하도록 하여 일부러 느리게 작동되도록 설정
                // onError 로그에서 The receiver is overrun by more signals than expected (bounded queue...) 가 등장
                .subscribe(data -> {
                    try {
                        Thread.sleep(Duration.ofMillis(5L));
                    } catch (InterruptedException ignored) {}
                    log.info("# onNext: {}", data);
                }, throwable -> log.error("# onError: {}", throwable.getMessage()));

        Thread.sleep(2000L);
    }

    @SneakyThrows
    @Test
    void usingBackPressureStrategiesOnDrop() {
        Flux
                // 1씩 증가하면서 0.001초에 한 번씩 증가하도록 설정
                .interval(Duration.ofMillis(1L))
                // DROP 전략 사용 - 버퍼 밖에서 대기 중인 먼저 emit 된 데이터부터 DROP
                .onBackpressureDrop(dropped -> log.info("# dropped: {}", dropped))
                // 별도의 스레드로 진행
                .publishOn(Schedulers.parallel())
                // Subscriber 에서 0.005초 마다 처리하도록 하여 일부러 느리게 작동되도록 설정
                // 버퍼 밖에서 대기 중인 데이터 중에서 먼저 emit 된 데이터부터 drop 됨
                .subscribe(data -> {
                    try {
                        Thread.sleep(Duration.ofMillis(5L));
                    } catch (InterruptedException ignored) {}
                    log.info("# onNext: {}", data);
                }, throwable -> log.error("# onError: {}", throwable.getMessage()));

        Thread.sleep(2000L);
    }

    @SneakyThrows
    @Test
    void usingBackPressureStrategiesOnLatest() {
        Flux
                // 1씩 증가하면서 0.001초에 한 번씩 증가하도록 설정
                .interval(Duration.ofMillis(1L))
                // LATEST 전략 사용 - 버퍼 밖에서 대기하는 가장 최근에 emit 된 데이터부터 버퍼에 채움
                .onBackpressureLatest()
                // 별도의 스레드로 진행
                .publishOn(Schedulers.parallel())
                // Subscriber 에서 0.005초 마다 처리하도록 하여 일부러 느리게 작동되도록 설정
                // 데이터가 들어올 때마다 이전에 유지하고 있던 데이터가 폐기됨
                .subscribe(data -> {
                    try {
                        Thread.sleep(Duration.ofMillis(5L));
                    } catch (InterruptedException ignored) {}
                    log.info("# onNext: {}", data);
                }, throwable -> log.error("# onError: {}", throwable.getMessage()));

        Thread.sleep(2000L);
    }
}
