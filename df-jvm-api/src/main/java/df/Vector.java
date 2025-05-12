package df;

public class Vector {
    double x;
    double y;
    double z;

    public Vector() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public Vector withX(double x) {
        this.x = x;
        return this;
    }

    public Vector withY(double y) {
        this.y = y;
        return this;
    }

    public Vector withZ(double z) {
        this.z = z;
        return this;
    }
}
