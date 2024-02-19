# Reactor 연습

## 용어 정리

* Reactive System
  * **반응을 잘하는 시스템** - 클라이언트의 요청에 즉각적으로 응답함으로써 지연 시간을 최소화
  * **빠른 응답성을 바탕으로 유지보수와 확장이 용이한 시스템**을 구축하는데 활용
* Reactive Manifesto
  * MEANS(가치)
    * 주요 통신 수단으로 무엇을 사용할 것인지 표현
    * 비동기 메시지 기반의 통신을 통해 느슨한 결합, 격리성, 위치 투명성 보장
  * FORM(형성)
    * 비동기 메시지 통신 기반하에 탄력성과 회복성을 가지는 시스템이어야 함
      * 탄력성 - 시스템의 작업량이 변화하더라도 일정한 응답을 유지
      * 회복성 - 시스템에 장애가 발생하더라도 응답성을 유지
  * VALUE(수단)
    * 비동기 메시지 기반 통신을 바탕으로 한 회복성과 예측 가능한 규모 확장 알고리즘을 통해 시스템의 처리량을 자동으로 확장하고 축소하는 탄력성을 확보
    * 즉각적으로 응답 가능한 시스셈 구축할 수 있음
* Reactive Programming
  * > In computing, reactive programming is a **declarative programming** paradigm concerned with **data streams** and **the propagation of change**.
  * Reactive System 을 구축하는 데 필요한 프로그래밍 모델
  * 특징
    * 선언형 프로그래밍
      * **동작을 구체적으로 명시하지 않고 목표만 선언한다.**
      * 각 동작에 대해 메서드 체인을 형성하여 한 문장으로 된 코드로 구성한다.
      * 코드 간결화, 가독성 높아짐
    * 데이터 지속 발생
    * 데이터 발생할 때마다 이를 변화하는 이벤트로 보고, 이벤트를 발생시키면서 데이터를 계속 전달
* Reactive Stream
  * Reactive 라이브러리를 어떻게 구현할지 정의해 놓은 별도의 표준화된 사양
  * Data Stream 을 Non-Blocking 이면서 비동기적인 방식으로 처리하기 위한 Reactive 라이브러리의 표준 사양
  * 이를 구현한 것
    * Project Reactor - Spring Webflux 에서 활용함

> **Stream**
> 데이터가 연속적으로 끊임없이 입력으로 들어오는 것. 예) IoT Device 센서로부터 측정된 데이터

> https://en.wikipedia.org/wiki/Reactive_programming

* Upstream / Downstream / Sequence
  * 아래 코드에서 `just`, `filter`, `map`, `subscribe` 순으로 downstream. 역은 upstream
  * 메서드 체이닝
    * 메서드가 하나로 연결된 것처럼 보이는 것
    * `filter`, `map`, `subscribe`
    * 이렇게 Publisher 가 emit 하는 데이터의 연속적인 흐름을 정의한 것 => Sequence

```java
public class Example {
    public static void main(String[] args) {
        Flux
                .just(1, 2, 3, 4, 5, 6)
                .filter(n -> n % 2 == 0)
                .map(n -> n * 2)
                .subscribe(System.out::println);
    }
}
```

## 구성 요소

* publisher
  * 입력으로 들어오는 데이터를 제공하는 역할
  * 데이터를 생성하고 통지하는 역할
* subscriber
  * Publisher 가 제공한 데이터를 전달받아 사용
  * Publisher 로부터 통지된 데이터를 전달받아 처리하는 역할
* subscription
  * Publisher 에 요청할 데이터를 지정하거나 데이터 구독을 취소하는 역할
* processor
  * Publisher, Subscriber 기능 모두 가짐
* data source
  * Publisher 의 입력으로 전달되는 데이터(대표 용어)
* operator
  * Publisher, Subscriber 사이에서 적절한 가공 처리가 이루어지는 것
  * `just`, `map`, `filter` 등

![publisher_subscriber_detail.png](images%2Fpublisher_subscriber_detail.png)

![publisher_subscriber.png](images%2Fpublisher_subscriber.png)

### Publisher

```java
public interface Publisher<T> {
    void subscribe(Subscriber<? super T> s);
}
```

* subscribe
  * 파라미터로 전달받은 Subscriber 를 등록하는 역할

> Kafka 에서의 Pub/Sub 과 Reactive Streams 에서의 Pub/Sub 의 의미는 조금 다르다. Reactive Streams 상에서는 Publisher 가 subscribe 메서드의 파라미터인 Subscriber 를 등록하는 형태로 구독이 이루어진다.

### Subscriber

```java
public interface Subscriber<T> {
    void onSubscribe(Subscription s);
    void onNext(T t);
    void onError(Throwable t);
    void onComplete();
}
```

* onSubscribe
  * 구독 시작 지점에 어떤 처리를 하는 역할
* onNext
  * Publisher 가 통지한 데이터를 처리하는 역할
* onError
  * Publisher 가 데이터 통지를 위한 처리 과정에서 에러가 발생했을 때 해당 에러를 처리하는 역할
* onComplete
  * Publisher 가 데이터 통지를 완료했음을 알릴 때 호출

### Subscription

```java
public interface Subscription {
    void request(long n);
    void cancel();
}
```

* request
  * Publisher 에게 데이터의 개수를 요청
* cancel
  * 구독 해지

### 동작 과정 정리

