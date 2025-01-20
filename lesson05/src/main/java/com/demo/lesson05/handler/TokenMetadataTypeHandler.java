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

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MappedJdbcTypes({JdbcType.BLOB})  //对应数据库类型
@MappedTypes({Map.class})
public class TokenMetadataTypeHandler implements TypeHandler<Map<String, Object>> {


    private ObjectMapper objectMapper;

    public TokenMetadataTypeHandler() {
        objectMapper = new ObjectMapper();
        /**
         * 此处注册json存储格式化
         */
        ClassLoader classLoader = TokenMetadataTypeHandler.class.getClassLoader();
        List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        this.objectMapper.registerModules(securityModules);
        this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());

    }

    @Override
    public void setParameter(PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType) throws SQLException {
        if(parameter!=null){
            ps.setString(i ,writeMap(parameter));
        }else{
            ps.setString(i, "");
        }
    }

    @Override
    public Map<String, Object> getResult(ResultSet rs, String columnName) throws SQLException {
        String str = rs.getString(columnName);
        return parseMap(str);
    }

    @Override
    public Map<String, Object> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String str = rs.getString(columnIndex);
        return parseMap(str);
    }

    @Override
    public Map<String, Object> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String str = cs.getString(columnIndex);
        return parseMap(str);
    }


    private String writeMap(Map<String, Object> data) {
        try {
            this.objectMapper.findAndRegisterModules();
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
