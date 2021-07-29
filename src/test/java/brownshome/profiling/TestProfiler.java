package brownshome.profiling;

import java.util.*;

final class TestProfiler implements Profiler {
	interface Event { }

	record StartEvent(Marker marker) implements Event { }
	record StopEvent(Marker marker) implements Event { }

	private final List<Marker> sections = new ArrayList<>();
	private final List<Event> events = new ArrayList<>();

	@Override
	public Profiler start(Marker marker) {
		sections.add(marker);
		events.add(new StartEvent(marker));

		return this;
	}

	@Override
	public void close() {
		events.add(new StopEvent(currentSection()));
		sections.remove(sections.size() - 1);
	}

	@Override
	public int numberOfSections() {
		return sections.size();
	}

	@Override
	public Marker section(int level) {
		return sections.get(level);
	}

	@Override
	public Marker currentSection() {
		return sections.get(sections.size() - 1);
	}

	@Override
	public boolean hasOpenSections() {
		return !sections.isEmpty();
	}

	@Override
	public Iterator<Marker> iterator() {
		return new Iterator<>() {
			final ListIterator<Marker> it = sections.listIterator(sections.size());

			@Override
			public boolean hasNext() {
				return it.hasPrevious();
			}

			@Override
			public Marker next() {
				return it.previous();
			}
		};
	}

	List<Event> events() {
		return events;
	}
}
