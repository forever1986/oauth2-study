package com.demo.lesson04.handler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@MappedJdbcTypes({JdbcType.VARCHAR})  //对应数据库类型
@MappedTypes({Set.class})            //java数据类型
public class SetStringTypeHandler implements TypeHandler<Set<String>> {

    private static final String COMMA =",";

    @Override
    public void setParameter(PreparedStatement ps, int i, Set<String> parameters, JdbcType jdbcType) throws SQLException {
        String str = "";
        if(parameters!=null){
            str = String.join(COMMA, parameters);
        }
        ps.setString(i, str);
    }

    @Override
    public Set<String> getResult(ResultSet rs, String columnName) throws SQLException {
        String str = rs.getString(columnName);
        return getSetFromString(str);
    }

    @Override
    public Set<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
        String str = rs.getString(columnIndex);
        return getSetFromString(str);
    }

    @Override
    public Set<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String str = cs.getString(columnIndex);
        return getSetFromString(str);
    }

    private Set<String> getSetFromString(String str){
        Set<String> set = new HashSet<>();
        if(str!=null&& !str.isEmpty()){
            String[] strs = str.split(COMMA);
            Collections.addAll(set, strs);
        }
        return set;
    }
}
