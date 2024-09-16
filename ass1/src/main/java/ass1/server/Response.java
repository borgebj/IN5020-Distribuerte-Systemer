package ass1.server;

import java.io.Serializable;

public class Response implements Serializable {
	public int result;
	public int executionTime;
	public int waitingTime;

	public Response(int result, int execTime, int waitTime)
	{
		this.result = result;
		this.executionTime = execTime;
		this.waitingTime = waitTime;
	}

	@Override
	public String toString() {
		return String.format("{Res: %d; Exec: %d; Wait: %d}", result, executionTime, waitingTime);
	}
}
