package priv.theo.resilienceexample;

import io.vavr.control.Try;

public class Test {

    public void test() {
        Try.of(() -> {
            return "hello";
        });
    }

    public static void main(String[] args) {
        System.out.println("test");
        new Test().test();
    }
}
