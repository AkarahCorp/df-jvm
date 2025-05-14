package df;

public abstract class Location {
    public abstract double x();
    public abstract double y();
    public abstract double z();

    public abstract Location withX(double x);
    public abstract Location withY(double y);
    public abstract Location withZ(double z);

    public static Location of(double x, double y, double z) {
        return null;
    }

    public static Location zeroed() {
        return null;
    }
}
