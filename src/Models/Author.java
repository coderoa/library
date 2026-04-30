public record Author(String id, String name) {
    @Override
    public String toString() {
        return name;
    }
}
