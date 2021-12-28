package shared;

import lombok.Data;

@Data
public class AuthMessageResponse extends AbstractCommand {
    private boolean status;

    public AuthMessageResponse(boolean status) {
        this.status = status;
    }
}
