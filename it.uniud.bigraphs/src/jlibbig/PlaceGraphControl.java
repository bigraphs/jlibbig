package jlibbig;

/**
 * Describes a control assignable to nodes of a place graph.
 * Place graph controls are uniquely identified by their immutable name.
 */
public interface PlaceGraphControl extends GraphControl {
	/**
	 * @return {@literal true} if the control is active
	 */
	public boolean isActive();
}
