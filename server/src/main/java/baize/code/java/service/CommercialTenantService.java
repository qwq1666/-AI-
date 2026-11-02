package baize.code.java.service;

import com.baomidou.mybatisplus.extension.service.IService;
import baize.code.java.common.Result;
import baize.code.java.entity.CommercialTenant;

public interface CommercialTenantService extends IService<CommercialTenant> {
    Result<?> login(CommercialTenant commercialTenant);
}