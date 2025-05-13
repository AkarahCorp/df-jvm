package df;

@SuppressWarnings("unused")
public class Events {
    public static void player$join(Player p) {
        Player player2 = getAPlayer();
        if(player2 == null) {
            p.sendMessage("is null");
        } else {
            p.sendMessage("is not null");
        }
        p.sendMessage("You just joined! Oh wow!");

        if(i() == j()) {
            p.sendMessage("true");
        } else {
            p.sendMessage("false");
        }
    }

    public static void player$command(Player p, String command) {
        p.sendMessage("<gray>You just sent:");
        p.sendMessage(command);
    }

    public static Player getAPlayer() {
        return null;
    }

    public static int i() {
        return 10;
    }

    public static int j() {
        return 20;
    }
}
