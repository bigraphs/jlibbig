package it.uniud.mads.jlibbig.core.ldb;

import it.uniud.mads.jlibbig.core.Owned;
import it.uniud.mads.jlibbig.core.std.LinkEntity;
import it.uniud.mads.jlibbig.core.std.Node;
import it.uniud.mads.jlibbig.core.std.Root;
import it.uniud.mads.jlibbig.core.std.Site;

/**
 * Describes entities composing the place graph such as roots ({@link Root}),
 * nodes ({@link Node}), and sites ({@link Site}).
 *
 * @see LinkEntity
 */
public interface PlaceEntity extends Owned,
        it.uniud.mads.jlibbig.core.PlaceEntity {
}