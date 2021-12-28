package shared;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthMessageRequest extends AbstractCommand {
    private String username;
    private String password;

    public AuthMessageRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
