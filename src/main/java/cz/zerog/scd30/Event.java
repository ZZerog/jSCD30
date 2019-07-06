package cz.zerog.scd30;

public class Event {

    private float value;
    private Type type;

    public Event(Type type, float value) {
        this.type = type;
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    enum Type {
        CO2, HUMID, TEMP;
    }
}
