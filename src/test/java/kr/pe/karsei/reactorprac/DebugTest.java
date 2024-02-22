package kr.pe.karsei.reactorprac;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Slf4j
public class DebugTest {
    @Test
    void operatorDebugTest() throws InterruptedException {
        Map<String, String> fruits = new HashMap<>() {{
            put("banana", "바나나");
            put("apple", "사과");
            put("pear", "배");
            put("grape", "포도");
        }};

        Hooks.onOperatorDebug();

        Flux
                .fromArray(new String[] {"BANANAS", "APPLES", "PEARS", "MELONS"})
                .subscribeOn(Schedulers.boundedElastic())
                .publishOn(Schedulers.parallel())
                .map(String::toLowerCase)
                .map(fruit -> fruit.substring(0, fruit.length() - 1))
                .map(fruits::get)
                .map(translated -> "맛있는 " + translated)
                .subscribe(
                        log::info,
                        error -> log.error("# onError: ", error)
                );

        Thread.sleep(100L);
    }

    @Test
    void tracebackTest() {
        Flux
                .just(2, 4, 6, 8)
                .zipWith(Flux.just(1, 2, 3, 0), (x, y) -> x / y)
                .map(num -> num + 2)
                .checkpoint()
                .subscribe(
                        data -> log.info("# onNext: {}", data),
                        error -> log.error("# onError: ", error)
                );
    }

    @Test
    void logTest() {
        Map<String, String> fruits = new HashMap<>() {{
            put("banana", "바나나");
            put("apple", "사과");
            put("pear", "배");
            put("grape", "포도");
        }};

        Flux
                .fromArray(new String[] {"BANANAS", "APPLES", "PEARS", "MELONS"})
                .map(String::toLowerCase)
                .map(fruit -> fruit.substring(0, fruit.length() - 1))
                // .log()
                .log("Fruit.Substring", Level.FINE)
                .map(fruits::get)
                .subscribe(
                        log::info,
                        error -> log.error("# onError: ", error));
    }
}
