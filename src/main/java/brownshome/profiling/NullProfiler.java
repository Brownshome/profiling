package brownshome.profiling;

import java.util.Iterator;
import java.util.List;

/**
 * A profiler always has one open section {@see MARKER}. When this section is closed it is always immediately
 * re-opened.
 */
public final class NullProfiler implements Profiler {
	private static final NullProfiler INSTANCE = new NullProfiler();
	private static final Marker MARKER = Marker.of("null");

	/**
	 * Gets the marker used by the null profiler
	 * @return the marker
	 */
	public static Marker marker() {
		return MARKER;
	}

	/**
	 * Gets an instance of the null profiler
	 * @return the instance
	 */
	public static Profiler instance() {
		return INSTANCE;
	}

	private NullProfiler() { }

	@Override
	public Profiler start(Marker marker) {
		return this;
	}

	@Override
	public Profiler start(String name) {
		return this;
	}

	@Override
	public void close() { }

	@Override
	public void closeAll() { }

	@Override
	public int numberOfSections() {
		return 1;
	}

	@Override
	public Marker section(int level) {
		assert level == 0;

		return MARKER;
	}

	@Override
	public Marker currentSection() {
		return MARKER;
	}

	@Override
	public boolean hasOpenSections() {
		return true;
	}

	@Override
	public Iterator<Marker> iterator() {
		return List.of(MARKER).iterator();
	}

	@Override
	public void closeUntil(Marker marker) {
		assert marker.equals(MARKER);
	}

	@Override
	public void close(Marker marker) {
		assert marker.equals(MARKER);
	}
}
