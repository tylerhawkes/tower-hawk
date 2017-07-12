package org.towerhawk.monitor.check.type.system;

import com.sun.management.OperatingSystemMXBean;
import org.towerhawk.monitor.check.impl.AbstractCheck;
import org.towerhawk.monitor.check.run.CheckRun;
import org.towerhawk.monitor.check.threshold.SimpleNumericThreshold;
import org.towerhawk.serde.resolver.CheckType;

import java.lang.management.ManagementFactory;

@CheckType("swapSpace")
public class SwapSpace extends AbstractCheck{

	public SwapSpace() {
		cacheMs = 0;
		threshold = SimpleNumericThreshold.builder().warnUpper(0.6).critUpper(0.8).build();
	}

	@Override
	protected void doRun(CheckRun.Builder builder) throws InterruptedException {
		builder.succeeded();
		if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean) {
			OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
			long freeSwapSpace = os.getFreeSwapSpaceSize();
			long totalSwapSpace = os.getTotalSwapSpaceSize();
			if (totalSwapSpace > 0) {
				getThreshold().evaluate(builder, freeSwapSpace / totalSwapSpace);
				builder.addContext("freeSwapSpace", freeSwapSpace)
					.addContext("totalSwapSpace", totalSwapSpace);
			}
		} else {
			builder.message("Cannot get swap information from jvm");
		}
	}
}
