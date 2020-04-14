package spark;

public class Package {
    private long size;
    private String toIp;
    private String fromIp;

    public Package(String line) {
        String[] args = line.split(" ");
        this.size = Long.parseLong(args[0]);
        this.toIp = args[2];
        this.fromIp = args[1];
    }

    public long getSize() {
        return size;
    }

    public String getToIp() {
        return toIp;
    }

    public String getFromIp() {
        return fromIp;
    }
}
