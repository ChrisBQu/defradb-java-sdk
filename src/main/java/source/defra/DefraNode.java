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

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * An embedded DefraDB node.
 *
 * <p>Most methods returning {@code String} expose the native result produced by DefraDB.
 * Overloads accepting a {@link DefraIdentity} execute as that identity; other
 * overloads use the node's default authorization context.</p>
 */
public class DefraNode {

    static {
        NativeLoader.load("nativewrapper");
        NativeLoader.load("defradb");
    }

    private native DefraNewNodeResult NewNodeNative(DefraNodeInitOptions options);
    private native DefraResult NodeCloseNative(long nodePtr);
    
    // ACP Methods
    private native DefraResult ACPAddDACPolicyNative(long nodePtr, long identityPtr, String policy);
    private native DefraResult ACPAddDACActorRelationshipNative(long nodePtr, long identityPtr, String collection, String docID, String relation, String actor);
    private native DefraResult ACPDeleteDACActorRelationshipNative(long nodePtr, long identityPtr, String collection, String docID, String relation, String actor);
    private native DefraResult ACPDisableNACNative(long nodePtr, long identityPtr);
    private native DefraResult ACPReEnableNACNative(long nodePtr, long identityPtr);
    private native DefraResult ACPAddNACActorRelationshipNative(long nodePtr, long identityPtr, String relation, String actor);
    private native DefraResult ACPDeleteNACActorRelationshipNative(long nodePtr, long identityPtr, String relation, String actor);
    private native DefraResult ACPGetNACStatusNative(long nodePtr, long identityPtr);
    
    // Collection Methods
    private native DefraResult AddCollectionNative(long nodePtr, String sdl, long identityPtr);
    private native DefraResult DescribeCollectionNative(long nodePtr, DefraCollectionOptions options, long identityPtr);
    private native DefraResult PatchCollectionNative(long nodePtr, String patch, String lensConfig, long identityPtr);
    private native DefraResult SetActiveCollectionNative(long nodePtr, DefraCollectionOptions options, long identityPtr);
    private native DefraResult TruncateCollectionNative(long nodePtr, DefraCollectionOptions options, long identityPtr);
    private native DefraResult DeleteCollectionNative(long nodePtr, String names, int activeOnly, long identityPtr);

    // Document Methods
    private native DefraResult AddDocumentNative(long nodePtr, String json, boolean isEncrypted, String encryptedFields, DefraCollectionOptions options, long identityPtr);
    private native DefraResult DeleteDocumentNative(long nodePtr, String docID, String filter, DefraCollectionOptions options, long identityPtr);
    private native DefraResult GetDocumentNative(long nodePtr, String docID, boolean showDeleted, DefraCollectionOptions options, long identityPtr);
    private native DefraResult UpdateDocumentNative(long nodePtr, String docID, String filter, String updater, DefraCollectionOptions options, long identityPtr);
    
    // Encrypted Index Methods
    private native DefraResult NewEncryptedIndexNative(long nodePtr, String collectionName, String fieldName, long identityPtr);
    private native DefraResult ListEncryptedIndexesNative(long nodePtr, String collectionName, long identityPtr);
    private native DefraResult DeleteEncryptedIndexNative(long nodePtr, String collectionName, String fieldName, long identityPtr);
    
    // Index Methods
    private native DefraResult NewIndexNative(long nodePtr, String indexName, String fields, boolean isUnique, String vectorJSON, DefraCollectionOptions options, long identityPtr);
    private native DefraResult ListIndexesNative(long nodePtr, DefraCollectionOptions options, long identityPtr);
    private native DefraResult DeleteIndexNative(long nodePtr, String indexName, DefraCollectionOptions options, long identityPtr);
    
    // Identity Methods
    private native DefraIdentityResult IdentityNewNative(String keyType);
    private native DefraIdentityResult NewIdentityFromPrivateKeyNative(String privateKeyHex);
    private native DefraResult ExportIdentityPrivateKeyNative(long identityPtr);
    private native DefraResult GetNodeIdentityNative(long nodePtr);
    private native void FreeIdentityNative(long identityPtr);

    // Action Methods
    private native DefraResult ListActionsNative(long nodePtr, long identityPtr);
    
    // Lens Methods
    private native DefraResult SetLensNative(long nodePtr, long identityPtr, String src, String dst, String cfg);
    private native DefraResult AddLensNative(long nodePtr, long identityPtr, String cfg);
    private native DefraResult ListLensesNative(long nodePtr, long identityPtr);
    
