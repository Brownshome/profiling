package brownshome.profiling;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StackProfilerTest {
	private StackProfiler stackProfiler;

	private record FinishedSection(Marker marker, long duration) { }
	private List<FinishedSection> endedMarkers;

	private int time;

	@BeforeEach
	void setup() {
		time = 0;
		endedMarkers = new ArrayList<>();
		stackProfiler = new StackProfiler() {
			@Override
			protected void sectionEnded(long duration) {
				endedMarkers.add(new FinishedSection(currentSection(), duration));
			}

			@Override
			protected long time() {
				return time;
			}
		};
	}

	@Test
	void start() {
		stackProfiler.start("A");

		assertEquals(1, stackProfiler.numberOfSections());
		assertEquals(Marker.of("A"), stackProfiler.currentSection());
	}

	@Test
	void close() {
		stackProfiler.start("A");

		time++;
		stackProfiler.close();

		assertFalse(stackProfiler.hasOpenSections());
		assertEquals(List.of(new FinishedSection(Marker.of("A"), 1)), endedMarkers);
	}

	@Test
	void closeWithResources() {
		try (var ignored = stackProfiler.start("A")) {
			time++;
		}

		assertFalse(stackProfiler.hasOpenSections());
		assertEquals(List.of(new FinishedSection(Marker.of("A"), 1)), endedMarkers);
	}

	@Test
	void section() {
		stackProfiler.start("A").start("B").start("C");

		assertEquals(Marker.of("A"), stackProfiler.section(0));
		assertEquals(Marker.of("B"), stackProfiler.section(1));
		assertEquals(Marker.of("C"), stackProfiler.section(2));
	}

	@Test
	void iterator() {
		stackProfiler.start("A").start("B").start("C");

		var it = stackProfiler.iterator();

		assertEquals(Marker.of("C"), it.next());

		stackProfiler.start("D");

		assertEquals(Marker.of("B"), it.next());
		assertEquals(Marker.of("A"), it.next());

		assertFalse(it.hasNext());

		assertEquals(4, stackProfiler.numberOfSections());
		assertEquals(Marker.of("A"), stackProfiler.section(0));
		assertEquals(Marker.of("B"), stackProfiler.section(1));
		assertEquals(Marker.of("C"), stackProfiler.section(2));
		assertEquals(Marker.of("D"), stackProfiler.section(3));
	}
}