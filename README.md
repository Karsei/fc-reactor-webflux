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

subscriber 가 역으로 데이터 제어

해당 스레드가 블록킹을 하게 되면 다른 것을 못함 -> 성능에 영향을 미침

request method 를 논블록킹 방식으로 동작 -> 이를 통틀어 논블록킹 백프레셔라고 불리게 됨

# References
* 스프링으로 시작하는 리액티브 프로그래밍 - 황정식 저
* 패스트캠퍼스 - Reactor