    // P2P Methods
    private native DefraResult GetP2PInfoNative(long nodePtr, long identityPtr);
    private native DefraResult ListP2PActivePeersNative(long nodePtr, long identityPtr);
    private native DefraResult ListP2PReplicatorsNative(long nodePtr, long identityPtr);
    private native DefraResult AddP2PReplicatorNative(long nodePtr, String collections, String addresses, long identityPtr);
    private native DefraResult DeleteP2PReplicatorNative(long nodePtr, String collections, String id, long identityPtr);
    private native DefraResult AddP2PCollectionNative(long nodePtr, String collections, long identityPtr);
    private native DefraResult DeleteP2PCollectionNative(long nodePtr, String collections, long identityPtr);
    private native DefraResult ListP2PCollectionsNative(long nodePtr, long identityPtr);
    private native DefraResult AddP2PDocumentNative(long nodePtr, String collections, long identityPtr);
    private native DefraResult DeleteP2PDocumentNative(long nodePtr, String collections, long identityPtr);
    private native DefraResult ListP2PDocumentsNative(long nodePtr, long identityPtr);
    private native DefraResult SyncP2PDocumentsNative(long nodePtr, String collection, String docIDs, String timeout, long identityPtr);
    private native DefraResult SyncP2PCollectionVersionsNative(long nodePtr, String versionIDs, String timeout, long identityPtr);
    private native DefraResult SyncP2PBranchableCollectionNative(long nodePtr, String collectionID, String timeout, long identityPtr);
    private native DefraResult ConnectP2PPeersNative(long nodePtr, String peerAddresses, long identityPtr);
    private native DefraResult DisconnectP2PPeersNative(long nodePtr, String peerAddresses, long identityPtr);

    // Query Methods
    private native DefraResult ExecuteQueryNative(long nodePtr, String query, long identityPtr, String operationName, String variables);
    private native DefraResult PollSubscriptionNative(String id);
    private native DefraResult CloseSubscriptionNative(String id);
    
    // Version Method
    private native DefraResult GetVersionNative(boolean flagFull, boolean flagJSON);
    
    // View Methods
    private native DefraResult AddViewNative(long nodePtr, String query, String sdl, String transformCID, long identityPtr);
    private native DefraResult RefreshViewNative(long nodePtr, DefraCollectionOptions options, long identityPtr);
    
    // Block Verification
    private native DefraResult VerifyBlockSignatureNative(long nodePtr, String keyType, String publicKey, String cid, long identityPtr);
    
    // Transaction Methods
    private native DefraTransactionResult TransactionCreateNative(long nodePtr, boolean isReadOnly);
    
	// Private members
    private long nodePtr;
	private static class ActiveSubscription {
		final String handle;
		final Consumer<String> callback;

		ActiveSubscription(String handle, Consumer<String> callback) {
			this.handle = handle;
			this.callback = callback;
		}
	}

	private final ConcurrentHashMap<String, ActiveSubscription> subscriptions = new ConcurrentHashMap<>();
	private final ExecutorService callbackExecutor = Executors.newCachedThreadPool();
	private volatile boolean dispatcherRunning = true;
	private final Thread dispatcherThread;

