package com.demo.lesson04.repository;

import com.demo.lesson04.entity.SelfRegisteredClient;
import com.demo.lesson04.mapper.Oauth2RegisteredClientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
public class SelfJdbcRegisteredClientRepository implements RegisteredClientRepository {

    @Autowired
    Oauth2RegisteredClientMapper mapper;

    @Override
    public void save(RegisteredClient registeredClient) {

        Assert.notNull(registeredClient, "registeredClient cannot be null");
        SelfRegisteredClient existingRegisteredClient = this.mapper.selectById(registeredClient.getId());
        if (existingRegisteredClient != null) {

            this.mapper.updateById(SelfRegisteredClient.covertSelfRegisteredClient(registeredClient));
        }
        else {
            this.mapper.insert(SelfRegisteredClient.covertSelfRegisteredClient(registeredClient));
        }

    }

    @Override
    public RegisteredClient findById(String id) {
        return SelfRegisteredClient.covertRegisteredClient(this.mapper.selectById(id));
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return SelfRegisteredClient.covertRegisteredClient(this.mapper.selectByClientId(clientId));
    }

    private void updateRegisteredClient(RegisteredClient registeredClient) {
        this.mapper.updateById(SelfRegisteredClient.covertSelfRegisteredClient(registeredClient));
    }

}
