package lagsit.smsserver;

/**
 * Created by Richard on 11/23/2016.
 */
public class Subscription {
    String code,name, email;

    public Subscription(String code, String email) {
        this.code = code;
        this.email = email;
    }
    public Subscription(String code, String name,String email) {
        this.code = code;
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
