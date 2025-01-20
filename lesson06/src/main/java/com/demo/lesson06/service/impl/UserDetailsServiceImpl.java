package com.demo.lesson06.service.impl;

import com.demo.lesson06.entity.LoginUserDetails;
import com.demo.lesson06.entity.TUser;
import com.demo.lesson06.mapper.TUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private TUserMapper tUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询自己数据库的用户信息
        TUser user = tUserMapper.selectByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException(username);
        }
        return new LoginUserDetails(user);
    }

}
