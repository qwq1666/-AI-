package baize.code.java.service.impl;

import baize.code.java.service.CommercialTenantService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import baize.code.java.code.ResultCode;
import baize.code.java.common.Result;
import baize.code.java.entity.CommercialTenant;
import baize.code.java.mapper.CommercialTenantMapper;
import org.springframework.stereotype.Service;

@Service
public class CommercialTenantServiceImpl extends ServiceImpl<CommercialTenantMapper, CommercialTenant> implements CommercialTenantService {
    @Override
    public Result<?> login(CommercialTenant commercialTenant) {
        CommercialTenant account = lambdaQuery().eq(CommercialTenant::getAccount, commercialTenant.getAccount()).one();
        if (account == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        if (!account.getPassword().equals(commercialTenant.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }
        account.clearPassword();
        return Result.success(ResultCode.LOGIN_SUCCESS, account);
    }
}