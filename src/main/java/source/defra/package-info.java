/**
 * Provides the Java API for running and interacting with an embedded DefraDB node.
 *
 * <p>Create a node with {@link source.defra.DefraNode}, select collections with
 * {@link source.defra.DefraCollectionOptions}, and use
 * {@link source.defra.DefraTransaction} when operations must be committed atomically.
 * Native operation failures are reported as {@link source.defra.DefraException}.</p>
 */
package source.defra;