1. Publisher 가 Subscriber 인터페이스 구현 객체를 subscribe 메서드의 파라미터로 전달
2. Publisher 내부에서는 전달받은 Subscriber 인터페이스 구현 객체의 onSubscribe 메서드를 호출하여 Subscriber 의 구독을 의미하는 Subscription 인터페이스 구현 객체를 Subscriber 에게 전달
3. 호출된 Subscriber 인터페이스 구현 객체의 onSubscribe 메서드에서 전달받은 Subscription 객체를 통해 전달받은 데이터의 개수를 Publisher 에게 요청
4. Publisher 는 Subscriber 로부터 전달받은 요청 개수만큼의 데이터를 onNext 메서드를 호출하여 Subscriber 에게 전달
5. Publisher 는 통지할 데이터가 없을 경우 onComplete 메서드를 호출하여 Subscriber 에게 데이터 처리 종료를 알림

#### asynchronous

비동기 처리 가능

데이터 처리 단계에서 같은 스레드나 별개의 스레드를 동기화 시킬 수 있으며 병렬 처리도 가능

![asyncronous.png](images%2Fasyncronous.png)


## 마블 다이어그램 (Marble Diagram)

여러 가지 구슬 모양의 도형으로 구성된 도표. Reactor 에서 지원하는 Operator 를 이해하는 데 중요한 역할을 한다.

### 예시

![mapForFlux.svg](images%2FmapForFlux.svg)

위 다이어그램은 `map` operator

* 상/하단의 기다란 가로 줄
  * 타임라인 (왼쪽에서 오른쪽으로 시간이 흐름)
  * 상단 - 시간순으로 데이터가 Emit
  * 하단 - emit 된 데이터가 함수를 거쳐 변환됨
    * Operator 의 출력으로 반환된 Flux 의 경우, Output Flux 라고도 불림
* 가운데 커다란 하얀색 박스
  * Publisher 로부터 전달받은 데이터를 처리하는 Operator 함수
* 초록색, 노란색, 파란색 도형
  * 동그라미 - Publisher 가 emit 하는 데이터
  * 사각형 - 함수(operator)의 출력으로 나온 변환된 데이터
* 상/하단의 점선 화살표
  * 상단 - 함수의 입력으로 들어감
  * 하단 - 함수의 출력으로 나옴
* 상/하단의 기다란 줄 맨 오른쪽에 있는 세로 선
  * 데이터 emit 이 정상적으로 끝났음을 의미
  * onComplete signal emit 을 의미

> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#map-java.util.function.Function-

![mono.svg](images%2Fmono.svg)

위 다이어그램은 `Mono`

* 하단의 X
  * 에러가 발생해 비정상적으로 데이터 처리가 종료되었음을 의미
  * onError signal emit 을 의미

> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#Mono--

![concatWithForMono.svg](images%2FconcatWithForMono.svg)

위 다이어그램은 `concatWith` operator

* `concatWith` 의 위쪽에 있는 Publisher 의 데이터 소스와 `concatWith` 내부에 있는 Publisher 데이터 소스를 연결
* 이는 새로운 Flux 의 데이터 소스가 되어 emit

```java
public class Example {
    public static void main(String[] args) {
        Flux<String> flux = 
                Mono.justOrEmpty("Steve")
                        .concatWith(Mono.justOrEmpty("Jobs"));
        flux.subscribe(System.out::println);
    }
}
```

```
Steve
Jobs
```

> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html#concatWith-org.reactivestreams.Publisher-

```java
public class Example {
    public static void main(String[] args) {
        Flux.concat( // 1
                Flux.just("Mercury", "Venus", "Earth"),
                Flux.just("Mars", "Jupiter", "Saturn"),
                Flux.just("Uranus", "Neptune", "Pluto"))
            .collectList() // 2
            .subscribe(planets -> System.out.println(planets)); // 3
    }
}
```

위 코드 기준으로

