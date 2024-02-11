package kr.pe.karsei.reactorprac;

import reactor.core.publisher.Flux;

public class Main {
    public static void main(String[] args) {
        System.out.printf("Hello and welcome!");
        Flux.just(1, 2, 3, 4, 5, 6)
                .map(n -> n * 2)
                ;
    }
}