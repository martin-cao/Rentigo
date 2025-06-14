package cc.martincao.rentigo.rentigobackend.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException() {
        super("用户名已被注册");
    }
}
