package brownshome.profiling;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfilerTest {
	private TestProfiler testProfiler;

	@BeforeEach
	void setup() {
		testProfiler = new TestProfiler();
		Profiler.setProfiler(testProfiler);
	}

	@Test
	void closeAll() {
		testProfiler.start("A").start("B").start("C");

		testProfiler.closeAll();
		assertFalse(testProfiler.hasOpenSections());

		assertEquals(List.of(
				new TestProfiler.StartEvent(Marker.of("A")),
				new TestProfiler.StartEvent(Marker.of("B")),
				new TestProfiler.StartEvent(Marker.of("C")),
				new TestProfiler.StopEvent(Marker.of("C")),
				new TestProfiler.StopEvent(Marker.of("B")),
				new TestProfiler.StopEvent(Marker.of("A"))
		), testProfiler.events());
	}

	@Test
	void closeAllWithNoSections() {
		testProfiler.closeAll();

		assertFalse(testProfiler.hasOpenSections());
		assertEquals(Collections.emptyList(), testProfiler.events());
	}

	@Test
	void closeUntil() {
		testProfiler.start("A").start("B").start("C");

		testProfiler.closeUntil(Marker.of("A"));
		assertEquals(1, testProfiler.numberOfSections());
		assertEquals(Marker.of("A"), testProfiler.currentSection());

		assertEquals(List.of(
				new TestProfiler.StartEvent(Marker.of("A")),
				new TestProfiler.StartEvent(Marker.of("B")),
				new TestProfiler.StartEvent(Marker.of("C")),
				new TestProfiler.StopEvent(Marker.of("C")),
				new TestProfiler.StopEvent(Marker.of("B"))
		), testProfiler.events());
	}
}