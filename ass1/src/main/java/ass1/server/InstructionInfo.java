package ass1.server;

public class InstructionInfo {
	String function;
	int arg1, arg2, arg3;
	int zone;

	public InstructionInfo(String function, int arg1, int arg2, int arg3, int zone) {
		this.function = function;
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.arg3 = arg3;
		this.zone = zone;
	}

	@Override
	public String toString() {
		return String.format("%s(%d, %d, %d) - %d", function, arg1, arg2, arg3, zone);
	}
}
