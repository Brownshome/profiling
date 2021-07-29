package brownshome.profiling;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * A sink for profiling data
 */
public interface ProfilingDataSink {
	/**
	 * A data sink that does nothing. This is the default sink
	 */
	ProfilingDataSink NULL = (profiler, duration) -> {};

	/**
	 * Sets the data sink for all threads that have not yet started profiling
	 * @param sink the sink to set
	 */
	static void setDefaultDataSink(Supplier<ProfilingDataSink> sink) {
		LongProfiler.DefaultProfiler.setDefaultDataSink(sink);
	}

	/**
	 * Sets the data sink just for this thread
	 * @param sink the sink to set
	 */
	static void setThreadDataSink(ProfilingDataSink sink) {
		LongProfiler.DEFAULT_PROFILER.get().setDataSink(sink);
	}

	/**
	 * Accepts a profiling end section event
	 * @param profiler the profiler that ended
	 * @param duration the duration of the section just ended
	 */
	void accept(Profiler profiler, Duration duration);
}
