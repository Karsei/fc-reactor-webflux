package kr.pe.karsei.reactorprac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

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
                .contextWrite(ctx -> ctx.putAll(Context.of(key2, "Steve" , key3, "Jobs").readOnly()))
                .contextWrite(ctx -> ctx.put(key1, "Apple"))
                .subscribe(data -> log.info("# onNext: {}", data));

        Thread.sleep(100L);
    }

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

    @SneakyThrows
    @Test
    void contextDuplicateTest() {
        final String key1 = "company";

        Mono
                .just("Steve")
                .transformDeferredContextual((stringMono, ctx) -> ctx.get("role"))
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

    @SneakyThrows
    @Test
    void contextWithExampleApiTest() {
        Mono<String> mono = postBook(
                Mono.just(new Book("abcd-1111-3533-2809", "Reactor's Bible", "Kevin"))
        )
                .contextWrite(Context.of("authToken", "eyJhbGciOi"));
        mono.subscribe(data -> log.info("# onNext: {}", data));
    }
    
    private static Mono<String> postBook(Mono<Book> book) {
        return Mono
                .zip(book, Mono.deferContextual(ctx -> Mono.just(ctx.get("authToken"))))
                .flatMap(tuple -> {
                    // HTTP Post 전송을 했다고 가정함
                    String response = "POST the book(" + tuple.getT1().getBookName() + "," + tuple.getT1().getAuthor() + ") with token: " + tuple.getT2();
                    return Mono.just(response);
                });
    }
    
    @AllArgsConstructor
    @Data
    static class Book {
        private String isbn;
        private String bookName;
        private String author;
    }
}
