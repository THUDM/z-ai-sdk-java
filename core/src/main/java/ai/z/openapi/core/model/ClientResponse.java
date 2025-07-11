package ai.z.openapi.core.model;

import ai.z.openapi.service.model.ChatError;

public interface ClientResponse<T> {

	public T getData();

	public void setData(T data);

	void setCode(int code);

	void setMsg(String msg);

	void setSuccess(boolean b);

	void setError(ChatError chatError);

}
