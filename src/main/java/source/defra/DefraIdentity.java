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
 * A native DefraDB identity handle used to authorize and sign operations.
 *
 * <p>Identities created by {@link DefraNode#identityNew(String)} or
 * {@link DefraNode#identityFromPrivateKey(String)} must be released with
 * {@link DefraNode#freeIdentity(DefraIdentity)} when no longer needed.</p>
 */
public class DefraIdentity {

    private long ptr;

    /**
     * Wraps a native identity pointer.
     *
     * @param ptr the native identity pointer
     */
    public DefraIdentity(long ptr) {
        this.ptr = ptr;
    }

    /**
     * Returns the native identity pointer.
     *
     * @return the native identity pointer
     */
    public long getPointer() {
        return this.ptr;
    }
    
}
