package kr.pe.karsei.reactorprac;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
public class TestingTest {
    @Test
    void testWithStepVerifier() {
        StepVerifier
                .create(Mono.just("Hello Reactor")) // 테스트 대상 Sequence 생성
                .expectNext("Hello Reactor") // emit 된 데이터 기댓값 평가
                //.expectNext("Helo Reactor") // emit 된 데이터 기댓값 평가
                .expectComplete() // onComplete Signal 기댓값 평가
                .verify(); // 검증 실행
    }
}
