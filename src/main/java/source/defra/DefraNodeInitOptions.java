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
 * Configuration used to create a {@link DefraNode}.
 *
 * <p>The fields mirror DefraDB's node, store, database, P2P, HTTP, document
 * ACP, and node ACP options. Duration fields use milliseconds.</p>
 */
public class DefraNodeInitOptions {

    /** Filesystem path for the database store. */
    public String dbPath;
    /** Comma-separated P2P listen addresses. */
    public String listeningAddresses;
    /** Comma-separated transaction retry intervals. */
    public String replicatorRetryIntervals;
    /** Comma-separated bootstrap peer addresses. */
    public String peers;
    /** Identity used by the node, or {@code null} if unspecified. */
    public DefraIdentity identity;
    /** Whether the database store runs in memory. */
    public boolean inMemory;
    /** Whether P2P networking is disabled. */
    public boolean disableP2P;
    /** Whether the HTTP API server is disabled. */
    public boolean disableAPI;
    /** Whether node access control is enabled. */
    public boolean enableNodeACP;
    /** Maximum number of retries per transaction; zero uses the DefraDB default. */
    public int maxTransactionRetries;
    /** Store implementation name, such as {@code badger} or {@code memory}. */
    public String storeType;
    /** Maximum Badger value-log file size in bytes. */
    public long badgerFileSize;
    /** Badger encryption key, or {@code null} if encryption is disabled. */
    public byte[] badgerEncryptionKey;
    /** Whether block signing is enabled by default. */
    public boolean enableSigning;
    /** Key used for searchable encryption, or {@code null} if disabled. */
    public byte[] searchableEncryptionKey;
    /** Timeout in milliseconds for synchronizing block links over P2P. */
    public long p2pBlockSyncTimeoutMs;
    /** Number of lens runtime instances in the pool. */
    public int lensPoolSize;
    /** Blockstore chunk size; zero uses the DefraDB default. */
    public int chunkSize;
    /** Whether the P2P PubSub system is enabled. */
    public boolean enablePubSub;
    /** Whether the P2P relay system is enabled. */
    public boolean enableRelay;
    /** Whether connection backoff is cleared before retrying. */
    public boolean enableClearBackoffOnRetry;
    /** Private key for the P2P node, or {@code null} to generate one. */
    public byte[] p2pPrivateKey;
    /** Address on which the HTTP API listens. */
    public String httpAddress;
    /** Comma-separated origins allowed by HTTP CORS handling. */
    public String httpAllowedOrigins;
    /** Path to the HTTP TLS certificate. */
    public String tlsCertPath;
    /** Path to the HTTP TLS private key. */
    public String tlsKeyPath;
    /** HTTP connection read timeout in milliseconds. */
    public long httpReadTimeoutMs;
    /** HTTP connection write timeout in milliseconds. */
    public long httpWriteTimeoutMs;
    /** HTTP connection idle timeout in milliseconds. */
    public long httpIdleTimeoutMs;
    /** Document ACP implementation, such as {@code local} or {@code source-hub}. */
    public String documentACPType;
    /** Filesystem path for the document ACP system. */
    public String documentACPPath;
    /** SourceHub chain identifier used by document ACP. */
    public String sourceHubChainID;
    /** SourceHub gRPC address used by document ACP. */
    public String sourceHubGRPCAddress;
    /** SourceHub Comet RPC address used by document ACP. */
    public String sourceHubCometRPCAddress;
    /** Filesystem path for the node ACP system. */
    public String nodeACPPath;

    /** Creates options initialized to the native client's default values. */
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
