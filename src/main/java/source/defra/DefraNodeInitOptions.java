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

public class DefraNodeInitOptions {
	
    public String dbPath;
    public String listeningAddresses;
    public String replicatorRetryIntervals;
    public String peers;
    public DefraIdentity identity;
    public boolean inMemory;
    public boolean disableP2P;
    public boolean disableAPI;
    public boolean enableNodeACP;
    public int maxTransactionRetries;
    public String storeType;
    public long badgerFileSize;
    public byte[] badgerEncryptionKey;
    public boolean enableSigning;
    public byte[] searchableEncryptionKey;
    public long p2pBlockSyncTimeoutMs;
    public int lensPoolSize;
    public int chunkSize;
    public boolean enablePubSub;
    public boolean enableRelay;
    public boolean enableClearBackoffOnRetry;
    public byte[] p2pPrivateKey;
    public String httpAddress;
    public String httpAllowedOrigins;
    public String tlsCertPath;
    public String tlsKeyPath;
    public long httpReadTimeoutMs;
    public long httpWriteTimeoutMs;
    public long httpIdleTimeoutMs;
    public String documentACPType;
    public String documentACPPath;
    public String sourceHubChainID;
    public String sourceHubGRPCAddress;
    public String sourceHubCometRPCAddress;
    public String nodeACPPath;

    public DefraNodeInitOptions() {
        this.dbPath = "";
        this.listeningAddresses = "";
        this.replicatorRetryIntervals = "";
        this.peers = "";
        this.identity = null;
        this.inMemory = false;
        this.disableP2P = true;
        this.disableAPI = true;
        this.enableNodeACP = false;
        this.maxTransactionRetries = 0;
        this.storeType = "";
        this.badgerFileSize = 0;
        this.badgerEncryptionKey = null;
        this.enableSigning = false;
        this.searchableEncryptionKey = null;
        this.p2pBlockSyncTimeoutMs = 0;
        this.lensPoolSize = 0;
        this.chunkSize = 0;
        this.enablePubSub = false;
        this.enableRelay = false;
        this.enableClearBackoffOnRetry = false;
        this.p2pPrivateKey = null;
        this.httpAddress = "";
        this.httpAllowedOrigins = "";
        this.tlsCertPath = "";
        this.tlsKeyPath = "";
        this.httpReadTimeoutMs = 0;
        this.httpWriteTimeoutMs = 0;
        this.httpIdleTimeoutMs = 0;
        this.documentACPType = "";
        this.documentACPPath = "";
        this.sourceHubChainID = "";
        this.sourceHubGRPCAddress = "";
        this.sourceHubCometRPCAddress = "";
        this.nodeACPPath = "";
    }
    
}
