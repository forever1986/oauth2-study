package com.demo.lesson06.handler;

import com.demo.lesson06.entity.TUser;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
        //让TUser支持Jackson序列化
        this.objectMapper.registerModules(new TUserSimpleModule());
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

    /**
     * 让TUser支持Jackson序列化
     */
    class TUserSimpleModule extends SimpleModule {
        public TUserSimpleModule() {
            super(TUserSimpleModule.class.getName(), new Version(1, 0, 0, null, null, null));
        }

        @Override
        public void setupModule(SetupContext context) {
            context.setMixInAnnotations(TUser.class, TUserMixin.class);
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    abstract class TUserMixin {

        @JsonCreator
        TUserMixin(TUser tUser) {
        }

    }
}
