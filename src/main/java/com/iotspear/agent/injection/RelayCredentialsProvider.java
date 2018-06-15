package com.iotspear.agent.injection;

import com.iotspear.agent.data.RelayCredentials;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class RelayCredentialsProvider implements Provider<RelayCredentials> {

    private final String deviceAccessId;
    private final String deviceSecretKey;

    @Inject
    public RelayCredentialsProvider(@Named("deviceAccessId") String deviceAccessId,
                                    @Named("deviceSecretKey") String deviceSecretKey) {

        this.deviceAccessId = deviceAccessId;
        this.deviceSecretKey = deviceSecretKey;
    }

    @Override
    public RelayCredentials get() {

        return new RelayCredentials(deviceAccessId, deviceSecretKey);
    }
}
