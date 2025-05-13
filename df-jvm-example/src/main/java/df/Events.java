package df;

public class Events {
    public static void playerJoin(Player p) {
        int[] arr = new int[]{10, 15, 19};
        p.damage(arr[2]);
        p.sendMessage("h");
    }

    public static void playerLeave(Player p) {
        p.sendMessage("<red>Goodbye!");
    }
}