	// Package-private constructor used by fromPtr — does not call NewNodeNative.
	// The node lifecycle is managed externally (e.g. by the Go test harness).
	DefraNode(long externalNodePtr) {
		this.nodePtr = externalNodePtr;
		this.dispatcherThread = new Thread(() -> {
			while (dispatcherRunning) {
				for (ConcurrentHashMap.Entry<String, ActiveSubscription> entry : subscriptions.entrySet()) {
					ActiveSubscription sub = entry.getValue();
					DefraResult poll = PollSubscriptionNative(sub.handle);
					if (poll.status == 0 && poll.value != null && !poll.value.isEmpty()) {
						callbackExecutor.submit(() -> sub.callback.accept(poll.value));
					}
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		});
		this.dispatcherThread.setDaemon(true);
		this.dispatcherThread.setName("defra-subscription-dispatcher");
		this.dispatcherThread.start();
	}

	// fromPtr wraps an externally-managed node pointer (a cgo.Handle value from
	// the Go test harness). The caller owns the handle lifecycle.
	static DefraNode fromPtr(long nodePtr) {
		return new DefraNode(nodePtr);
	}

	// Constructor
	/**
	 * Creates and starts an embedded DefraDB node.
	 *
	 * @param options node configuration
	 * @throws DefraException if the native node cannot be created
	 */
	public DefraNode(DefraNodeInitOptions options) throws DefraException {
		DefraNewNodeResult result = NewNodeNative(options);
		if (result.status != 0) {
			this.nodePtr = 0;
			throw new DefraException(result.error);
		}
		this.nodePtr = result.nodePtr;
        // Create a dispatcher thread for handling subscription and callback execution
		this.dispatcherThread = new Thread(() -> {
			while (dispatcherRunning) {
				for (ConcurrentHashMap.Entry<String, ActiveSubscription> entry : subscriptions.entrySet()) {
					ActiveSubscription sub = entry.getValue();
					DefraResult poll = PollSubscriptionNative(sub.handle);
					if (poll.status == 0 && poll.value != null && !poll.value.isEmpty()) {
						callbackExecutor.submit(() -> sub.callback.accept(poll.value));
					}
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		});
		this.dispatcherThread.setDaemon(true);
		this.dispatcherThread.setName("defra-subscription-dispatcher");
		this.dispatcherThread.start();
	}

    /**
     * Closes the node and releases its native resources.
     *
     * @throws DefraException if DefraDB rejects the operation
     */
    public void close() throws DefraException {
		dispatcherRunning = false;
		dispatcherThread.interrupt();
		callbackExecutor.shutdown();
        DefraResult result = NodeCloseNative(this.nodePtr);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
    }


    // ACP Methods
    /**
     * Adds a document access-control policy.
     *
     * @param policy policy definition encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPAddDACPolicy(String policy) throws DefraException {
        DefraResult result = ACPAddDACPolicyNative(this.nodePtr, 0, policy);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds a document access-control policy.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param policy policy definition encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPAddDACPolicy(String policy, DefraIdentity identity) throws DefraException {
        DefraResult result = ACPAddDACPolicyNative(this.nodePtr, identity.getPointer(), policy);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds an actor relationship to a document access-control policy.
     *
     * @param collection collection name or collection selector encoded as JSON
     * @param docID document identifier
     * @param relation relationship name
     * @param actor actor DID
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPAddDACActorRelationship(String collection, String docID, String relation, String actor) throws DefraException {
        DefraResult result = ACPAddDACActorRelationshipNative(this.nodePtr, 0, collection, docID, relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds an actor relationship to a document access-control policy.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collection collection name or collection selector encoded as JSON
     * @param docID document identifier
     * @param relation relationship name
     * @param actor actor DID
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPAddDACActorRelationship(String collection, String docID, String relation, String actor, DefraIdentity identity) throws DefraException {
        DefraResult result = ACPAddDACActorRelationshipNative(this.nodePtr, identity.getPointer(), collection, docID, relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }   

    /**
     * Deletes an actor relationship from a document access-control policy.
     *
     * @param collection collection name or collection selector encoded as JSON
     * @param docID document identifier
     * @param relation relationship name
     * @param actor actor DID
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPDeleteDACActorRelationship(String collection, String docID, String relation, String actor) throws DefraException {
        DefraResult result = ACPDeleteDACActorRelationshipNative(this.nodePtr, 0, collection, docID, relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an actor relationship from a document access-control policy.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collection collection name or collection selector encoded as JSON
     * @param docID document identifier
     * @param relation relationship name
     * @param actor actor DID
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPDeleteDACActorRelationship(String collection, String docID, String relation, String actor, DefraIdentity identity) throws DefraException {
        DefraResult result = ACPDeleteDACActorRelationshipNative(this.nodePtr, identity.getPointer(), collection, docID, relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Disables node access control.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPDisableNAC() throws DefraException {
        DefraResult result = ACPDisableNACNative(this.nodePtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Disables node access control.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPDisableNAC(DefraIdentity identity) throws DefraException {
        DefraResult result = ACPDisableNACNative(this.nodePtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Re-enables node access control.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPReEnableNAC() throws DefraException {
        DefraResult result = ACPReEnableNACNative(this.nodePtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Re-enables node access control.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPReEnableNAC(DefraIdentity identity) throws DefraException {
        DefraResult result = ACPReEnableNACNative(this.nodePtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds an actor relationship to the node access-control policy.
     *
     * @param relation relationship name
     * @param actor actor DID
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPAddNACActorRelationship(String relation, String actor) throws DefraException {
        DefraResult result = ACPAddNACActorRelationshipNative(this.nodePtr, 0, relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds an actor relationship to the node access-control policy.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param relation relationship name
     * @param actor actor DID
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPAddNACActorRelationship(String relation, String actor, DefraIdentity identity) throws DefraException {
        DefraResult result = ACPAddNACActorRelationshipNative(this.nodePtr, identity.getPointer(), relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an actor relationship from the node access-control policy.
     *
     * @param relation relationship name
     * @param actor actor DID
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPDeleteNACActorRelationship(String relation, String actor) throws DefraException {
        DefraResult result = ACPDeleteNACActorRelationshipNative(this.nodePtr, 0, relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an actor relationship from the node access-control policy.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param relation relationship name
     * @param actor actor DID
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPDeleteNACActorRelationship(String relation, String actor, DefraIdentity identity) throws DefraException {
        DefraResult result = ACPDeleteNACActorRelationshipNative(this.nodePtr, identity.getPointer(), relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the node access-control status as JSON.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPGetNACStatus() throws DefraException {
        DefraResult result = ACPGetNACStatusNative(this.nodePtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the node access-control status as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPGetNACStatus(DefraIdentity identity) throws DefraException {
        DefraResult result = ACPGetNACStatusNative(this.nodePtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Collection Methods
    /**
     * Adds collection definitions from Schema Definition Language (SDL).
     *
     * @param sdl Schema Definition Language (SDL) collection definition
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addCollection(String sdl) throws DefraException {
        DefraResult result = AddCollectionNative(this.nodePtr, sdl, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds collection definitions from Schema Definition Language (SDL).
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param sdl Schema Definition Language (SDL) collection definition
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addCollection(String sdl, DefraIdentity identity) throws DefraException {
        DefraResult result = AddCollectionNative(this.nodePtr, sdl, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns matching collection descriptions as JSON.
     *
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String describeCollection(DefraCollectionOptions options) throws DefraException {
        DefraResult result = DescribeCollectionNative(this.nodePtr, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Helper function for getCollectionByName functions
    private DefraCollection getCollectionByNameWithIdentityPointer(String name, long identityPtr) throws DefraException {
		DefraCollectionOptions copts = new DefraCollectionOptions();
		copts.name = name;
		DefraResult describeResult = DescribeCollectionNative(this.nodePtr, copts, identityPtr);
		if (describeResult.status != 0) {
			throw new DefraException(describeResult.error);
		}
        String collectionID = "";
        String versionID = "";
        try {
            JSONArray array = new JSONArray(describeResult.value);
            if (array.length() == 0) {
            throw new DefraException("Collection with name '" + name + "' not found");
            }
            JSONObject first = array.getJSONObject(0);
            collectionID = first.getString("CollectionID");
            versionID = first.getString("VersionID");
        } catch (JSONException e) {
            throw new DefraException(e.getMessage());
        }
		return new DefraCollection(this.nodePtr, name, collectionID, versionID);
    }
	
	/**
	 * Returns a handle for the active collection with the given name.
	 *
	 * @param name collection name
	 * @return a handle to the active collection
	 * @throws DefraException if DefraDB rejects the operation
	 */
	public DefraCollection getCollectionByName(String name) throws DefraException {
        return getCollectionByNameWithIdentityPointer(name, 0);
	}
	
	/**
	 * Returns a handle for the active collection with the given name.
	 *
	 * <p>If an identity parameter is present, it authorizes this operation.</p>
	 *
	 * @param name collection name
	 * @param identity identity authorizing the operation
	 * @return a handle to the active collection
	 * @throws DefraException if DefraDB rejects the operation
	 */
	public DefraCollection getCollectionByName(String name, DefraIdentity identity) throws DefraException {
        return getCollectionByNameWithIdentityPointer(name, identity.getPointer());
	}

    /**
     * Returns matching collection descriptions as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String describeCollection(DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = DescribeCollectionNative(this.nodePtr, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Applies a schema patch and optional migration lens configuration.
     *
     * @param patch schema patch encoded as JSON
     * @param lensConfig migration lens configuration, or an empty string
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String patchCollection(String patch, String lensConfig) throws DefraException {
        DefraResult result = PatchCollectionNative(this.nodePtr, patch, lensConfig, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Applies a schema patch and optional migration lens configuration.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param patch schema patch encoded as JSON
     * @param lensConfig migration lens configuration, or an empty string
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String patchCollection(String patch, String lensConfig, DefraIdentity identity) throws DefraException {
        DefraResult result = PatchCollectionNative(this.nodePtr, patch, lensConfig, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Makes the selected collection version active.
     *
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String setActiveCollection(DefraCollectionOptions options) throws DefraException {
        DefraResult result = SetActiveCollectionNative(this.nodePtr, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Makes the selected collection version active.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String setActiveCollection(DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = SetActiveCollectionNative(this.nodePtr, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes every document in the selected collection.
     *
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String truncateCollection(DefraCollectionOptions options) throws DefraException {
        DefraResult result = TruncateCollectionNative(this.nodePtr, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes every document in the selected collection.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String truncateCollection(DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = TruncateCollectionNative(this.nodePtr, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes the selected collections.
     *
     * @param names collection names
     * @param activeOnly whether only active collection versions are deleted
     * @throws DefraException if DefraDB rejects the operation
     */
    public void deleteCollection(String[] names, boolean activeOnly) throws DefraException {
        DefraResult result = DeleteCollectionNative(this.nodePtr, String.join(",", names), activeOnly ? 1 : 0, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
    }

    /**
     * Deletes the selected collections.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param names collection names
     * @param activeOnly whether only active collection versions are deleted
     * @param identity identity authorizing the operation
     * @throws DefraException if DefraDB rejects the operation
     */
    public void deleteCollection(String[] names, boolean activeOnly, DefraIdentity identity) throws DefraException {
        DefraResult result = DeleteCollectionNative(this.nodePtr, String.join(",", names), activeOnly ? 1 : 0, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
    }

    // Document Methods
    /**
     * Adds a JSON document to the selected collection.
     *
     * @param json document encoded as JSON
     * @param isEncrypted whether to encrypt the document
     * @param encryptedFields comma-separated field names to encrypt
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addDocument(String json, boolean isEncrypted, String encryptedFields, DefraCollectionOptions options) throws DefraException {
        DefraResult result = AddDocumentNative(this.nodePtr, json, isEncrypted, encryptedFields, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds a JSON document to the selected collection.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param json document encoded as JSON
     * @param isEncrypted whether to encrypt the document
     * @param encryptedFields comma-separated field names to encrypt
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addDocument(String json, boolean isEncrypted, String encryptedFields, DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = AddDocumentNative(this.nodePtr, json, isEncrypted, encryptedFields, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes the document matching an ID and optional filter.
     *
     * @param docID document identifier
     * @param filter JSON filter that must match, or an empty string
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteDocument(String docID, String filter, DefraCollectionOptions options) throws DefraException {
        DefraResult result = DeleteDocumentNative(this.nodePtr, docID, filter, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes the document matching an ID and optional filter.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param docID document identifier
     * @param filter JSON filter that must match, or an empty string
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteDocument(String docID, String filter, DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = DeleteDocumentNative(this.nodePtr, docID, filter, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns a document by ID as JSON.
     *
     * @param docID document identifier
     * @param showDeleted whether a deleted document may be returned
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String getDocument(String docID, boolean showDeleted, DefraCollectionOptions options) throws DefraException {
        DefraResult result = GetDocumentNative(this.nodePtr, docID, showDeleted, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns a document by ID as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param docID document identifier
     * @param showDeleted whether a deleted document may be returned
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String getDocument(String docID, boolean showDeleted, DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = GetDocumentNative(this.nodePtr, docID, showDeleted, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Updates the document matching an ID and optional filter.
     *
     * @param docID document identifier
     * @param filter JSON filter that must match, or an empty string
     * @param updater JSON update expression
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String updateDocument(String docID, String filter, String updater, DefraCollectionOptions options) throws DefraException {
        DefraResult result = UpdateDocumentNative(this.nodePtr, docID, filter, updater, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Updates the document matching an ID and optional filter.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param docID document identifier
     * @param filter JSON filter that must match, or an empty string
     * @param updater JSON update expression
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String updateDocument(String docID, String filter, String updater, DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = UpdateDocumentNative(this.nodePtr, docID, filter, updater, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Encrypted Index Methods
    /**
     * Creates an encrypted index on a collection field.
     *
     * @param collectionName collection name
     * @param fieldName field name
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String newEncryptedIndex(String collectionName, String fieldName) throws DefraException {
        DefraResult result = NewEncryptedIndexNative(this.nodePtr, collectionName, fieldName, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Creates an encrypted index on a collection field.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collectionName collection name
     * @param fieldName field name
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String newEncryptedIndex(String collectionName, String fieldName, DefraIdentity identity) throws DefraException {
        DefraResult result = NewEncryptedIndexNative(this.nodePtr, collectionName, fieldName, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns encrypted-index descriptions as JSON.
     *
     * @param collectionName collection name
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listEncryptedIndexes(String collectionName) throws DefraException {
        DefraResult result = ListEncryptedIndexesNative(this.nodePtr, collectionName, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns encrypted-index descriptions as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collectionName collection name
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listEncryptedIndexes(String collectionName, DefraIdentity identity) throws DefraException {
        DefraResult result = ListEncryptedIndexesNative(this.nodePtr, collectionName, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an encrypted index from a collection field.
     *
     * @param collectionName collection name
     * @param fieldName field name
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteEncryptedIndex(String collectionName, String fieldName) throws DefraException {
        DefraResult result = DeleteEncryptedIndexNative(this.nodePtr, collectionName, fieldName, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an encrypted index from a collection field.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collectionName collection name
     * @param fieldName field name
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteEncryptedIndex(String collectionName, String fieldName, DefraIdentity identity) throws DefraException {
        DefraResult result = DeleteEncryptedIndexNative(this.nodePtr, collectionName, fieldName, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Index Methods
    /**
     * Creates an index on the selected collection.
     *
     * @param indexName index name
     * @param fields comma-separated fields with optional ASC or DESC order
     * @param isUnique whether indexed values must be unique
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String newIndex(String indexName, String fields, boolean isUnique, DefraCollectionOptions options) throws DefraException {
        DefraResult result = NewIndexNative(this.nodePtr, indexName, fields, isUnique, null, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Creates an index on the selected collection.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param indexName index name
     * @param fields comma-separated fields with optional ASC or DESC order
     * @param isUnique whether indexed values must be unique
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String newIndex(String indexName, String fields, boolean isUnique, DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = NewIndexNative(this.nodePtr, indexName, fields, isUnique, null, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns index descriptions for the selected collection as JSON.
     *
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listIndexes(DefraCollectionOptions options) throws DefraException {
        DefraResult result = ListIndexesNative(this.nodePtr, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns index descriptions for the selected collection as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listIndexes(DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = ListIndexesNative(this.nodePtr, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an index from the selected collection.
     *
     * @param indexName index name
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteIndex(String indexName, DefraCollectionOptions options) throws DefraException {
        DefraResult result = DeleteIndexNative(this.nodePtr, indexName, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an index from the selected collection.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param indexName index name
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteIndex(String indexName, DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = DeleteIndexNative(this.nodePtr, indexName, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Identity Methods
    /**
     * Creates a new identity using the requested key type.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param keyType key type supported by DefraDB
     * @return the new identity handle
     * @throws DefraException if DefraDB rejects the operation
     */
    public DefraIdentity identityNew(String keyType) throws DefraException {
        DefraIdentityResult identResult = IdentityNewNative(keyType);
        if (identResult.status != 0) {
            throw new DefraException(identResult.error);
        }
        return new DefraIdentity(identResult.identityPtr);
    }

    /**
     * Returns the public identity of the node as JSON.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String getNodeIdentity() throws DefraException {
        DefraResult result = GetNodeIdentityNative(this.nodePtr);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Imports an identity from a hexadecimal private key.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param privateKeyHex private key encoded as hexadecimal
     * @return the imported identity handle
     * @throws DefraException if DefraDB rejects the operation
     */
    public DefraIdentity identityFromPrivateKey(String privateKeyHex) throws DefraException {
        DefraIdentityResult identResult = NewIdentityFromPrivateKeyNative(privateKeyHex);
        if (identResult.status != 0) {
            throw new DefraException(identResult.error);
        }
        return new DefraIdentity(identResult.identityPtr);
    }

    /**
     * Exports an identity private key as a hexadecimal string.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the private key encoded as hexadecimal
     * @throws DefraException if DefraDB rejects the operation
     */
    public String exportIdentityPrivateKey(DefraIdentity identity) throws DefraException {
        DefraResult result = ExportIdentityPrivateKeyNative(identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Releases a native identity handle.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @throws DefraException if DefraDB rejects the operation
     */
    public void freeIdentity(DefraIdentity identity) throws DefraException {
        FreeIdentityNative(identity.getPointer());
    }

    // Action Methods
    /**
     * Returns the access-control actions available to an identity as JSON.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listActions() throws DefraException {
        DefraResult result = ListActionsNative(this.nodePtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the access-control actions available to an identity as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listActions(DefraIdentity identity) throws DefraException {
        DefraResult result = ListActionsNative(this.nodePtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Lens Methods
    /**
     * Sets a migration lens between collection versions.
     *
     * @param src source collection version identifier
     * @param dst destination collection version identifier
     * @param cfg lens configuration encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String setLens(String src, String dst, String cfg) throws DefraException {
        DefraResult result = SetLensNative(this.nodePtr, 0, src, dst, cfg);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Sets a migration lens between collection versions.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param src source collection version identifier
     * @param dst destination collection version identifier
     * @param cfg lens configuration encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String setLens(String src, String dst, String cfg, DefraIdentity identity) throws DefraException {
        DefraResult result = SetLensNative(this.nodePtr, identity.getPointer(), src, dst, cfg);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds a lens from its configuration.
     *
     * @param cfg lens configuration encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addLens(String cfg) throws DefraException {
        DefraResult result = AddLensNative(this.nodePtr, 0, cfg);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds a lens from its configuration.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param cfg lens configuration encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addLens(String cfg, DefraIdentity identity) throws DefraException {
        DefraResult result = AddLensNative(this.nodePtr, identity.getPointer(), cfg);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns registered lens descriptions as JSON.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listLenses() throws DefraException {
        DefraResult result = ListLensesNative(this.nodePtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns registered lens descriptions as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listLenses(DefraIdentity identity) throws DefraException {
        DefraResult result = ListLensesNative(this.nodePtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // P2P Methods
    /**
     * Returns information about the P2P node as JSON.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String getP2PInfo() throws DefraException {
        DefraResult result = GetP2PInfoNative(this.nodePtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns information about the P2P node as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String getP2PInfo(DefraIdentity identity) throws DefraException {
        DefraResult result = GetP2PInfoNative(this.nodePtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns active P2P peers as JSON.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PActivePeers() throws DefraException {
        DefraResult result = ListP2PActivePeersNative(this.nodePtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns active P2P peers as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PActivePeers(DefraIdentity identity) throws DefraException {
        DefraResult result = ListP2PActivePeersNative(this.nodePtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns configured P2P replicators as JSON.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PReplicators() throws DefraException {
        DefraResult result = ListP2PReplicatorsNative(this.nodePtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns configured P2P replicators as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PReplicators(DefraIdentity identity) throws DefraException {
        DefraResult result = ListP2PReplicatorsNative(this.nodePtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds a P2P replicator for collections and peer addresses.
     *
     * @param collections collection selectors encoded as JSON
     * @param addresses peer addresses encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addP2PReplicator(String collections, String addresses) throws DefraException {
        DefraResult result = AddP2PReplicatorNative(this.nodePtr, collections, addresses, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds a P2P replicator for collections and peer addresses.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collections collection selectors encoded as JSON
     * @param addresses peer addresses encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addP2PReplicator(String collections, String addresses, DefraIdentity identity) throws DefraException {
        DefraResult result = AddP2PReplicatorNative(this.nodePtr, collections, addresses, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes a P2P replicator.
     *
     * @param collections collection selectors encoded as JSON
     * @param id replicator identifier
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteP2PReplicator(String collections, String id) throws DefraException {
        DefraResult result = DeleteP2PReplicatorNative(this.nodePtr, collections, id, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes a P2P replicator.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collections collection selectors encoded as JSON
     * @param id replicator identifier
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteP2PReplicator(String collections, String id, DefraIdentity identity) throws DefraException {
        DefraResult result = DeleteP2PReplicatorNative(this.nodePtr, collections, id, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds collections to the P2P collection set.
     *
     * @param collections collection selectors encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addP2PCollection(String collections) throws DefraException {
        DefraResult result = AddP2PCollectionNative(this.nodePtr, collections, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds collections to the P2P collection set.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collections collection selectors encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addP2PCollection(String collections, DefraIdentity identity) throws DefraException {
        DefraResult result = AddP2PCollectionNative(this.nodePtr, collections, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Removes collections from the P2P collection set.
     *
     * @param collections collection selectors encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteP2PCollection(String collections) throws DefraException {
        DefraResult result = DeleteP2PCollectionNative(this.nodePtr, collections, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Removes collections from the P2P collection set.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collections collection selectors encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteP2PCollection(String collections, DefraIdentity identity) throws DefraException {
        DefraResult result = DeleteP2PCollectionNative(this.nodePtr, collections, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the P2P collection set as JSON.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PCollections() throws DefraException {
        DefraResult result = ListP2PCollectionsNative(this.nodePtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the P2P collection set as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PCollections(DefraIdentity identity) throws DefraException {
        DefraResult result = ListP2PCollectionsNative(this.nodePtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds documents to the P2P document set.
     *
     * @param collections collection selectors encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addP2PDocument(String collections) throws DefraException {
        DefraResult result = AddP2PDocumentNative(this.nodePtr, collections, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds documents to the P2P document set.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collections collection selectors encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addP2PDocument(String collections, DefraIdentity identity) throws DefraException {
        DefraResult result = AddP2PDocumentNative(this.nodePtr, collections, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Removes documents from the P2P document set.
     *
     * @param collections collection selectors encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteP2PDocument(String collections) throws DefraException {
        DefraResult result = DeleteP2PDocumentNative(this.nodePtr, collections, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Removes documents from the P2P document set.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collections collection selectors encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteP2PDocument(String collections, DefraIdentity identity) throws DefraException {
        DefraResult result = DeleteP2PDocumentNative(this.nodePtr, collections, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the P2P document set as JSON.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PDocuments() throws DefraException {
        DefraResult result = ListP2PDocumentsNative(this.nodePtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the P2P document set as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PDocuments(DefraIdentity identity) throws DefraException {
        DefraResult result = ListP2PDocumentsNative(this.nodePtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Synchronizes documents from connected P2P peers.
     *
     * @param collection collection name or collection selector encoded as JSON
     * @param docIDs document identifiers encoded as JSON
     * @param timeout synchronization timeout accepted by DefraDB
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String syncP2PDocuments(String collection, String docIDs, String timeout) throws DefraException {
        DefraResult result = SyncP2PDocumentsNative(this.nodePtr, collection, docIDs, timeout, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Synchronizes documents from connected P2P peers.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collection collection name or collection selector encoded as JSON
     * @param docIDs document identifiers encoded as JSON
     * @param timeout synchronization timeout accepted by DefraDB
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String syncP2PDocuments(String collection, String docIDs, String timeout, DefraIdentity identity) throws DefraException {
        DefraResult result = SyncP2PDocumentsNative(this.nodePtr, collection, docIDs, timeout, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Synchronizes collection versions from connected P2P peers.
     *
     * @param versionIDs collection version identifiers encoded as JSON
     * @param timeout synchronization timeout accepted by DefraDB
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String syncP2PCollectionVersions(String versionIDs, String timeout) throws DefraException {
        DefraResult result = SyncP2PCollectionVersionsNative(this.nodePtr, versionIDs, timeout, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Synchronizes collection versions from connected P2P peers.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param versionIDs collection version identifiers encoded as JSON
     * @param timeout synchronization timeout accepted by DefraDB
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String syncP2PCollectionVersions(String versionIDs, String timeout, DefraIdentity identity) throws DefraException {
        DefraResult result = SyncP2PCollectionVersionsNative(this.nodePtr, versionIDs, timeout, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Synchronizes a branchable collection from connected P2P peers.
     *
     * @param collectionID collection identifier
     * @param timeout synchronization timeout accepted by DefraDB
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String syncP2PBranchableCollection(String collectionID, String timeout) throws DefraException {
        DefraResult result = SyncP2PBranchableCollectionNative(this.nodePtr, collectionID, timeout, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Synchronizes a branchable collection from connected P2P peers.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collectionID collection identifier
     * @param timeout synchronization timeout accepted by DefraDB
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String syncP2PBranchableCollection(String collectionID, String timeout, DefraIdentity identity) throws DefraException {
        DefraResult result = SyncP2PBranchableCollectionNative(this.nodePtr, collectionID, timeout, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Connects to P2P peer addresses.
     *
     * @param peerAddresses peer multiaddresses encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String connectP2PPeers(String peerAddresses) throws DefraException {
        DefraResult result = ConnectP2PPeersNative(this.nodePtr, peerAddresses, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Connects to P2P peer addresses.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param peerAddresses peer multiaddresses encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String connectP2PPeers(String peerAddresses, DefraIdentity identity) throws DefraException {
        DefraResult result = ConnectP2PPeersNative(this.nodePtr, peerAddresses, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Disconnects from P2P peer addresses.
     *
     * @param peerAddresses peer multiaddresses encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String disconnectP2PPeers(String peerAddresses) throws DefraException {
        DefraResult result = DisconnectP2PPeersNative(this.nodePtr, peerAddresses, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Disconnects from P2P peer addresses.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param peerAddresses peer multiaddresses encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String disconnectP2PPeers(String peerAddresses, DefraIdentity identity) throws DefraException {
        DefraResult result = DisconnectP2PPeersNative(this.nodePtr, peerAddresses, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Query Methods
    /**
     * Executes a GraphQL query and returns its result as JSON.
     *
     * @param query GraphQL query
     * @param operationName GraphQL operation name, or an empty string
     * @param variables GraphQL variables encoded as JSON, or an empty string
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String executeQuery(String query, String operationName, String variables) throws DefraException {
        DefraResult result = ExecuteQueryNative(this.nodePtr, query, 0, operationName, variables);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Executes a GraphQL query and returns its result as JSON.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param query GraphQL query
     * @param operationName GraphQL operation name, or an empty string
     * @param variables GraphQL variables encoded as JSON, or an empty string
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String executeQuery(String query, String operationName, String variables, DefraIdentity identity) throws DefraException {
        DefraResult result = ExecuteQueryNative(this.nodePtr, query, identity.getPointer(), operationName, variables);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Version Method
    /**
     * Returns DefraDB version information.
     *
     * @param flagFull whether to include full build information
     * @param flagJSON whether to return JSON instead of plain text
     * @return version information in the requested format
     * @throws DefraException if DefraDB rejects the operation
     */
    public String getVersion(boolean flagFull, boolean flagJSON) throws DefraException {
        DefraResult result = GetVersionNative(flagFull, flagJSON);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // View Methods
    /**
     * Creates a view from a query, SDL, and optional transform.
     *
     * @param query GraphQL query
     * @param sdl Schema Definition Language (SDL) collection definition
     * @param transformCID content identifier of the view transform, or an empty string
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addView(String query, String sdl, String transformCID) throws DefraException {
        DefraResult result = AddViewNative(this.nodePtr, query, sdl, transformCID, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Creates a view from a query, SDL, and optional transform.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param query GraphQL query
     * @param sdl Schema Definition Language (SDL) collection definition
     * @param transformCID content identifier of the view transform, or an empty string
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addView(String query, String sdl, String transformCID, DefraIdentity identity) throws DefraException {
        DefraResult result = AddViewNative(this.nodePtr, query, sdl, transformCID, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Refreshes the selected view.
     *
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String refreshView(DefraCollectionOptions options) throws DefraException {
        DefraResult result = RefreshViewNative(this.nodePtr, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Refreshes the selected view.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String refreshView(DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = RefreshViewNative(this.nodePtr, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Block Verification
    /**
     * Verifies the signature of a block.
     *
     * @param keyType key type supported by DefraDB
     * @param publicKey public key encoded as hexadecimal
     * @param cid content identifier of the block
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String verifyBlockSignature(String keyType, String publicKey, String cid) throws DefraException {
        DefraResult result = VerifyBlockSignatureNative(this.nodePtr, keyType, publicKey, cid, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Verifies the signature of a block.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param keyType key type supported by DefraDB
     * @param publicKey public key encoded as hexadecimal
     * @param cid content identifier of the block
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String verifyBlockSignature(String keyType, String publicKey, String cid, DefraIdentity identity) throws DefraException {
        DefraResult result = VerifyBlockSignatureNative(this.nodePtr, keyType, publicKey, cid, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Function creates a new transaction
    /**
     * Creates a database transaction.
     *
     * @param isReadOnly whether the transaction is read-only
     * @return the new transaction
     * @throws DefraException if DefraDB rejects the operation
     */
    public DefraTransaction transactionCreate(boolean isReadOnly) throws DefraException {
        DefraTransactionResult txnResult = TransactionCreateNative(this.nodePtr, isReadOnly);
        if (txnResult.status != 0) {
            throw new DefraException(txnResult.error);
        }
        return new DefraTransaction(txnResult.txnPtr);
    }

    // Function is a getter for the node pointer.
    /**
     * Returns the underlying native pointer.
     * @return the underlying native pointer
     */
    public long getPointer() {
        return this.nodePtr;
    }
	
	// Subscription callback functionality
	/**
	 * Subscribes to a GraphQL query and dispatches each result to a callback.
	 *
	 * @param query GraphQL query
	 * @param callback consumer invoked for each subscription result
	 * @return the subscription identifier
	 * @throws DefraException if DefraDB rejects the operation
	 */
	public String subscribe(String query, Consumer<String> callback) throws DefraException {
		if (!isSubscriptionQuery(query)) {
			throw new DefraException("Query is not a subscription. Use executeQuery for mutations and regular queries.");
		}
		DefraResult result = ExecuteQueryNative(this.nodePtr, query, 0, "", "");
		if (result.status == 1) {
			throw new DefraException(result.error);
		}
		if (result.status != 2) {
			throw new DefraException("Expected subscription response (status 2), got status: " + result.status);
		}
		subscriptions.put(result.value, new ActiveSubscription(result.value, callback));
		return result.value;
	}

	// Helper function for determining if the subscription query was not another type of query (i.e. mutation)
	private boolean isSubscriptionQuery(String query) {
		String stripped = query.trim().toLowerCase();
		// Handle optional leading comments
		for (String line : stripped.split("\n")) {
			String trimmed = line.trim();
			if (trimmed.startsWith("#")) continue;
			return trimmed.startsWith("subscription");
		}
		return false;
	}

	/**
	 * Cancels a query subscription.
	 *
	 * @param id replicator identifier
	 * @throws DefraException if DefraDB rejects the operation
	 */
	public void unsubscribe(String id) throws DefraException {
		ActiveSubscription sub = subscriptions.remove(id);
		if (sub != null) {
            DefraResult result = CloseSubscriptionNative(id);
            if (result.status != 0) {
                throw new DefraException(result.error);
            }
		}
	}
	
}