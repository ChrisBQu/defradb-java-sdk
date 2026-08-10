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
 * Indicates that a DefraDB operation failed.
 */
public class DefraException extends Exception {
    /**
     * Creates an exception with the error message returned by DefraDB.
     *
     * @param message the DefraDB error message
     */
    public DefraException(String message) {
        super(message);
    }

    /**
     * Creates an exception with an error message and underlying cause.
     *
     * @param message the DefraDB error message
     * @param cause the underlying cause
     */
    public DefraException(String message, Throwable cause) {
        super(message, cause);
    }
}
