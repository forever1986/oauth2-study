package com.demo.lesson05.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MappedJdbcTypes({JdbcType.VARCHAR})  //对应数据库类型
@MappedTypes({ClientSettings.class})            //java数据类型
public class ClientSettingsTypeHandler implements TypeHandler<ClientSettings> {


    private ObjectMapper objectMapper;

    public ClientSettingsTypeHandler() {
        objectMapper = new ObjectMapper();
        /**
         * 此处注册json存储格式化
         */
        ClassLoader classLoader = ClientSettingsTypeHandler.class.getClassLoader();
        List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        this.objectMapper.registerModules(securityModules);
        this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
    }

    @Override
    public void setParameter(PreparedStatement ps, int i, ClientSettings parameter, JdbcType jdbcType) throws SQLException {
        if(parameter!=null&&parameter.getSettings()!=null){
            ps.setString(i ,writeMap(parameter.getSettings()));
        }else{
            ps.setString(i, "");
        }

    }

    @Override
    public ClientSettings getResult(ResultSet rs, String columnName) throws SQLException {
        String str = rs.getString(columnName);
        return ClientSettings.withSettings(parseMap(str)).build();
    }

    @Override
    public ClientSettings getResult(ResultSet rs, int columnIndex) throws SQLException {
        String str = rs.getString(columnIndex);
        return ClientSettings.withSettings(parseMap(str)).build();
    }

    @Override
    public ClientSettings getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String str = cs.getString(columnIndex);
        return ClientSettings.withSettings(parseMap(str)).build();
    }

    private String writeMap(Map<String, Object> data) {
        try {
            return this.objectMapper.writeValueAsString(data);
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    private Map<String, Object> parseMap(String data) {
        if(data!=null&&!data.isEmpty()){
            try {
                return this.objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
                });
            }
            catch (Exception ex) {
                throw new IllegalArgumentException(ex.getMessage(), ex);
            }
        }else{
            return new HashMap<>();
        }
    }
}
