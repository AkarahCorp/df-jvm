package df;


@SuppressWarnings("unused")
public class Events {
    public static void player$join(Player p) {
        for(int i = 0; i<500; i++) {
            p.teleport(new Location(
                    10,
                    100,
                    10
            ));
        }
    }
}
