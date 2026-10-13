package model;

public class Patient extends Person {
    private int age;
    private String bloodGroup;
    private String address;
    private String disease;
    // Hospital association (nullable — existing patients have no hospital)
    private int hospitalId;          // 0 means null / not set
    private String hospitalName;     // from JOIN, read-only
    private String hospitalCode;     // from JOIN, read-only (registration_number)

    public Patient() {}

    /** Full constructor used by DAO when reading from DB */
    public Patient(int id, String name, String phone, int age, String bloodGroup,
                   String address, String disease, int hospitalId,
                   String hospitalName, String hospitalCode) {
        super(id, name, phone);
        this.age          = age;
        this.bloodGroup   = bloodGroup;
        this.address      = address;
        this.disease      = disease;
        this.hospitalId   = hospitalId;
        this.hospitalName = hospitalName;
        this.hospitalCode = hospitalCode;
    }

    public int    getAge()           { return age; }
    public void   setAge(int age)    { this.age = age; }

    public String getBloodGroup()              { return bloodGroup; }
    public void   setBloodGroup(String bg)     { this.bloodGroup = bg; }

    public String getAddress()                 { return address; }
    public void   setAddress(String address)   { this.address = address; }

    public String getDisease()                 { return disease; }
    public void   setDisease(String disease)   { this.disease = disease; }

    public int    getHospitalId()              { return hospitalId; }
    public void   setHospitalId(int id)        { this.hospitalId = id; }

    public String getHospitalName()            { return hospitalName; }
    public void   setHospitalName(String n)    { this.hospitalName = n; }

    public String getHospitalCode()            { return hospitalCode; }
    public void   setHospitalCode(String c)    { this.hospitalCode = c; }

    @Override
    public String displayDetails() {
        return "Patient[id=" + getId() + ", name=" + getName()
             + ", bloodGroup=" + bloodGroup + ", disease=" + disease
             + ", hospital=" + hospitalName + "]";
    }
}
