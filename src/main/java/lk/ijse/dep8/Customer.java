package lk.ijse.dep8;

import java.io.Serializable;

public class Customer implements Serializable {
    private String id;
    private byte[] image;
    private String name;
    private String address;

    public Customer() {
    }

    public Customer(String id, byte[] image, String name, String address) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

    public void printDetails(){
        System.out.printf("id=%s, name=%s, address=%s\n", id, name, address);
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
