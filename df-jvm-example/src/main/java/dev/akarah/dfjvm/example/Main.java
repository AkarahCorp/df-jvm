package dev.akarah.dfjvm.example;

public class Main {
    public static int main() {
        var pair = new Pair(10, 20);
        return pair.x() + pair.y();
    }
}