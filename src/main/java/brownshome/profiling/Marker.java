package brownshome.profiling;

/**
 * Identifies an object that can be used as a profiling marker
 */
public interface Marker {
	/**
	 * Creates a named marker
	 * @param name the name of the marker
	 * @return a named marker
	 */
	static Marker of(String name) {
		return new StringMarker(name);
	}
}
