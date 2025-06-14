package cc.martincao.rentigo.rentigobackend.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("邮箱已被注册");
    }
}