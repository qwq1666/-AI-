package baize.code.java.service.impl;

import baize.code.java.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import baize.code.java.code.ResultCode;
import baize.code.java.common.Result;
import baize.code.java.entity.User;
import baize.code.java.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public Result<?> login(User user) {
        User account = lambdaQuery().eq(User::getAccount, user.getAccount()).one();
        if (account == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        if (!account.getPassword().equals(user.getPassword())) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        account.clearPassword();
        return Result.success(ResultCode.LOGIN_SUCCESS, account);
    }

}