package baize.code.java.service;

import com.baomidou.mybatisplus.extension.service.IService;
import baize.code.java.common.Result;
import baize.code.java.entity.User;

public interface UserService extends IService<User> {
    Result<?> login(User user);
}