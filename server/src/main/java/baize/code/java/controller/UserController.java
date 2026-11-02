package baize.code.java.controller;

import baize.code.java.common.Result;
import baize.code.java.entity.User;
import baize.code.java.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user){
        return userService.login(user);
    }
}
