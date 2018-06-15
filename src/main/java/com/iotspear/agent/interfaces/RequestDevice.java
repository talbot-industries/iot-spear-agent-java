package com.iotspear.agent.interfaces;

import com.iotspear.agent.data.AgentResponse;
import com.iotspear.agent.data.ProxyRequest;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public interface RequestDevice {

    CompletionStage<AgentResponse> processRequest(ProxyRequest request, Supplier<Optional<String>> accountHost);
}
