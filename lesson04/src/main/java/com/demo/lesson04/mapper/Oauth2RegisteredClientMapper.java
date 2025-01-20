package com.demo.lesson04.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.lesson04.entity.SelfRegisteredClient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface Oauth2RegisteredClientMapper extends BaseMapper<SelfRegisteredClient> {

    // 根据client_id，查询客户端信息
    @Select("select * from oauth2_registered_client where client_id = #{client_id}")
    SelfRegisteredClient selectByClientId(String client_id);
}
