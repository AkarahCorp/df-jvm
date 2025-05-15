package df;

import dev.akarah.dfjvm.example.MyRecord;

@SuppressWarnings("unused")
public class Events {
    public static void player$join(Player p) {
        var record = new MyRecord("Endistic", 15);

        p.teleport(
                Location.zeroed()
                        .withX(100)
                        .withY(51)
                        .withZ(record.y())
        );
    }


}
