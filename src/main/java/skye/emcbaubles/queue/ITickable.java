package skye.emcbaubles.queue;

public interface ITickable {
    int getTicks();
    void setTicks(int ticks);

    default int getTicksThenDecrement() {
        int current = getTicks();
        setTicks(current - 1);
        return current + 1;
    }
}
