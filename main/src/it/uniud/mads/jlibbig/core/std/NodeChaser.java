package it.uniud.mads.jlibbig.core.std;

import java.lang.ref.*;
import java.util.*;

import it.uniud.mads.jlibbig.core.Owner;
import it.uniud.mads.jlibbig.core.attachedProperties.*;
import it.uniud.mads.jlibbig.core.util.WeakHashSet;

/**
 * Objects created from this class can be used to keep trace of nodes and their
 * replicas.
 */
public class NodeChaser {

	private static final Owner FAKE_OWNER = new Owner() {
	};

	private final Map<Owner, WeakHashSet<Node>> index = new WeakHashMap<>();
	private final Map<Node, PropertyListener<Owner>> ownerLsts = new WeakHashMap<>();
	private final Map<Node, ReplicationListener> repLsts = new WeakHashMap<>();

	private Owner getOwnerKey(Node node) {
		return getOwnerKey(node.getOwner());
	}

	private Owner getOwnerKey(Owner owner) {
		if (owner == null)
			owner = FAKE_OWNER;
		return owner;
	}

	/**
	 * @return the collection of all chased nodes.
	 */
	public Collection<Node> getAll() {
		Collection<Node> col = new ArrayList<>(ownerLsts.keySet().size());
		Iterator<Owner> oi = index.keySet().iterator();
		while(oi.hasNext()){
			Owner o = oi.next();
			col.addAll(getAll(o));
		}
		return col;
	}

	/**
	 * Retrieves the chased nodes for the given owner. 
	 * @param owner
	 * @return the collection of all chased nodes belonging to the given owner.
	 */
	public Collection<Node> getAll(Owner owner) {
		WeakHashSet<Node> s = index.get(getOwnerKey(owner));
		if (s == null)
			return new HashSet<>();
		else
			return s.toSet();
	}

	/**
	 * Stops chasing every node.
	 */
	public void releaseAll() {
		Iterator<Owner> oi = index.keySet().iterator();
		while(oi.hasNext()){
			Owner o = oi.next();
			releaseAll(o);
			oi.remove();
		}
//		Iterator<Node> in = ownerLsts.keySet().iterator();
//		while(in.hasNext()){
//			EditableNode node = (EditableNode) in.next();
//			node.<Owner>getProperty(EditableNode.PROPERTY_OWNER)
//					.unregisterListener(ownerLsts.get(node));
//			node.unregisterListener(repLsts.remove(node));
//			in.remove();
//		}
	}

	/**
	 * Stops chasing the nodes for the given owner.
	 * @param owner
	 */
	public void releaseAll(Owner owner) {
		WeakHashSet<Node> set = index.get(owner);
		if (set == null)
			return;
		Iterator<Node> ir = set.iterator();
		while (ir.hasNext()) {
			Node node = ir.next();
			if (node != null) {
				node.<Owner> getProperty(EditableNode.PROPERTY_OWNER)
				.unregisterListener(ownerLsts.get(node));
				((EditableNode) node).unregisterListener(repLsts.remove(node));
				ir.remove();
			}
		}
	}

	/**
	 * Stops chasing the given node.
	 * @param node
	 */
	public void release(Node node) {
		if (isChased(node)) {
			node.<Owner> getProperty(EditableNode.PROPERTY_OWNER)
					.unregisterListener(ownerLsts.remove(node));
			((EditableNode) node).unregisterListener(repLsts.remove(node));
		}
	}

	/**
	 * @param node
	 * @return a boolean indicating whether the given node is being chased by this chaser.
	 */
	public boolean isChased(Node node) {
		return ownerLsts.containsKey(node);
	}

	/**
	 * Starts chasing the given node.
	 * @param node
	 */
	public void chase(Node node) {
		chase((EditableNode) node);
	}

	void chase(EditableNode node) {
		WeakHashSet<Node> ns = index.get(getOwnerKey(node));
		if (ns == null) {
			ns = new WeakHashSet<>();
			index.put(getOwnerKey(node), ns);
		}
		ns.add(node);
		// avoid strong references
		final WeakReference<EditableNode> ref = new WeakReference<>(node);

		PropertyListener<Owner> ol = new PropertyListener<Owner>() {
			@Override
			public void onChanged(Property<? extends Owner> property,
					Owner oldValue, Owner newValue) {
				WeakHashSet<Node> ns = index.get(getOwnerKey(oldValue));
				if (ns != null)
					ns.remove(ref.get());
				ns = index.get(getOwnerKey(newValue));
				if (ns == null) {
					ns = new WeakHashSet<>();
					index.put(getOwnerKey(newValue), ns);
				}
				ns.add(ref.get());
				onOwnerChanged(ref.get(), oldValue, newValue);
			}
		};
		node.<Owner> getProperty(EditableNode.PROPERTY_OWNER).registerListener(
				ol);
		ownerLsts.put(node, ol);
		ReplicationListener rl = new ReplicationListener() {
			@Override
			public void onReplicated(Replicating original, Replicating copy) {
				chase((EditableNode) copy);
				NodeChaser.this.onReplicated((Node) original, (Node) copy);
			}
		};
		repLsts.put(node, rl);
		node.registerListener(rl);

		onNodeAdded(node);
	}

	/**
	 * The method is invoked when the chaser starts chasing a node.
	 * @param node the new node.
	 */
	protected void onNodeAdded(Node node) {
	}

	/**
	 * The method is invoked after the owner of a node changes.
	 * @param node the node whose owner changed.
	 * @param oldValue the old owner.
	 * @param newValue the new owner.
	 */
	protected void onOwnerChanged(Node node, Owner oldValue, Owner newValue) {
	};

	/**
	 * The method is invoked after a node replicates.
	 * @param original the original node.
	 * @param copy the replica.
	 */
	protected void onReplicated(Node original, Node copy) {
	};
}
