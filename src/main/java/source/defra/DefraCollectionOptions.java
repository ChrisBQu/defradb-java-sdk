// Copyright 2026 Democratized Data Foundation
//
// Use of this software is governed by the Business Source License
// included in the file licenses/BSL.txt.
//
// As of the Change Date specified in that file, in accordance with
// the Business Source License, use of this software will be governed
// by the Apache License, Version 2.0, included in the file
// licenses/APL.txt.

package source.defra;

/**
 * Identifies a DefraDB collection or collection version for an operation.
 *
 * <p>Set whichever identifier is appropriate for the operation. Empty values
 * are treated as unspecified by the native client.</p>
 */
public class DefraCollectionOptions {

    /** The collection version identifier, or an empty string if unspecified. */
    public String version;
    /** The collection identifier, or an empty string if unspecified. */
    public String collectionID;
    /** The collection name, or an empty string if unspecified. */
    public String name;
    /** Whether inactive collection versions may be selected. */
    public boolean getInactive;
    /**
     * Overrides the node-level block-signing setting for the operation.
     * A {@code null} value uses the node-level default; {@code true} or
     * {@code false} explicitly enables or disables signing.
     */
    public Boolean enableSigning;

    /** Creates collection options with all identifiers unspecified. */
    public DefraCollectionOptions() {
        this.version = "";
        this.collectionID = "";
        this.name = "";
    }
    
}
