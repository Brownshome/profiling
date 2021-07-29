package brownshome.profiling;

import java.util.Iterator;
import java.util.function.Supplier;

class ProfilerThreadLocal extends ThreadLocal<Profiler> {
	private static final ProfilerThreadLocal INSTANCE = new ProfilerThreadLocal();
	private Supplier<? extends Profiler> supplier = NullProfiler::instance;

	static void setProfiler(Supplier<? extends Profiler> profiler) {
		INSTANCE.supplier = profiler;
	}

	static ProfilerThreadLocal instance() {
		return INSTANCE;
	}

	@Override
	protected Profiler initialValue() {
		var result = supplier.get();

		assert result != null;

		return result;
	}
}

/**
 * A store of profiling data. This class tracks entrances and exits from method calls
 */
public interface Profiler extends Iterable<Marker>, AutoCloseable {
	/**
	 * Sets the default profiler for all threads that have not yet called {@see thread}
	 * @param profiler a supplier to use for new profilers. This must not be null, or return null.
	 */
	static void setDefaultProfiler(Supplier<? extends Profiler> profiler) {
		assert profiler != null;

		ProfilerThreadLocal.setProfiler(profiler);
	}

	/**
	 * Sets the profiler for this thread, all data in the old profiler are lost
	 * @param profiler the new profiler, this must not be null
	 */
	static void setProfiler(Profiler profiler) {
		assert profiler != null;

		ProfilerThreadLocal.instance().set(profiler);
	}

	/**
	 * A profiler for this thread. This will be unique to the calling thread
	 * @return the profiler specific to this thread
	 */
	static Profiler thread() {
		return ProfilerThreadLocal.instance().get();
	}

	/**
	 * Starts a profiler section with a given marker.
	 * @param marker the marker to use, may be null
	 * @return this
	 */
	Profiler start(Marker marker);

	/**
	 * Starts a profiler section with the given name. The marker for this section will be {@see Marker#of}
	 * @param name the name of the new section
	 * @return this
	 */
	default Profiler start(String name) {
		return start(Marker.of(name));
	}

	/**
	 * Closes the provided section.
	 * @param marker the marker for the section to close
	 */
	default void close(Marker marker) {
		assert currentSection().equals(marker);

		close();
	}

	/**
	 * Closes the current section. This must only be used if there is a currently open section.
	 */
	@Override
	void close();

	/**
	 * Closes all open sections. If no sections are open this method does nothing
	 */
	default void closeAll() {
		while (hasOpenSections()) {
			close();
		}
	}

	/**
	 * Closes all sections from the top until the provided marker is reached. This must only be called if the provided
	 * marker is a currently open section. The section provided will not be closed.
	 * @param marker the marker to close until
	 */
	default void closeUntil(Marker marker) {
		while (currentSection() != marker) {
			close();
		}
	}

	/**
	 * The number of sections current open
	 * @return the number of currently open sections
	 */
	int numberOfSections();

	/**
	 * Gets the marker for the specific section
	 * @param level the number of section up the stack where 0 is the root section
	 * @return the marker
	 */
	Marker section(int level);

	/**
	 * Gets the marker for the current section. This must not be called if there is no open section
	 * @return the marker for the current (topmost) section
	 */
	Marker currentSection();

	/**
	 * Checks if this profiler has open sections
	 * @return if the profiler has open sections
	 */
	boolean hasOpenSections();

	/**
	 * Creates an iterator for the sections of the profiler from the top section to the deepest section. Sections must
	 * not be closed before they are iterated over or undefined behaviour occurs.
	 *
	 * @return an iterator for all open sections
	 */
	@Override
	Iterator<Marker> iterator();
}
