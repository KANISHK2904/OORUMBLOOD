package model;

public class Donor extends Person {
    private String email;
    private int age;
    private String bloodGroup;
    private String address;
    private String lastDonationDate;

    public Donor() {}

    public Donor(int id, String name, String phone, String email, int age, String bloodGroup, String address, String lastDonationDate) {
        super(id, name, phone);
        this.email = email;
        this.age = age;
        this.bloodGroup = bloodGroup;
        this.address = address;
        this.lastDonationDate = lastDonationDate;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(String lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    @Override
    public String displayDetails() {
        return "Donor[id=" + getId() + ", name=" + getName() + ", bloodGroup=" + bloodGroup + ", age=" + age + "]";
    }
}
