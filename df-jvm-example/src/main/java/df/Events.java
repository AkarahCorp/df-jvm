package df;

import dev.akarah.dfjvm.example.Main;

@SuppressWarnings("unused")
public class Events {
    public static void player$join(Player p) {
        for(int i = 0; i<10; i++) {
            p.sendMessage(Main.TEST_STRING);
        }
    }
}
