package model;

public class Donation {
    private int id;
    private int donorId;
    private String donorName;
    private String bloodGroup;
    private int units;
    private String donationDate;

    public Donation() {}
    public Donation(int id, int donorId, String donorName, String bloodGroup, int units, String donationDate) {
        this.id = id;
        this.donorId = donorId;
        this.donorName = donorName;
        this.bloodGroup = bloodGroup;
        this.units = units;
        this.donationDate = donationDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getDonorId() { return donorId; }
    public void setDonorId(int donorId) { this.donorId = donorId; }
    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public int getUnits() { return units; }
    public void setUnits(int units) { this.units = units; }
    public String getDonationDate() { return donationDate; }
    public void setDonationDate(String donationDate) { this.donationDate = donationDate; }
}
