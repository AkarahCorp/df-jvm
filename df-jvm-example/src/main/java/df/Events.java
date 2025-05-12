package df;

public class Events {
    public static void playerJoin(Player p) {
        p.sendMessage("Hello there, %default!");
        p.sendMessage("This code is coming straight from a JVM!");

        p.setHealth(10);
    }
}
