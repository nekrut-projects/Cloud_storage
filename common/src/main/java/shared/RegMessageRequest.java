package shared;

import lombok.Data;

@Data
public class RegMessageRequest extends AuthMessageRequest{
    private String name;
    private String email;

    public RegMessageRequest(String username, String password, String name, String email) {
        super(username, password);
        this.name = name;
        this.email = email;
    }

}
