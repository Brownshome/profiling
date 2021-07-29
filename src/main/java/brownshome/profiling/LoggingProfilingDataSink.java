package brownshome.profiling;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Objects;

/**
 * A sink for profiling data that logs a list of times taken every period. This implementation uses {@see System.Logger}
 */
public final class LoggingProfilingDataSink implements ProfilingDataSink {
	private static final System.Logger LOGGER = System.getLogger(LoggingProfilingDataSink.class.getModule().toString());

	private Instant lastLog = Instant.now();
	private final Duration period;

	private final System.Logger.Level level;

	private static final class MarkerStats {
		final Marker marker;

		Duration duration;
		int count;
		Duration max;
		Duration min;

		MarkerStats(Marker marker) {
			this.marker = marker;

			reset();
		}

		void add(Duration time) {
			duration = duration.plus(time);
			count++;
			if (max == null || time.compareTo(max) > 0) {
				max = time;
			}

			if (min == null || time.compareTo(min) < 0) {
				min = time;
			}
		}

		void reset() {
			duration = Duration.ZERO;
			count = 0;
			max = null;
			min = null;
		}
	}

	private final HashMap<Marker, MarkerStats> accumulatedDurations = new HashMap<>();

	/**
	 * Creates a data sink
	 * @param period the period to log at
	 * @param level the level to log at
	 */
	public LoggingProfilingDataSink(Duration period, System.Logger.Level level) {
		this.period = period;
		this.level = level;
	}

	@Override
	public void accept(Profiler profiler, Duration duration) {
		var marker = profiler.currentSection();

		accumulatedDurations.computeIfAbsent(marker, MarkerStats::new).add(duration);

		var now = Instant.now();
		if (lastLog.plus(period).isBefore(now)) {
			lastLog = now;

			log();
		}
	}

	private void log() {
		final int MAX_MARKER_LENGTH = 15;
		final String ELIPS = "...";

		var log = new StringBuilder("Profiling Table (%s)%n".formatted(formatDuration(period, "%#.2f%s")));
		log.append("\t%16s%10s%10s%10s%10s%10s%n".formatted("name", "total", "min", "average", "max", "count"));

		for (var v : accumulatedDurations.values()) {
			var name = Objects.toString(v.marker);
			if (name.length() > MAX_MARKER_LENGTH) {
				name = name.substring(0, MAX_MARKER_LENGTH - ELIPS.length()) + ELIPS;
			}

			log.append("\t%16s".formatted(name))
					.append(formatDuration(v.duration))
					.append(formatDuration(v.min))
					.append(formatDuration(v.count == 0 ? null : v.duration.dividedBy(v.count)))
					.append(formatDuration(v.max))
					.append("%10d".formatted(v.count))
					.append(System.lineSeparator());

			v.reset();
		}

		LOGGER.log(level, log);
	}

	private static final String[] UNITS = {
			"ns",
			"μs",
			"ms",
			"s ",
			"ks",
			"Ms",
			"Gs",
			"Ts"
	};

	private static String formatDuration(Duration duration) {
		return formatDuration(duration, "%#8.2f%s");
	}

	private static String formatDuration(Duration duration, String format) {
		if (duration == null) {
			return format.formatted(Double.NaN, "  ");
		}

		double count = duration.toNanos();

		for (var unit : UNITS) {
			if (count < 1000.0) {
				return format.formatted(count, unit);
			}

			count /= 1000.0;
		}

		throw new IllegalArgumentException();
	}
}
