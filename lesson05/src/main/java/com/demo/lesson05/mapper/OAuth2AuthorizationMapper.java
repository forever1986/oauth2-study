package com.demo.lesson05.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.lesson05.entity.SelfOAuth2Authorization;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OAuth2AuthorizationMapper extends BaseMapper<SelfOAuth2Authorization> {
}
