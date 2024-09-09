package ass1.server;

import java.util.List;

public class InstructionInfo {
	String function;
	List<String> args;
	int zone;

	public InstructionInfo(String function, List<String> args, int zone) {
		this.function = function;
		this.args = args;
		this.zone = zone;
	}

	@Override
	public String toString() {
		return String.format("%s(%s) - %d", function, args.toString(), zone);
	}
}
