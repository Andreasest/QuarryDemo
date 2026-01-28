package QuarryDemo.model;

public class Firma {
    private final String name;
    private final String phone;
    private final String email;
    private final String spokesperson;
    private final String address;
    private final int makseTingimus;

    public Firma(String name, String spokesperson, String email, String phone, String address, int makseTingimus) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.spokesperson = spokesperson;
        this.address=address;
        this.makseTingimus = makseTingimus;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public int getMakseTingimus() {
        return makseTingimus;
    }

    public String getAddress() {
        return address;
    }

    public String getSpokesperson() {
        return spokesperson;
    }
}
