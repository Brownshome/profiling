package brownshome.profiling;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An implementation of a profiler that tracks a stack of marked sections. Implementors will need to provide a clock
 * implementation and a way of handling closed frames.
 */
public abstract class StackProfiler implements Profiler {
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

	/**
	 * Handles when a section closes. Note, this is called on the same thread that just
	 * closed the section.
	 *
	 * @param duration the duration of that section.
	 */
	protected abstract void sectionEnded(long duration);

	/**
	 * Gets the current time. The difference between timestamps is used as the duration value in {@see sectionEnded}, but
	 * the exact meaning of the values is left up to the implementor.
	 *
	 * @return the current time
	 */
	protected abstract long time();

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
