package org.towerhawk.monitor.check.threshold;

import org.junit.Before;
import org.junit.Test;
import org.towerhawk.monitor.check.TestCheck;
import org.towerhawk.monitor.check.run.CheckRun;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.towerhawk.monitor.check.run.CheckRun.Status.CRITICAL;
import static org.towerhawk.monitor.check.run.CheckRun.Status.SUCCEEDED;
import static org.towerhawk.monitor.check.run.CheckRun.Status.WARNING;

public class NagiosSyntaxThresholdTest {

	private CheckRun.Builder builder;

	@Before
	public void setup() {
		builder = CheckRun.builder(new TestCheck()).succeeded();
	}

	@Test
	public void evaluateDouble() throws Exception {
		//warn if < 10 critical if > 10
		Map<Integer, CheckRun.Status> expected = new LinkedHashMap<>();
		Threshold threshold = new NagiosSyntaxThreshold("10", "20");
		expected.put(-1, CRITICAL);
		expected.put(5, SUCCEEDED);
		expected.put(15, WARNING);
		expected.put(25, CRITICAL);
		evaluateMultiple(threshold, expected);
		threshold = new NagiosSyntaxThreshold("30:", "25:");
		expected.put(35, SUCCEEDED);
		expected.put(28, WARNING);
		expected.put(23, CRITICAL);
		evaluateMultiple(threshold, expected);
		threshold = new NagiosSyntaxThreshold("~:10", "~:20");
		expected.put(9, SUCCEEDED);
		expected.put(12, WARNING);
		expected.put(22, CRITICAL);
		evaluateMultiple(threshold, expected);
		threshold = new NagiosSyntaxThreshold("10:20", "0:30");
		expected.put(-3, CRITICAL);
		expected.put(3, WARNING);
		expected.put(13, SUCCEEDED);
		expected.put(23, WARNING);
		expected.put(33, CRITICAL);
		evaluateMultiple(threshold, expected);
		threshold = new NagiosSyntaxThreshold("@0:30", "@10:20");
		expected.put(-4, SUCCEEDED);
		expected.put(4, WARNING);
		expected.put(14, CRITICAL);
		expected.put(24, WARNING);
		expected.put(34, SUCCEEDED);
		evaluateMultiple(threshold, expected);
	}

	private void evaluateMultiple(Threshold threshold, Map<Integer, CheckRun.Status> expected) {
		expected.forEach((k, v) -> {
			builder.forceSucceeded();
			threshold.evaluate(builder, k.doubleValue());
			assertTrue(String.format("Expected value of %s but got %s for value %d", v, builder.getStatus(), k), v == builder.getStatus());
		});
		expected.clear();
	}

	@Test
	public void evaluateString() throws Exception {
		Threshold threshold = new NagiosSyntaxThreshold("10", "20");
		threshold.evaluate(builder, "5");
		assertTrue("Expected that a string as a number succeeds on evaluation", builder.getStatus() == SUCCEEDED);
		threshold.evaluate(builder, "someString");
		assertTrue("Expected an error but got none", builder.build().getError() != null);
	}

	@Test
	public void evaluateObject() throws Exception {
		Threshold threshold = new NagiosSyntaxThreshold("10", "20");
		threshold.evaluate(builder, new Long(5));
		assertTrue("Expected that a Number type succeeds on evaluation", builder.getStatus() == SUCCEEDED);
		threshold.evaluate(builder, new Object());
		assertTrue(builder.build().getError() != null);
	}

}