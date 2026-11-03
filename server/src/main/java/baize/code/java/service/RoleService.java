package baize.code.java.service;

import com.baomidou.mybatisplus.extension.service.IService;
import baize.code.java.common.Result;
import baize.code.java.entity.Role;

public interface RoleService extends IService<Role> {
    Result<?> add(Role role);

    Result<?> delete(Integer id);

    Result<?> update(Role role);

    Result<?> detailsByCtId(Integer ctId);

    Role getRoleByCtId(Integer ctId);
}