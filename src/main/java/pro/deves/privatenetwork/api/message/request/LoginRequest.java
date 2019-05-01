package pro.deves.privatenetwork.api.message.request;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;

    private String password;
}
