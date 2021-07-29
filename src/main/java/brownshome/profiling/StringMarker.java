package brownshome.profiling;

/**
 * A marker defined by a single string name
 */
public record StringMarker(String name) implements Marker {
	@Override
	public String toString() {
		return name;
	}
}
