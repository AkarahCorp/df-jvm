package df;

@SuppressWarnings("unused")
public class Events {
    public static void player$join(Player p) {
        var test = getNull();
        if(test == null) {
           p.sendMessage("YEP, ITS NULL");
        } else {
            p.sendMessage("nop not null ?!");
        }
        p.sendMessage(test.toString());
        p.sendMessage("Welcome!");
    }

    public static Object getNull() {
        return null;
    }

    public static void player$command(Player p, String command) {
        p.sendMessage("<gray>You just sent:");
        p.sendMessage(command);
    }
}
