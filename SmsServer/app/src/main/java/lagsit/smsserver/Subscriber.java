package lagsit.smsserver;

/**
 * Created by Richard on 11/22/2016.
 */
public class Subscriber {
    String code,number,subscribeTo;

    public Subscriber(String code,String number, String subscribeTo) {
        this.code = code;
        this.number = number;
        this.subscribeTo = subscribeTo;
    }
    public Subscriber(String number, String subscribeTo) {
        this.number = number;
        this.subscribeTo = subscribeTo;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSubscribeTo() {
        return subscribeTo;
    }

    public void setSubscribeTo(String subscribeTo) {
        this.subscribeTo = subscribeTo;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Subscriber)
        {
            Subscriber temp = (Subscriber) obj;
            if(this.number.equals(temp.number) && this.subscribeTo.equals(temp.subscribeTo))
                return true;
        }
        return false;

    }

    @Override
    public int hashCode() {
        return (this.number.hashCode() +' '+ this.subscribeTo.hashCode());

    }
}
