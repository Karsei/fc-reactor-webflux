package kr.pe.karsei.reactorprac;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MarbleDiagramExampleTest {
    @Test
    void concatWith() {
        Flux<String> flux =
                Mono
                        .justOrEmpty("Steve")
                        // `concatWith` 의 위쪽에 있는 Publisher 의 데이터 소스와 `concatWith` 내부에 있는 Publisher 데이터 소스를 연결
                        // 이는 새로운 Flux 의 데이터 소스가 되어 emit
                        .concatWith(Mono.justOrEmpty("Jobs"));
        flux.subscribe(System.out::println);
    }

    @Test
    void advanced() {
        Flux
                // Flux
                .concat(
                        Flux.just("Mercury", "Venus", "Earth"),
                        Flux.just("Mars", "Jupiter", "Saturn"),
                        Flux.just("Uranus", "Neptune", "Pluto")
                )
                // Mono
                .collectList()
                // List
                .subscribe(planets -> System.out.println(planets));
    }
}