* 1 에서 반환되는 Publisher ([concat](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#concat-org.reactivestreams.Publisher...-)) - Flux
* 2 에서 반환되는 Publisher ([collectList](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html#collectList--)) - Mono
* 3 에서의 결과
  * `[Mercury, Venus, Earth, Mars, Jupiter, Saturn, Uranus, Neptune, Pluto]`

## Cold / Hot Sequence

* Cold - 무언가를 새로 시작
* Hot - 무언가를 새로 시작하지 않음

## Back Pressure

publisher 가 생성한 것을 subscriber 가 충분히 처리하지 못할 때 불균형 처리 가능

### 처리 방식

#### 데이터 개수 제어

데이터의 요청 개수를 직접적으로 제어할 필요가 있다면 `BaseSubscriber` 인터페이스를 사용하여 데이터 요청 개수를 적절하게 제어 가능

```java
public class Example {
    public static void main(String[] args) {
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
}
```

#### Backpressure 전략 사용

* IGNORE - Backpressure 적용 X
* ERROR - Exception 발생
* DROP - 버퍼 밖에서 대기하는 먼저 Emit 된 데이터부터 DROP
* LATEST - 버퍼 밖에서 대기하는 가장 최근에 emit 된 데이터부터 버퍼에 채움
  * 새로운 데이터가 들어오는 시점에 가장 최근의 데이터만 남겨 두고 나머지 데이터를 폐기
* BUFFER - 버퍼 안에 있는 데이터부터 DROP
  * DROP_LATEST - 가장 최근에 버퍼 안에 채워진 데이터를 DROP 후, 확보된 공간에 emit 된 데이터를 채움
  * DROP_OLDEST - 버퍼 안에 채워진 데이터 중에서 가장 오래된 데이터를 DROP 하여 폐기한 후, 확보된 공간에 emit 된 데이터를 채움

```java
// ERROR 전략 예시
public class Example {
    public static void main(String[] args) {
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
}
```

```
22:27:04.480 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # doOnNext: 0
22:27:04.484 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # doOnNext: 1
22:27:04.484 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # doOnNext: 2
...
22:27:06.048 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 254
22:27:06.054 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 255
22:27:06.054 [parallel-1] ERROR kr.pe.karsei.reactorprac.BackPressureTest -- # onError: The receiver is overrun by more signals than expected (bounded queue...)
```

```java
// DROP 전략 예시
public class Example {
    public static void main(String[] args) {
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
}
```

```
22:23:20.608 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 0
22:23:20.619 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 1
22:23:20.625 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 2
...
22:23:20.847 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 37
22:23:20.855 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 38
22:23:20.856 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # dropped: 256
22:23:20.857 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # dropped: 257
...
22:23:22.178 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # dropped: 1577
22:23:22.178 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 255
22:23:22.180 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # dropped: 1578
22:23:22.180 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # dropped: 1579
22:23:22.181 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # dropped: 1580
22:23:22.183 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # dropped: 1581
22:23:22.183 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # dropped: 1582
22:23:22.184 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # dropped: 1583
22:23:22.184 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 1196 // dropped 된 데이터 건너뜀
22:23:22.185 [parallel-2] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # dropped: 1584
```

```java
// LATEST 전략 예시
public class Example {
    public static void main(String[] args) {
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
```

```
22:29:16.543 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 0
22:29:16.552 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 1
22:29:16.557 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 2
...
22:29:18.127 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 254
22:29:18.133 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 255
22:29:18.139 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 1196
22:29:18.145 [parallel-1] INFO kr.pe.karsei.reactorprac.BackPressureTest -- # onNext: 1197
```

## Sink

Processor(Publisher 와 Subscriber 의 기능을 모두 지님)의 기능을 개선한 것

"Reactive Streams 의 Signal 을 프로그래밍 방식으로 Push 할 수 있는 구조이며 Flux 또는 Mono 의 의미 체계를 가진다."

> Sinks are constructs through which Reactive Streams signals can be programmatically pushed, with Flux or Mono semantics.

> https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Sinks.html
 
즉, Flux 또는 Mono 가 `onNext` 같은 signal 을 내부적으로 전송해주는 방식으로 제공한다면, Sinks 를 사용하면 프로그래밍 코드를 통해 명시적으로 Signal 을 전송할 수 있음

* Operator(`generate()`, `create()`) 는 싱글스레드 기반에서 Signal 전송을 하는 반면에, Sinks 는 멀티스레드 방식으로 Signal 전송
  * 스레드 안전성 보장함

```java
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

    private String doTasks(int taskNumber) {
        return "task " + taskNumber + " result";
    }
}
```

```
22:54:26.523 [boundedElastic-1] INFO kr.pe.karsei.reactorprac.SinksTest -- # create(): task 1 result
22:54:26.526 [boundedElastic-1] INFO kr.pe.karsei.reactorprac.SinksTest -- # create(): task 2 result
22:54:26.526 [parallel-2] INFO kr.pe.karsei.reactorprac.SinksTest -- # map(): task 1 result success!
22:54:26.526 [boundedElastic-1] INFO kr.pe.karsei.reactorprac.SinksTest -- # create(): task 3 result
22:54:26.526 [parallel-1] INFO kr.pe.karsei.reactorprac.SinksTest -- # onNext: task 1 result success!
22:54:26.526 [parallel-2] INFO kr.pe.karsei.reactorprac.SinksTest -- # map(): task 2 result success!
```

위 결과를 보면 총 3개의 스레드가 실행되고 있는 것을 알 수 있고, `create()` 메서드를 통해 doTask() 메서드를 실행함으로써 Signal 을 전달하고 있는 것을 알 수 있다.

```java
public class SinksTest {
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
}
```

```
23:02:09.114 [Thread-3] INFO kr.pe.karsei.reactorprac.SinksTest -- # emitted: 1
23:02:09.211 [Thread-4] INFO kr.pe.karsei.reactorprac.SinksTest -- # emitted: 2
23:02:09.313 [Thread-5] INFO kr.pe.karsei.reactorprac.SinksTest -- # emitted: 3
23:02:09.415 [Thread-6] INFO kr.pe.karsei.reactorprac.SinksTest -- # emitted: 4
23:02:09.517 [Thread-7] INFO kr.pe.karsei.reactorprac.SinksTest -- # emitted: 5
23:02:09.652 [parallel-2] INFO kr.pe.karsei.reactorprac.SinksTest -- # map(): task 1 result success!
23:02:09.652 [parallel-2] INFO kr.pe.karsei.reactorprac.SinksTest -- # map(): task 2 result success!
23:02:09.652 [parallel-1] INFO kr.pe.karsei.reactorprac.SinksTest -- # onNext: task 1 result success!
23:02:09.652 [parallel-2] INFO kr.pe.karsei.reactorprac.SinksTest -- # map(): task 3 result success!
23:02:09.652 [parallel-1] INFO kr.pe.karsei.reactorprac.SinksTest -- # onNext: task 2 result success!
23:02:09.652 [parallel-2] INFO kr.pe.karsei.reactorprac.SinksTest -- # map(): task 4 result success!
23:02:09.652 [parallel-1] INFO kr.pe.karsei.reactorprac.SinksTest -- # onNext: task 3 result success!
23:02:09.652 [parallel-2] INFO kr.pe.karsei.reactorprac.SinksTest -- # map(): task 5 result success!
23:02:09.652 [parallel-1] INFO kr.pe.karsei.reactorprac.SinksTest -- # onNext: task 4 result success!
23:02:09.652 [parallel-1] INFO kr.pe.karsei.reactorprac.SinksTest -- # onNext: task 5 result success!
```

위 로그를 보면 총 7개의 스레드가 실행된 것을 확인할 수 있고, 루프를 돌 때마다 새로운 스레드가 생성되어 여러 개의 스레드에서도 사용이 가능하다. 스레드 안전성을 보장받을 수 있는 장점이 있음

### Sinks.One

한 건의 데이터를 전송하는 방법을 정의해 둔 기능 명세

```java
public class SinksTest {
    @Test
    void sinkOne() {
        Sinks.One<String> sinkOne = Sinks.one();
        Mono<String> mono = sinkOne.asMono();
    
        sinkOne.emitValue("Hello Reactor", Sinks.EmitFailureHandler.FAIL_FAST);
        // sinkOne.emitValue("Hi Reactor", Sinks.EmitFailureHandler.FAIL_FAST);
    
        mono.subscribe(data -> log.info("# subscriber1 : {}", data));
        mono.subscribe(data -> log.info("# subscriber2 : {}", data));
    }
}
```

```
23:08:44.304 [Test worker] INFO kr.pe.karsei.reactorprac.SinksTest -- # subscriber1 : Hello Reactor
23:08:44.309 [Test worker] INFO kr.pe.karsei.reactorprac.SinksTest -- # subscriber2 : Hello Reactor
```

위에서 주석을 해제해도 똑같은 결과가 나온다. 아무리 많은 수의 데이터를 emit 한다 하더라도 **처음 emit 한 데이터는 정상적으로 emit 되지만 나머지 데이터들은 drop 된다.**

### Sinks.Many

여러 건의 데이터를 여러 가지 방식으로 전송하는 기능을 정의해 둔 기능 명세

* broadcast - 네트워크에 연결된 모든 시스템이 정보를 전달받는 방식 (One to All)
* unicast - 하나의 특정 시스템만 정보를 전달받는 방식 (One to One)
* multicast - 일부 시스템들만 정보를 전달받는 방식 (One to Many)

```java
public class SinksTest {
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
```

```
23:19:34.472 [Test worker] INFO kr.pe.karsei.reactorprac.SinksTest -- # subscriber1 : 1
23:19:34.476 [Test worker] INFO kr.pe.karsei.reactorprac.SinksTest -- # subscriber1 : 2
23:19:34.476 [Test worker] INFO kr.pe.karsei.reactorprac.SinksTest -- # subscriber1 : 3
```

만약 위에 있는 주석을 풀면 아래와 같이 나타난다.

```
23:22:06.563 [Test worker] INFO kr.pe.karsei.reactorprac.SinksTest -- # subscriber1 : 1
23:22:06.566 [Test worker] INFO kr.pe.karsei.reactorprac.SinksTest -- # subscriber1 : 2
23:22:06.566 [Test worker] INFO kr.pe.karsei.reactorprac.SinksTest -- # subscriber1 : 3
23:22:06.567 [Test worker] ERROR reactor.core.publisher.Operators -- Operator called default onErrorDropped
reactor.core.Exceptions$ErrorCallbackNotImplemented: java.lang.IllegalStateException: Sinks.many().unicast() sinks only allow a single Subscriber
Caused by: java.lang.IllegalStateException: Sinks.many().unicast() sinks only allow a single Subscriber
	at reactor.core.publisher.SinkManyUnicast.subscribe(SinkManyUnicast.java:426)
```

위처럼 오류가 나타나는 이유는 `UnicastSpec` 의 기능이 단 하나의 Subscriber 에게만 데이터를 Emit 하는 것이기 때문에 두 번째 Subscriber 에게는 허용하지 않기 때문이다.

만약 위 코드에서 `unicast` 를 `multicast` 로 변경하면 제대로 작동된다.

Sinks 가 Publisher 의 역할을 할 경우 기본적으로 **Hot** Publisher 로 동작한다. (특히, `onBackpressureBuffer` 메서드는 Warm Up 의 특징을 가지는 Hot Sequence 로 동작한다)

## Scheduler

Reactor Sequence 에서 **스레드를 관리해 주는 관리자 역할**

운영체제의 Scheduler 의 의미와 비슷하고 Scheduler 를 사용하여 어떤 스레드에서 무엇을 처리할지 제어

Scheduler 를 사용하면 코드 자체가 매우 간결해지고, Scheduler 가 스레드의 제어를 대신 해주므로 개발자 부담 감소

### Operator

#### subscribeOn()

구독이 발생한 직후 실행될 스레드를 지정

원본 Publisher 의 동작을 수행하기 위한 스레드라고 볼 수 있음

```java
public class SchedulerTest {
    @SneakyThrows
    @Test
    void subscribeOn() {
        Flux
                .fromArray(new Integer[] {1, 3, 5, 7})
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(data -> log.info("# doOnNext: {}", data))
                // 구독 시점의 스레드는 메인 스레드에서 진행됨
                .doOnSubscribe(subscription -> log.info("# doOnSubscribe"))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(500L);
    }
}
```

```
22:52:34.440 [Test worker] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # doOnSubscribe
22:52:34.444 [boundedElastic-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # doOnNext: 1
22:52:34.445 [boundedElastic-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 1
22:52:34.445 [boundedElastic-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # doOnNext: 3
22:52:34.445 [boundedElastic-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 3
...
```

위 코드에서 subscribeOn() 을 추가하지 않으면 여전히 메인 스레드에서 진행됨

#### publishOn()

코드 상에서 publishOn 을 기준으로 아래쪽인 Downstream 의 실행 스레드를 변경

operator 체인 상에서 한 개 이상을 사용할 수 있음

```java
public class SchedulerTest {
    @SneakyThrows
    @Test
    void publishOn() {
        Flux
                .fromArray(new Integer[] {1, 3, 5, 7})
                .doOnNext(data -> log.info("# doOnNext: {}", data))
                .doOnSubscribe(subscription -> log.info("# doOnSubscribe"))
                .publishOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext: {}", data));
    
        Thread.sleep(500L);
    }
}
```

```
22:55:25.547 [Test worker] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # doOnSubscribe
22:55:25.553 [Test worker] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # doOnNext: 1
22:55:25.554 [Test worker] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # doOnNext: 3
22:55:25.554 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 1
22:55:25.554 [Test worker] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # doOnNext: 5
22:55:25.554 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 3
22:55:25.554 [Test worker] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # doOnNext: 7
22:55:25.554 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 5
22:55:25.554 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 7
```

#### parallel()

라운드 로빈 방식으로 CPU 코어(논리 코어, 물리 스레드) 개수만큼의 스레드를 병렬로 실행

```java
public class SchedulerTest {
    @SneakyThrows
    @Test
    void parallel() {
        Flux
                .fromArray(new Integer[] {1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23})
                .parallel()
                //.parallel(4)
                .runOn(Schedulers.parallel())
                .subscribe(data -> log.info("# onNext: {}", data));
    
        Thread.sleep(100L);
    }
}
```

실습을 진행하는 컴퓨터 사양의 논리 프로세서의 개수가 12이므로 12개의 스레드가 생성되어 병렬로 진행됨

![parallel_for_pc_threads.png](images%2Fparallel_for_pc_threads.png)

```
22:58:18.624 [parallel-10] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 19
22:58:18.624 [parallel-2] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 3
22:58:18.624 [parallel-7] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 13
22:58:18.624 [parallel-3] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 5
22:58:18.624 [parallel-11] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 21
22:58:18.624 [parallel-4] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 7
22:58:18.624 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 1
22:58:18.624 [parallel-5] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 9
22:58:18.624 [parallel-8] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 15
22:58:18.624 [parallel-12] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 23
22:58:18.624 [parallel-9] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 17
22:58:18.624 [parallel-6] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 11
```

만약 일부의 스레드 개수만 사용하고 싶다면, 주석처럼 숫자를 지정하면 해당 개수 만큼의 스레드가 병렬로 실행됨

```
23:01:21.458 [parallel-2] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 3
23:01:21.458 [parallel-4] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 7
23:01:21.458 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 1
23:01:21.461 [parallel-4] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 15
23:01:21.458 [parallel-3] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 5
23:01:21.461 [parallel-2] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 11
23:01:21.461 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 9
23:01:21.462 [parallel-4] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 23
23:01:21.462 [parallel-3] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 13
23:01:21.462 [parallel-3] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 21
23:01:21.462 [parallel-2] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 19
23:01:21.462 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 17
```

### publishOn, subscribeOn 을 같이 사용하면?

* publishOn 은 한 개 이상 사용할 수 있으며, 실행 스레드를 목적에 맞게 적절하게 분리할 수 있음
* subscribeOn 은 operator 체인 상에서 어떤 위치에 있든 간에 구독 시점 직후, 즉 Publisher 가 데이터를 emit 하기 전에 실행 스레드를 변경

위 두 개를 함께 사용해서 원본 Publisher 에서 데이터를 emit 하는 스레드와 emit 된 데이터를 가공 처리하는 스레드를 적절하게 분리할 수 있음

### Scheduler 종류

#### Schedulers.immediate()

별도의 스레드를 추가적으로 생성하지 않고, 현재 스레드에서 작업을 처리하고자 할 때 사용

```java
@Slf4j
public class SchedulerTest {
    @SneakyThrows
    @Test
    void immediate() {
        Flux
                .fromArray(new Integer[] {1, 3, 5, 7})
                .publishOn(Schedulers.parallel())
                .filter(data -> data > 3)
                .doOnNext(data -> log.info("# doOnNext filter: {}", data))

                .publishOn(Schedulers.immediate())
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# doOnNext map: {}", data))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(200L);
    }
}
```

```
23:08:42.306 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # doOnNext filter: 5
23:08:42.310 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # doOnNext map: 50
23:08:42.310 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 50
23:08:42.311 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # doOnNext filter: 7
23:08:42.311 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # doOnNext map: 70
23:08:42.311 [parallel-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 70
```

Scheduler 가 필요한 경우가 있긴 한데, 별도의 스레드를 추가 할당하고 싶지 않을 경우에 사용된다.

#### Schedulers.single()

스레드 하나만 생성해서 Scheduler 가 제거되기 전까지 재사용하는 방식

```java
@Slf4j
public class SchedulerTest {
    @SneakyThrows
    @Test
    void schedulerSingle() {
        doTask("task1")
                .subscribe(data -> log.info("# onNext: {}", data));
        doTask("task2")
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(200L);
    }

    private static Flux<Integer> doTask(String taskName) {
        return Flux
                .fromArray(new Integer[]{1, 3, 5, 7})
                .publishOn(Schedulers.single())
                .filter(data -> data > 3)
                .doOnNext(data -> log.info("# {} doOnNext filter: {}", taskName, data))
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# {} doOnNext map: {}", taskName, data));
    }
}
```

```
23:12:20.433 [single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task1 doOnNext filter: 5
23:12:20.436 [single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task1 doOnNext map: 50
23:12:20.436 [single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 50
23:12:20.436 [single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task1 doOnNext filter: 7
23:12:20.436 [single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task1 doOnNext map: 70
23:12:20.436 [single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 70
23:12:20.437 [single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task2 doOnNext filter: 5
23:12:20.437 [single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task2 doOnNext map: 50
23:12:20.437 [single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 50
23:12:20.437 [single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task2 doOnNext filter: 7
23:12:20.437 [single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task2 doOnNext map: 70
23:12:20.438 [single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 70
```

doTask 가 두 번 호출되었으나 여전히 하나의 스레드만을 사용하면서 재사용하는 모습을 확인할 수 있음

#### Schedulers.newSingle()

호출할 때마다 매번 새로운 스레드 하나를 생성

```java
@Slf4j
public class SchedulerTest {
    @SneakyThrows
    @Test
    void schedulerNewSingle() {
        doTaskWithNewSingle("task1")
                .subscribe(data -> log.info("# onNext: {}", data));
        doTaskWithNewSingle("task2")
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(200L);
    }

    private static Flux<Integer> doTaskWithNewSingle(String taskName) {
        return Flux
                .fromArray(new Integer[]{1, 3, 5, 7})
                .publishOn(Schedulers.newSingle("new-single", true))
                .filter(data -> data > 3)
                .doOnNext(data -> log.info("# {} doOnNext filter: {}", taskName, data))
                .map(data -> data * 10)
                .doOnNext(data -> log.info("# {} doOnNext map: {}", taskName, data));
    }
}
```

```
23:14:32.838 [new-single-2] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task2 doOnNext filter: 5
23:14:32.838 [new-single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task1 doOnNext filter: 5
23:14:32.842 [new-single-2] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task2 doOnNext map: 50
23:14:32.842 [new-single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task1 doOnNext map: 50
23:14:32.842 [new-single-2] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 50
23:14:32.842 [new-single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 50
23:14:32.842 [new-single-2] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task2 doOnNext filter: 7
23:14:32.842 [new-single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task1 doOnNext filter: 7
23:14:32.842 [new-single-2] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task2 doOnNext map: 70
23:14:32.842 [new-single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # task1 doOnNext map: 70
23:14:32.842 [new-single-2] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 70
23:14:32.842 [new-single-1] INFO kr.pe.karsei.reactorprac.SchedulerTest -- # onNext: 70
```

#### Schedulers.boundedElastic()

`ExecutorService` 기반의 스레드 풀을 생성한 후, 그 안에서 정해진 수만큼의 스레드를 사용하여 작업을 처리하고 작업이 종료된 스레드는 반납하여 재사용하는 방식

기본적으로 CPU 코어 수 * 10 만큼의 스레드를 생성하며, 풀에 있는 모든 스레드가 작업을 처리하고 있을 경우 이용 가능한 스레드가 생길 때까지 최대 100,000개의 작업이 큐에서 대기할 수 있음

**Blocking I/O 작업을 효과적으로 처리하기 위한 방식**. 실행 시간이 긴 작업이 포함된 경우, 다른 Non-blocking 처리에 영향을 주지 않도록 전용 스레드를 할당하여 Blocking I/O 작업을 처리

#### Schedulers.parallel()

Schedulers.boundedElastic() 와 다르게 Non-Blocking I/O 에 최적화되어 있고, CPU 코어 수만큼의 스레드 생성

#### Schedulers.fromExecutorService()

기존에 이미 사용하고 있는 `ExecutorService` 가 있다면 이 `ExecutorService` 로부터 Scheduler 를 생성하는 방식

Reactor 에서는 이 방식을 권장하지 않음

#### Schedulers.newXXXX()

스레드 이름, 생성 가능한 디폴트 스레드의 개수, 스레드의 유휴 시간, 데몬 스레드로의 동작 여부 등을 직접 지정하여 커스텀한 스레드 풀을 새로 생성할 수 있음

* Schedulers.newSingle()
* Schedulers.newBoundedElastic()
* Schedulers.newParallel()

## Context

어떠한 상황에서 그 상황을 처리하기 위해 필요한 정보

Reactor 의 Context 는 각각의 실행 스레드와 매핑되는 ThreadLocal 과 다르게 Subscriber 와 매핑됨. 즉, 구독이 발생할 때마다 해당 구독과 연결된 하나의 Context 가 생김

Operator 체인 상의 서로 다른 스레드들이 Context의 저장된 데이터에 손쉽게 접근 가능

Context 에 데이터를 쓴 후에는 매번 불변 객체를 다음 `contextWrite()` operator 에 전달함으로써 스레드 안전성을 보장함

* Context 에 데이터 쓰기 - Context API
* Context 에서 데이터 읽기 - ContextView API

### Context API

자주 사용되는 것들

* put(key, value)
* of(key1, value1, key2, value2)
  * 여섯 개 이상의 데이터를 쓰기 위해서는 `putAll` 을 사용해야 함
* putAll(ContextView)
* delete(key)

> https://projectreactor.io/docs/core/release/api

```java
@Slf4j
public class ContextTest {
    @SneakyThrows
    @Test
    void contextTest() {
        final String key1 = "company";
        final String key2 = "firstName";
        final String key3 = "lastName";

        Mono
                .deferContextual(ctx ->
                        Mono.just(ctx.get(key1) + ", " + ctx.get(key2) + " " + ctx.get(key3))
                )
                .publishOn(Schedulers.parallel())
                .contextWrite(ctx -> ctx.putAll(
                        Context
                                .of(key2, "Steve" , key3, "Jobs") // Context
                                .readOnly() // ContextView
                ))
                .contextWrite(ctx -> ctx.put(key1, "Apple"))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(100L);
    }
}
```

```
22:22:07.239 [parallel-1] INFO kr.pe.karsei.reactorprac.ContextTest -- # onNext: Apple, Steve Jobs
```

### ContextView

Java Collection 중 `Map`에서 데이터를 읽는 것과 유사

자주 사용되는 것들

* get(key)
* getOrEmpty(key)
* getOrDefault(key, default value)
* hasKey(key)
* isEmpty()
* size()

```java
@Slf4j
public class ContextTest {
    @SneakyThrows
    @Test
    void contextViewTest() {
        final String key1 = "company";
        final String key2 = "firstName";
        final String key3 = "lastName";

        Mono
                .deferContextual(ctx ->
                        Mono.just(
                                ctx.get(key1) + ", " +
                                        ctx.getOrEmpty(key2).orElse("no firstName") + " " +
                                        ctx.getOrDefault(key3, "no lastName"))
                )
                .publishOn(Schedulers.parallel())
                .contextWrite(ctx -> ctx.put(key1, "Apple"))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(100L);
    }
}
```
```
22:31:39.015 [parallel-1] INFO kr.pe.karsei.reactorprac.ContextTest -- # onNext: Apple, no firstName no lastName
```

### 특징

* 구독이 발생할 때마다 해당하는 **하나의 Context 가 하나의 구독에 연결**된다.
   * 얼핏 보면 두 개의 데이터가 하나의 Context 에 저장되는 것처럼 보일 수 있으므로 주의
* Context 는 **Operator 체인의 아래에서 위로 전파**된다.
* 동일 키에 대한 값을 중복 저장 시 가장 위쪽에 저장한 `contextWrite()` 이 저장한 값으로 덮어쓴다.
* 인증 정보 같은 직교성(독립성)을 가지는 정보를 전송하는 데 적합함

```java
@Slf4j
public class ContextTest {
    @SneakyThrows
    @Test
    void contextConnectTest() {
        final String key1 = "company";

        Mono<String> mono = Mono
                .deferContextual(ctx ->
                        Mono.just("Company: " + " " + ctx.get(key1))
                )
                .publishOn(Schedulers.parallel());

        mono
                .contextWrite(ctx -> ctx.put(key1, "Apple"))
                .subscribe(data -> log.info("# subscribe1 onNext: {}", data));

        mono
                .contextWrite(ctx -> ctx.put(key1, "Microsoft"))
                .subscribe(data -> log.info("# subscribe2 onNext: {}", data));

        Thread.sleep(100L);
    }
}
```
```
22:38:41.826 [parallel-1] INFO kr.pe.karsei.reactorprac.ContextTest -- # subscribe1 onNext: Company:  Apple
22:38:41.826 [parallel-2] INFO kr.pe.karsei.reactorprac.ContextTest -- # subscribe2 onNext: Company:  Microsoft
```

위 예제에서 구독이 발생할 때마다 context 도 따로 별개로 연결되는 것을 알 수 있다.

```java
@Slf4j
public class ContextTest {
    @SneakyThrows
    @Test
    void contextDuplicateTest() {
        final String key1 = "company";
        
        Mono
                .just("Steve")
                //.transformDeferredContextual((stringMono, ctx) -> ctx.get("role"))
                .flatMap(name -> Mono.deferContextual(ctx ->
                        Mono
                                .just(ctx.get(key1) + ", " + name)
                                .transformDeferredContextual((mono, innerCtx) -> 
                                        mono.map(data -> data + ", " + innerCtx.get("role"))
                                )
                                .contextWrite(context -> context.put("role", "CEO"))
                        )
                )
                .publishOn(Schedulers.parallel())
                .contextWrite(context -> context.put(key1, "Apple"))
                .subscribe(data -> log.info("# onNext: {}", data));
        
        Thread.sleep(100L);
    }
}
```
```
23:08:05.309 [parallel-1] INFO kr.pe.karsei.reactorprac.ContextTest -- # onNext: Apple, Steve, CEO
```

위의 주석을 풀면 아래처럼 오류가 나타난다. `role` 이라는 키가 없기 때문이며 Inner Sequence 외부에서는 Inner Sequence 내부 Context 에 저장된 데이터를 읽을 수 없다.

```
23:08:51.212 [parallel-1] ERROR reactor.core.publisher.Operators -- Operator called default onErrorDropped
reactor.core.Exceptions$ErrorCallbackNotImplemented: java.util.NoSuchElementException: Context does not contain key: role
Caused by: java.util.NoSuchElementException: Context does not contain key: role
	at reactor.util.context.Context1.get(Context1.java:68)
```

## Debugging

Reactor 는 처리되는 작업들이 대부분 비동기적으로 실행되고, 선언형 프로그래밍 방식으로 구성되므로 디버깅이 쉽지 않다.

### Debug mode 를 활용하는 방법

```java
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

        //Hooks.onOperatorDebug();

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
}
```
```
23:49:13.323 [parallel-1] INFO kr.pe.karsei.reactorprac.DebugTest -- 맛있는 바나나
23:49:13.326 [parallel-1] INFO kr.pe.karsei.reactorprac.DebugTest -- 맛있는 사과
23:49:13.327 [parallel-1] INFO kr.pe.karsei.reactorprac.DebugTest -- 맛있는 배
23:49:13.329 [parallel-1] ERROR kr.pe.karsei.reactorprac.DebugTest -- # onError: 
java.lang.NullPointerException: The mapper [kr.pe.karsei.reactorprac.DebugTest$$Lambda$412/0x000002a601194a08] returned a null value.
```

NPE 오류가 나긴 하지만 정확하게 어떤 부분에서 오류가 났는지 정보가 부족하다.

위 코드에서 주석을 풀고 다시 실행하면 아래처럼 자세하게 나타난다.

```
23:50:07.785 [parallel-1] INFO kr.pe.karsei.reactorprac.DebugTest -- 맛있는 바나나
23:50:07.789 [parallel-1] INFO kr.pe.karsei.reactorprac.DebugTest -- 맛있는 사과
23:50:07.789 [parallel-1] INFO kr.pe.karsei.reactorprac.DebugTest -- 맛있는 배
23:50:07.793 [parallel-1] ERROR kr.pe.karsei.reactorprac.DebugTest -- # onError: 
java.lang.NullPointerException: The mapper [kr.pe.karsei.reactorprac.DebugTest$$Lambda$413/0x0000023f1ed952c0] returned a null value.
	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:115)
	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Assembly trace from producer [reactor.core.publisher.FluxMapFuseable] :
	reactor.core.publisher.Flux.map(Flux.java:6517)
	kr.pe.karsei.reactorprac.DebugTest.operatorDebugTest(DebugTest.java:31)
Error has been observed at the following site(s):
	*__Flux.map ? at kr.pe.karsei.reactorprac.DebugTest.operatorDebugTest(DebugTest.java:31)
	|_ Flux.map ? at kr.pe.karsei.reactorprac.DebugTest.operatorDebugTest(DebugTest.java:32)
```

`Hooks.onOperatorDebug()`으로 디버그 모드를 활성화하면 에러가 발생한 지점을 좀 더 명확하게 찾을 수 있다. 그러나 애플리케이션 내에서 비용이 많이 드는 동작 과정을 거치므로 처음부터 디버그 모드를 활성화하는 것은 권장하지 않는다.

> **동작 과정**
> 1. 애플리케이션 내 모든 operator 의 Stacktrace 를 캡처한다.
> 2. 오류가 발생하면 캡쳐한 정보를 기반으로 오류가 발생한 Assembly 의 Stacktrace 를 Original Stacktrace 중간에 끼워 넣는다.

### `checkpoint()` operator 활용 

특정 operator 체인 내의 Stacktrace 만 캡처한다.

#### traceback 출력

`checkpoint()`를 사용하면 실제 오류가 발생한 assembly 지점 또는 오류가 전파된 assembly 지점의 traceback 이 추가된다.

```java
@Slf4j
public class DebugTest {
    @Test
    void tracebackTest() {
        Flux
                .just(2, 4, 6, 8)
                .zipWith(Flux.just(1, 2, 3, 0), (x, y) -> x / y)
                //.checkpoint()
                .map(num -> num + 2)
                .checkpoint()
                .subscribe(
                        data -> log.info("# onNext: {}", data),
                        error -> log.error("# onError: ", error)
                );
    }
}
```
```
23:56:24.371 [Test worker] INFO kr.pe.karsei.reactorprac.DebugTest -- # onNext: 4
23:56:24.375 [Test worker] INFO kr.pe.karsei.reactorprac.DebugTest -- # onNext: 4
23:56:24.375 [Test worker] INFO kr.pe.karsei.reactorprac.DebugTest -- # onNext: 4
23:56:24.381 [Test worker] ERROR kr.pe.karsei.reactorprac.DebugTest -- # onError: 
java.lang.ArithmeticException: / by zero
	at kr.pe.karsei.reactorprac.DebugTest.lambda$tracebackTest$3(DebugTest.java:45)
	Suppressed: reactor.core.publisher.FluxOnAssembly$OnAssemblyException: 
Assembly trace from producer [reactor.core.publisher.FluxMap] :
	reactor.core.publisher.Flux.checkpoint(Flux.java:3559)
	kr.pe.karsei.reactorprac.DebugTest.tracebackTest(DebugTest.java:47)
Error has been observed at the following site(s):
	*__checkpoint() ? at kr.pe.karsei.reactorprac.DebugTest.tracebackTest(DebugTest.java:47)
Original Stack Trace:
		at kr.pe.karsei.reactorprac.DebugTest.lambda$tracebackTest$3(DebugTest.java:45)
```

위에 있는 `checkpoint()` 지점이 오류와 관련이 있음을 알 수 있다. 하나로는 알기 어렵기 때문에 위의 주석을 풀면 `zipWith` 부분에서 오류가 있음을 추정할 수 있다.

`checkpoint(description, forceStackTrace)`를 사용해서 traceback 과 description 모두를 출력할 수도 있다. 

# References
* 스프링으로 시작하는 리액티브 프로그래밍 - 황정식 저
* 패스트캠퍼스 - Reactor