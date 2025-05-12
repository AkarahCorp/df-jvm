package df;

public class Events {
    public static void playerJoin(Player p) {
        p.sendMessage("Welcome!");
    }

    public static void playerLeave(Player p) {
        p.sendMessage("<red>Goodbye!");
    }
}
