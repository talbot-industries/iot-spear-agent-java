package com.iotspear.agent.interfaces;

import com.iotspear.agent.data.AgentResponse;
import com.iotspear.agent.data.BearerInformation;
import com.iotspear.agent.data.ProxyRequests;
import com.iotspear.agent.data.RelayCredentials;

import java.util.concurrent.CompletionStage;

public interface RequestsRelay {

    CompletionStage<BearerInformation> login(RelayCredentials credentials);

    CompletionStage<ProxyRequests> getRequests(String bearerToken);

    CompletionStage<Void> putResponse(String bearerToken, String requestId, String clientId, String correlation, AgentResponse response);
}
