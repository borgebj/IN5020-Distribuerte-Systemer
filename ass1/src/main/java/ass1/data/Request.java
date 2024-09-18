package ass1.data;

import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

public class Request {
	private final String cacheKey;
	private final Supplier<Integer> computation;
	private final CountDownLatch latch;
	private int result;

	public Request(String cacheKey, Supplier<Integer> computation) {
		this.cacheKey = cacheKey;
		this.computation = computation;
		this.latch = new CountDownLatch(1);
	}

	public String getCacheKey() {
		return cacheKey;
	}
	public Supplier<Integer> getComputation() {
		return computation;
	}
	public int getResult() throws InterruptedException {
		latch.await();
		return result;
	}
	public void complete(int result) {
		this.result = result;
		latch.countDown();
	}

	@Override
	public String toString() {
		return String.format("request={%s ; %s ; %d}", cacheKey, computation.get(), result);
	}
}
