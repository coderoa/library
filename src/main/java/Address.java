public record Address(String street, String city, String state, String postalCode, String country) {
    @Override
    public String toString() {
        return street + ", " + city + ", " + state + " " + postalCode + ", " + country;
    }
}
