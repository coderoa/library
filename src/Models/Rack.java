public record Rack(String rackNumber, String locationIdentifier) {
    @Override
    public String toString() {
        return rackNumber + " / " + locationIdentifier;
    }
}
