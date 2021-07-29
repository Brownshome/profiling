package brownshome.profiling;

import java.time.Duration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

abstract class LongProfiler implements Profiler {
	static class DefaultProfiler extends LongProfiler {
		private static volatile Supplier<ProfilingDataSink> DEFAULT_DATA_SINK_SUPPLIER = () -> ProfilingDataSink.NULL;

		private ProfilingDataSink profilingDataSink = DEFAULT_DATA_SINK_SUPPLIER.get();

		static void setDefaultDataSink(Supplier<ProfilingDataSink> sink) {
			DEFAULT_DATA_SINK_SUPPLIER = sink;
		}

		void setDataSink(ProfilingDataSink sink) {
			profilingDataSink = sink;
		}

		@Override
		void sectionEnded(long duration) {
			profilingDataSink.accept(this, Duration.ofNanos(duration));
		}

		@Override
		long time() {
			return System.nanoTime();
		}
	}

	static final ThreadLocal<DefaultProfiler> DEFAULT_PROFILER = ThreadLocal.withInitial(DefaultProfiler::new);

	private final class Section {
		final Marker marker;
		final long start;
		final int depth;
		final Section parent;

		Section(Marker marker) {
			this.parent = currentSection;
			this.marker = marker;
			this.start = time();
			this.depth = parent != null ? parent.depth + 1 : 0;
		}

		long duration() {
			return time() - start;
		}
	}

	private Section currentSection;

	abstract void sectionEnded(long duration);
	abstract long time();

	@Override
	public Profiler start(Marker marker) {
		currentSection = new Section(marker);

		return this;
	}

	@Override
	public void close() {
		sectionEnded(currentSection.duration());
		currentSection = currentSection.parent;
	}

	@Override
	public int numberOfSections() {
		return currentSection == null ? 0 : (currentSection.depth + 1);
	}

	@Override
	public Marker section(int level) {
		var s = currentSection;
		while (s.depth != level) {
			s = s.parent;
		}

		return s.marker;
	}

	@Override
	public Marker currentSection() {
		return currentSection.marker;
	}

	@Override
	public boolean hasOpenSections() {
		return currentSection != null;
	}

	@Override
	public Iterator<Marker> iterator() {
		return new Iterator<>() {
			Section next = currentSection;

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public Marker next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}

				var result = next.marker;
				next = next.parent;
				return result;
			}
		};
	}
}
