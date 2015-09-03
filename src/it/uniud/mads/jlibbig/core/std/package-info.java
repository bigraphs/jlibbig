/**
 * Standard implementation of bigraphs with abstract internal names but concrete
 * interfaces. This reflects on nodes and edges identity being instance based instead
 * of name based. As a consequence, operations like bigraph composition and 
 * juxtaposition are defined whenever the operands have compatible interfaces.
 */
package it.uniud.mads.jlibbig.core.std;