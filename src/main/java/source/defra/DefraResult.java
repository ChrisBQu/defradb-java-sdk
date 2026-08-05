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

class DefraResult {

	public String value;
    public String error;
    public int status;

    public DefraResult(int status, String error, String value) {
        this.status = status;
        this.error = error;
        this.value = value;
    }
    
}
