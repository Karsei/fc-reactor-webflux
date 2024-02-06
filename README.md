# Reactor 연습

## Reactive Stream

비동기 데이터 스트림 처리를 위한 일종의 표준화된 스펙

이를 구현한 것 -> Project Reactor -> Spring Webflux 에서 이를 활용

### 구성 요소

#### stream

* publisher
* subscriber
* subscription
* processor
  * publisher, subscriber 를 둘 다 상속받아서 데이터를 가공하거나 가공한 데이터를 다시 전달하는 역할

![publisher_subscriber_detail.png](images%2Fpublisher_subscriber_detail.png)

![publisher_subscriber.png](images%2Fpublisher_subscriber.png)

```java
public interface Subscription {
    public void request(long n); // 데이터를 몇 개까지 가질 수 있다라고 publisher 에게 알려줌
    public void cancel(); // 구독관계 취소
}
```

```java
// Processor 는 Subscriber 와 Publisher 둘 다 상속받음
public interface Processor<T, R> extends Subcriber<T>, Publisher<R> {}
```

#### asynchronous

비동기 처리 가능

데이터 처리 단계에서 같은 스레드나 별개의 스레드를 동기화 시킬 수 있으며 병렬 처리도 가능

![asyncronous.png](images%2Fasyncronous.png)

#### back pressure

publisher 가 생성한 것을 subscriber 가 충분히 처리하지 못할 때 불균형 처리 가능

subscriber 가 역으로 데이터 제어

해당 스레드가 블록킹을 하게 되면 다른 것을 못함 -> 성능에 영향을 미침

request method 를 논블록킹 방식으로 동작 ->이를 통틀어 논블록킹 백프레셔라고 불리게 됨 