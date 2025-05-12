package df;

public abstract class Player {
    String playerName;

    public abstract void sendMessage(String message);
    public abstract void sendActionBar(String actionBar);

    public abstract void damage(int health);
    public abstract void heal(int health);
    public abstract void setHealth(int health);

    public abstract void teleport(Location location);
}
