package ai.z.openapi.service.agents;

import ai.z.openapi.ZaiClient;
import ai.z.openapi.api.agents.AgentsApi;
import ai.z.openapi.service.model.ChatCompletionResponse;
import ai.z.openapi.service.model.ModelData;
import ai.z.openapi.utils.FlowableRequestSupplier;
import ai.z.openapi.utils.RequestSupplier;
import io.reactivex.Single;
import okhttp3.ResponseBody;

/**
 * Agent completion service implementation
 */
public class AgentServiceImpl implements AgentService {

	private final ZaiClient zAiClient;

	private final AgentsApi agentsApi;

	public AgentServiceImpl(ZaiClient zAiClient) {
		this.zAiClient = zAiClient;
		this.agentsApi = zAiClient.retrofit().create(AgentsApi.class);
	}

	@Override
	public ChatCompletionResponse createAgentCompletion(AgentsCompletionRequest request) {
		validateParams(request);
		if (request.getStream()) {
			return streamAgentCompletion(request);
		}
		else {
			return syncAgentCompletion(request);
		}
	}

	@Override
	public Single<ModelData> retrieveAgentAsyncResult(AgentAsyncResultRetrieveParams request) {
		return agentsApi.queryAgentsAsyncResult(request);
	}

	private ChatCompletionResponse streamAgentCompletion(AgentsCompletionRequest request) {
		FlowableRequestSupplier<AgentsCompletionRequest, retrofit2.Call<ResponseBody>> supplier = agentsApi::agentsCompletionStream;
		;
		return this.zAiClient.streamRequest(request, supplier, ChatCompletionResponse.class, ModelData.class);
	}

	private ChatCompletionResponse syncAgentCompletion(AgentsCompletionRequest request) {
		RequestSupplier<AgentsCompletionRequest, ModelData> supplier = agentsApi::agentsCompletionSync;
		return this.zAiClient.executeRequest(request, supplier, ChatCompletionResponse.class);
	}

	private void validateParams(AgentsCompletionRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("request cannot be null");
		}
		if (request.getMessages() == null || request.getMessages().isEmpty()) {
			throw new IllegalArgumentException("request messages cannot be null or empty");
		}
		if (request.getAgentId() == null) {
			throw new IllegalArgumentException("request agent_id cannot be null");
		}
	}

}
