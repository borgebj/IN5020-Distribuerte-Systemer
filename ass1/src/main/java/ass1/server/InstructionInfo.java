package ass1.server;

import java.util.List;

public class InstructionInfo {
	public String function;
	public List<String> args;
	public int zone;

	public InstructionInfo(String function, List<String> args, int zone) {
		this.function = function;
		this.args = args;
		this.zone = zone;
	}


	@Override
	public String toString() {
		String argsString = String.join(" ", args);
		return String.format("%s %s Zone:%d", function, argsString, zone);
	}
}
