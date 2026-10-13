package model;

public class BloodStock {
    private int id;
    private String bloodGroup;
    private int quantity;

    public BloodStock() {}
    public BloodStock(int id, String bloodGroup, int quantity) {
        this.id = id;
        this.bloodGroup = bloodGroup;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
