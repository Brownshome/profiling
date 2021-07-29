package brownshome.profiling;

record StringMarker(String name) implements Marker {
	@Override
	public String toString() {
		return name;
	}
}
