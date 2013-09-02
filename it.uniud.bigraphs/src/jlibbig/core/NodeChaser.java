package jlibbig.core;

import java.lang.ref.*;
import java.util.*;
import jlibbig.core.attachedProperties.*;

public class NodeChaser {

	private static final Owner FAKE_OWNER = new Owner() {
	};

//	ReferenceQueue<Node> nodeQueue  = new ReferenceQueue<Node>();

	private final Map<Owner, WeakHashSet<Node>> index = new WeakHashMap<>();
	private final Map<Node, PropertyListener<Owner>> ownerLsts = new WeakHashMap<>();
	private final Map<Node, ReplicateListener> repLsts = new WeakHashMap<>();

//	private final void processQueue() {
//		Reference<? extends Node> ref = null;
//		do {
//			ref = this.nodeQueue.poll();
//			onNodeRemoved((Node) ref.get());
//		} while (ref != null);
//	}

	private Owner getOwnerKey(Node node) {
		return getOwnerKey(node.getOwner());
	}

	private Owner getOwnerKey(Owner owner) {
		if (owner == null)
			owner = FAKE_OWNER;
		return owner;
	}

	public Set<Node> getAll() {
		return ownerLsts.keySet();
	}

	public Set<Node> getAll(Owner owner) {
		WeakHashSet<Node> s = index.get(getOwnerKey(owner));
		if (s == null)
			return new HashSet<>();
		else
			return s.toSet();
	}

	public void releaseAll() {
		for (Node node : ownerLsts.keySet()) {
			node.<Owner>getProperty(EditableNode.PROPERTY_OWNER)
					.unregisterListener(ownerLsts.remove(node));
			((EditableNode) node).unregisterListener(repLsts.remove(node));
			// onNodeRemoved(node);
		}
	}

	public void releaseAll(Owner owner) {
		WeakHashSet<Node> set = index.get(owner);
		if (set == null)
			return;
		Iterator<Node> ir = set.iterator();
		while (ir.hasNext()) {
			Node node = ir.next();
			if (node != null) {
				node.<Owner>getProperty(EditableNode.PROPERTY_OWNER)
						.unregisterListener(ownerLsts.remove(node));
				((EditableNode) node).unregisterListener(repLsts.remove(node));
				// onNodeRemoved(node);
			}
		}
	}

	public void release(Node node) {
		if (isChased(node)) {
			node.<Owner>getProperty(EditableNode.PROPERTY_OWNER)
					.unregisterListener(ownerLsts.remove(node));
			((EditableNode) node).unregisterListener(repLsts.remove(node));
		}
	}

	public boolean isChased(Node node) {
		return ownerLsts.containsKey(node);
	}

	public void chase(Node node) {
		chased((EditableNode) node);
	}

	void chased(EditableNode node) {
		WeakHashSet<Node> ns = index.get(getOwnerKey(node));
		if (ns == null) {
			ns = new WeakHashSet<>();
			index.put(getOwnerKey(node), ns);
		}
		ns.add(node);
		// avoid strong references
		final WeakReference<EditableNode> ref = new WeakReference<>(node); //,nodeQueue);

		PropertyListener<Owner> ol = new PropertyListener<Owner>() {
			@Override
			public void onChange(Property<? extends Owner> property, Owner oldValue,
					Owner newValue) {
				WeakHashSet<Node> ns = index.get(getOwnerKey(oldValue));
				if (ns != null)
					ns.remove(ref.get());
				ns = index.get(getOwnerKey(newValue));
				if (ns == null) {
					ns = new WeakHashSet<>();
					index.put(getOwnerKey(newValue), ns);
				}
				ns.add(ref.get());
				onOwnerChanges(ref.get(),oldValue,newValue);
			}
		};
		node.<Owner>getProperty(EditableNode.PROPERTY_OWNER).registerListener(ol);
		ownerLsts.put(node, ol);
		ReplicateListener rl = new ReplicateListener() {
			@Override
			public void onReplicate(Replicable original, Replicable copy) {
				chased((EditableNode) copy);
				onReplicates((Node) original, (Node) copy);
			}
		};
		repLsts.put(node, rl);
		node.registerListener(rl);

		onNodeAdded(node);
	}

	protected void onNodeAdded(Node node){}

	// protected void onNodeRemoved(Node node){}

	protected void onOwnerChanges(Node node, Owner oldValue,Owner newValue){};

	protected void onReplicates(Node original, Node copy){};
}
