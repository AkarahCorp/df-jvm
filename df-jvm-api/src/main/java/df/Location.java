package df;

public class Location {
    double x;
    double y;
    double z;

    float pitch;
    float yaw;

    public Location() {
        this.x = 0;
        this.y = 0;
        this.z = 0;

        this.pitch = 0;
        this.yaw = 0;
    }

    public Location(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.pitch = 0;
        this.yaw = 0;
    }

    public Location(double x, double y, double z, float pitch, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;

        this.pitch = pitch;
        this.yaw = yaw;
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    public double z() {
        return this.z;
    }

    public float pitch() {
        return this.pitch;
    }

    public float yaw() {
        return this.yaw;
    }

    public Location withX(double x) {
        return new Location(x, this.y, this.z);

    }

    public Location withY(double y) {
        return new Location(this.x, y, this.z);
    }

    public Location withZ(double z) {
        return new Location(this.x, this.y, z);
    }

    public Location shiftByVector(Vector vector) {
        return new Location(this.x + vector.x, this.y + vector.y, this.z + vector.z);
    }
}
