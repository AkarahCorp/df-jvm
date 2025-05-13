package df;

@SuppressWarnings("unused")
public class Events {
    public static void player$join(Player p) {
        p.sendMessage("You just joined! Oh wow!");
    }

    public static void player$command(Player p, String command) {
        p.sendMessage("<gray>You just sent:");
        p.sendMessage(command);
    }
}
