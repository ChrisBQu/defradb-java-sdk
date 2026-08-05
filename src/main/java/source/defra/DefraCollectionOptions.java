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

public class DefraCollectionOptions {

    public String version;
    public String collectionID;
    public String name;
    public boolean getInactive;
    // enableSigning overrides the node-level signing setting for this operation.
    // null means unset (use the node-level default); TRUE/FALSE explicitly enable/disable it.
    public Boolean enableSigning;

    public DefraCollectionOptions() {
        this.version = "";
        this.collectionID = "";
        this.name = "";
    }
    
}
