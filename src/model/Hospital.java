package model;

public class Hospital extends Person {
    private String email;
    private String address;
    private String registrationNumber;

    public Hospital() {}

    public Hospital(int id, String name, String phone, String email, String address, String registrationNumber) {
        super(id, name, phone);
        this.email = email;
        this.address = address;
        this.registrationNumber = registrationNumber;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    @Override
    public String displayDetails() {
        return "Hospital[id=" + getId() + ", name=" + getName() + ", regNo=" + registrationNumber + "]";
    }
}
