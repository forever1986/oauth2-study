package com.demo.lesson06.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.lesson06.entity.SelfOAuth2AuthorizationConsent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OAuth2AuthorizationConsentMapper extends BaseMapper<SelfOAuth2AuthorizationConsent> {
}
