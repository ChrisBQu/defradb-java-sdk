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

class DefraNewNodeResult {

	public long nodePtr;
    public String error;
    public int status;

    public DefraNewNodeResult(int status, String error, long ptr) {
        this.status = status;
        this.error = error;
        this.nodePtr = ptr;
    }
    
}
