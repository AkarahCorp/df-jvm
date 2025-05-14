package df;


@SuppressWarnings("unused")
public class Events {
    public static void player$join(Player p) {
        p.teleport(
                Location.zeroed()
                        .withX(100)
                        .withY(51)
                        .withZ(30)
        );
    }
}
