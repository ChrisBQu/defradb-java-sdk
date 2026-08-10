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

/**
 * An explicit DefraDB transaction.
 *
 * <p>Operations remain isolated until {@link #commit()} is called. Call
 * {@link #discard()} to abandon the transaction. Overloads accepting a
 * {@link DefraIdentity} execute as that identity.</p>
 */
public class DefraTransaction {

    static {
        NativeLoader.load("nativewrapper");
        NativeLoader.load("defradb");
    }

    // Core Transaction Methods
    private native DefraResult TransactionCommitNative(long txnPtr);
    private native void TransactionDiscardNative(long txnPtr);
    
    // ACP Methods
    private native DefraResult ACPAddDACPolicyNative(long txnPtr, long identityPtr, String policy);
    private native DefraResult ACPAddDACActorRelationshipNative(long txnPtr, long identityPtr, String collection, String docID, String relation, String actor);
    private native DefraResult ACPDeleteDACActorRelationshipNative(long txnPtr, long identityPtr, String collection, String docID, String relation, String actor);
    private native DefraResult ACPDisableNACNative(long txnPtr, long identityPtr);
    private native DefraResult ACPReEnableNACNative(long txnPtr, long identityPtr);
    private native DefraResult ACPAddNACActorRelationshipNative(long txnPtr, long identityPtr, String relation, String actor);
    private native DefraResult ACPDeleteNACActorRelationshipNative(long txnPtr, long identityPtr, String relation, String actor);
    private native DefraResult ACPGetNACStatusNative(long txnPtr, long identityPtr);
    
    // Collection Methods
    private native DefraResult AddCollectionNative(long txnPtr, String sdl, long identityPtr);
    private native DefraResult DescribeCollectionNative(long txnPtr, DefraCollectionOptions options, long identityPtr);
    private native DefraResult PatchCollectionNative(long txnPtr, String patch, String lensConfig, long identityPtr);
    private native DefraResult SetActiveCollectionNative(long txnPtr, DefraCollectionOptions options, long identityPtr);
    private native DefraResult TruncateCollectionNative(long txnPtr, DefraCollectionOptions options, long identityPtr);
    
    // Document Methods
    private native DefraResult AddDocumentNative(long txnPtr, String json, boolean isEncrypted, String encryptedFields, DefraCollectionOptions options, long identityPtr);
    private native DefraResult DeleteDocumentNative(long txnPtr, String docID, String filter, DefraCollectionOptions options, long identityPtr);
    private native DefraResult GetDocumentNative(long txnPtr, String docID, boolean showDeleted, DefraCollectionOptions options, long identityPtr);
    private native DefraResult UpdateDocumentNative(long txnPtr, String docID, String filter, String updater, DefraCollectionOptions options, long identityPtr);
    
    // Encrypted Index Methods
    private native DefraResult NewEncryptedIndexNative(long txnPtr, String collectionName, String fieldName, long identityPtr);
    private native DefraResult ListEncryptedIndexesNative(long txnPtr, String collectionName, long identityPtr);
    private native DefraResult DeleteEncryptedIndexNative(long txnPtr, String collectionName, String fieldName, long identityPtr);
    
    // Index Methods
    private native DefraResult NewIndexNative(long txnPtr, String indexName, String fields, boolean isUnique, DefraCollectionOptions options, long identityPtr);
    private native DefraResult ListIndexesNative(long txnPtr, DefraCollectionOptions options, long identityPtr);
    private native DefraResult DeleteIndexNative(long txnPtr, String indexName, DefraCollectionOptions options, long identityPtr);
    
    // Identity Methods
    private native DefraResult GetNodeIdentityNative(long txnPtr);
    
    // Lens Methods
    private native DefraResult SetLensNative(long txnPtr, long identityPtr, String src, String dst, String cfg);
    private native DefraResult AddLensNative(long txnPtr, long identityPtr, String cfg);
    private native DefraResult ListLensesNative(long txnPtr, long identityPtr);
    
    // P2P Methods
    private native DefraResult GetP2PInfoNative(long txnPtr, long identityPtr);
    private native DefraResult ListP2PActivePeersNative(long txnPtr, long identityPtr);
    private native DefraResult ListP2PReplicatorsNative(long txnPtr, long identityPtr);
    private native DefraResult AddP2PReplicatorNative(long txnPtr, String collections, String addresses, long identityPtr);
    private native DefraResult DeleteP2PReplicatorNative(long txnPtr, String collections, String id, long identityPtr);
    private native DefraResult AddP2PCollectionNative(long txnPtr, String collections, long identityPtr);
    private native DefraResult DeleteP2PCollectionNative(long txnPtr, String collections, long identityPtr);
    private native DefraResult ListP2PCollectionsNative(long txnPtr, long identityPtr);
    private native DefraResult AddP2PDocumentNative(long txnPtr, String collections, long identityPtr);
    private native DefraResult DeleteP2PDocumentNative(long txnPtr, String collections, long identityPtr);
    private native DefraResult ListP2PDocumentsNative(long txnPtr, long identityPtr);
    private native DefraResult SyncP2PDocumentsNative(long txnPtr, String collection, String docIDs, String timeout, long identityPtr);
    private native DefraResult SyncP2PCollectionVersionsNative(long txnPtr, String versionIDs, String timeout, long identityPtr);
    private native DefraResult SyncP2PBranchableCollectionNative(long txnPtr, String collectionID, String timeout, long identityPtr);
    private native DefraResult ConnectP2PPeersNative(long txnPtr, String peerAddresses, long identityPtr);
    private native DefraResult DisconnectP2PPeersNative(long txnPtr, String peerAddresses, long identityPtr);

    // Query Methods
    private native DefraResult ExecuteQueryNative(long txnPtr, String query, long identityPtr, String operationName, String variables);
    
    // View Methods
    private native DefraResult AddViewNative(long txnPtr, String query, String sdl, String transformCID, long identityPtr);
    private native DefraResult RefreshViewNative(long txnPtr, DefraCollectionOptions options, long identityPtr);
    
    // Block Verification
    private native DefraResult VerifyBlockSignatureNative(long txnPtr, String keyType, String publicKey, String cid, long identityPtr);
    
    private long txnPtr;

    /**
     * Wraps a native transaction pointer.
     *
     * @param ptr native transaction pointer
     */
    public DefraTransaction(long ptr) {
        this.txnPtr = ptr;
    }

    /**
     * Commits the transaction.
     *
     * @return the native commit result encoded as JSON
     * @throws DefraException if DefraDB rejects the operation
     */
    public String commit() throws DefraException {
        DefraResult result = TransactionCommitNative(this.txnPtr);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Discards the transaction and releases its native resources.
     */
    public void discard() {
        TransactionDiscardNative(this.txnPtr);
    }

    // ACP Methods
    /**
     * Adds a document access-control policy within this transaction.
     *
     * @param policy policy definition encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPAddDACPolicy(String policy) throws DefraException {
        DefraResult result = ACPAddDACPolicyNative(this.txnPtr, 0, policy);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds a document access-control policy within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param policy policy definition encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPAddDACPolicy(String policy, DefraIdentity identity) throws DefraException {
        DefraResult result = ACPAddDACPolicyNative(this.txnPtr, identity.getPointer(), policy);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds an actor relationship to a document access-control policy within this transaction.
     *
     * @param collection collection name or collection selector encoded as JSON
     * @param docID document identifier
     * @param relation relationship name
     * @param actor actor DID
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPAddDACActorRelationship(String collection, String docID, String relation, String actor) throws DefraException {
        DefraResult result = ACPAddDACActorRelationshipNative(this.txnPtr, 0, collection, docID, relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds an actor relationship to a document access-control policy within this transaction.
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
        DefraResult result = ACPAddDACActorRelationshipNative(this.txnPtr, identity.getPointer(), collection, docID, relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }   

    /**
     * Deletes an actor relationship from a document access-control policy within this transaction.
     *
     * @param collection collection name or collection selector encoded as JSON
     * @param docID document identifier
     * @param relation relationship name
     * @param actor actor DID
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPDeleteDACActorRelationship(String collection, String docID, String relation, String actor) throws DefraException {
        DefraResult result = ACPDeleteDACActorRelationshipNative(this.txnPtr, 0, collection, docID, relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an actor relationship from a document access-control policy within this transaction.
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
        DefraResult result = ACPDeleteDACActorRelationshipNative(this.txnPtr, identity.getPointer(), collection, docID, relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Disables node access control within this transaction.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPDisableNAC() throws DefraException {
        DefraResult result = ACPDisableNACNative(this.txnPtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Disables node access control within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPDisableNAC(DefraIdentity identity) throws DefraException {
        DefraResult result = ACPDisableNACNative(this.txnPtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Re-enables node access control within this transaction.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPReEnableNAC() throws DefraException {
        DefraResult result = ACPReEnableNACNative(this.txnPtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Re-enables node access control within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPReEnableNAC(DefraIdentity identity) throws DefraException {
        DefraResult result = ACPReEnableNACNative(this.txnPtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds an actor relationship to the node access-control policy within this transaction.
     *
     * @param relation relationship name
     * @param actor actor DID
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPAddNACActorRelationship(String relation, String actor) throws DefraException {
        DefraResult result = ACPAddNACActorRelationshipNative(this.txnPtr, 0, relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds an actor relationship to the node access-control policy within this transaction.
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
        DefraResult result = ACPAddNACActorRelationshipNative(this.txnPtr, identity.getPointer(), relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an actor relationship from the node access-control policy within this transaction.
     *
     * @param relation relationship name
     * @param actor actor DID
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPDeleteNACActorRelationship(String relation, String actor) throws DefraException {
        DefraResult result = ACPDeleteNACActorRelationshipNative(this.txnPtr, 0, relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an actor relationship from the node access-control policy within this transaction.
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
        DefraResult result = ACPDeleteNACActorRelationshipNative(this.txnPtr, identity.getPointer(), relation, actor);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the node access-control status as JSON within this transaction.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPGetNACStatus() throws DefraException {
        DefraResult result = ACPGetNACStatusNative(this.txnPtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the node access-control status as JSON within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String ACPGetNACStatus(DefraIdentity identity) throws DefraException {
        DefraResult result = ACPGetNACStatusNative(this.txnPtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Collection Methods
    /**
     * Adds collection definitions from Schema Definition Language (SDL) within this transaction.
     *
     * @param sdl Schema Definition Language (SDL) collection definition
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addCollection(String sdl) throws DefraException {
        DefraResult result = AddCollectionNative(this.txnPtr, sdl, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds collection definitions from Schema Definition Language (SDL) within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param sdl Schema Definition Language (SDL) collection definition
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addCollection(String sdl, DefraIdentity identity) throws DefraException {
        DefraResult result = AddCollectionNative(this.txnPtr, sdl, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns matching collection descriptions as JSON within this transaction.
     *
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String describeCollection(DefraCollectionOptions options) throws DefraException {
        DefraResult result = DescribeCollectionNative(this.txnPtr, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns matching collection descriptions as JSON within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String describeCollection(DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = DescribeCollectionNative(this.txnPtr, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Applies a schema patch and optional migration lens configuration within this transaction.
     *
     * @param patch schema patch encoded as JSON
     * @param lensConfig migration lens configuration, or an empty string
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String patchCollection(String patch, String lensConfig) throws DefraException {
        DefraResult result = PatchCollectionNative(this.txnPtr, patch, lensConfig, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Applies a schema patch and optional migration lens configuration within this transaction.
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
        DefraResult result = PatchCollectionNative(this.txnPtr, patch, lensConfig, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Makes the selected collection version active within this transaction.
     *
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String setActiveCollection(DefraCollectionOptions options) throws DefraException {
        DefraResult result = SetActiveCollectionNative(this.txnPtr, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Makes the selected collection version active within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String setActiveCollection(DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = SetActiveCollectionNative(this.txnPtr, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes every document in the selected collection within this transaction.
     *
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String truncateCollection(DefraCollectionOptions options) throws DefraException {
        DefraResult result = TruncateCollectionNative(this.txnPtr, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes every document in the selected collection within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String truncateCollection(DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = TruncateCollectionNative(this.txnPtr, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

        // Helper function for getCollectionByName functions
    private DefraCollection getCollectionByNameWithIdentityPointer(String name, long identityPtr) throws DefraException {
		DefraCollectionOptions copts = new DefraCollectionOptions();
		copts.name = name;
		DefraResult describeResult = DescribeCollectionNative(this.txnPtr, copts, identityPtr);
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
		return new DefraCollection(this.txnPtr, name, collectionID, versionID);
    }
	
	/**
	 * Returns a handle for the active collection with the given name within this transaction.
	 *
	 * @param name collection name
	 * @return a handle to the active collection
	 * @throws DefraException if DefraDB rejects the operation
	 */
	public DefraCollection getCollectionByName(String name) throws DefraException {
		return getCollectionByNameWithIdentityPointer(name, 0);		
	}
	
	/**
	 * Returns a handle for the active collection with the given name within this transaction.
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

    // Document Methods
    /**
     * Adds a JSON document to the selected collection within this transaction.
     *
     * @param json document encoded as JSON
     * @param isEncrypted whether to encrypt the document
     * @param encryptedFields comma-separated field names to encrypt
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addDocument(String json, boolean isEncrypted, String encryptedFields, DefraCollectionOptions options) throws DefraException {
        DefraResult result = AddDocumentNative(this.txnPtr, json, isEncrypted, encryptedFields, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds a JSON document to the selected collection within this transaction.
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
        DefraResult result = AddDocumentNative(this.txnPtr, json, isEncrypted, encryptedFields, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes the document matching an ID and optional filter within this transaction.
     *
     * @param docID document identifier
     * @param filter JSON filter that must match, or an empty string
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteDocument(String docID, String filter, DefraCollectionOptions options) throws DefraException {
        DefraResult result = DeleteDocumentNative(this.txnPtr, docID, filter, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes the document matching an ID and optional filter within this transaction.
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
        DefraResult result = DeleteDocumentNative(this.txnPtr, docID, filter, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns a document by ID as JSON within this transaction.
     *
     * @param docID document identifier
     * @param showDeleted whether a deleted document may be returned
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String getDocument(String docID, boolean showDeleted, DefraCollectionOptions options) throws DefraException {
        DefraResult result = GetDocumentNative(this.txnPtr, docID, showDeleted, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns a document by ID as JSON within this transaction.
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
        DefraResult result = GetDocumentNative(this.txnPtr, docID, showDeleted, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Updates the document matching an ID and optional filter within this transaction.
     *
     * @param docID document identifier
     * @param filter JSON filter that must match, or an empty string
     * @param updater JSON update expression
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String updateDocument(String docID, String filter, String updater, DefraCollectionOptions options) throws DefraException {
        DefraResult result = UpdateDocumentNative(this.txnPtr, docID, filter, updater, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Updates the document matching an ID and optional filter within this transaction.
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
    public String updateDocument(String docID, String filter, String updater, DefraCollectionOptions options, DefraIdentity identity) throws DefraException{
        DefraResult result = UpdateDocumentNative(this.txnPtr, docID, filter, updater, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Encrypted Index Methods
    /**
     * Creates an encrypted index on a collection field within this transaction.
     *
     * @param collectionName collection name
     * @param fieldName field name
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String newEncryptedIndex(String collectionName, String fieldName) throws DefraException {
        DefraResult result = NewEncryptedIndexNative(this.txnPtr, collectionName, fieldName, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Creates an encrypted index on a collection field within this transaction.
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
        DefraResult result = NewEncryptedIndexNative(this.txnPtr, collectionName, fieldName, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns encrypted-index descriptions as JSON within this transaction.
     *
     * @param collectionName collection name
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listEncryptedIndexes(String collectionName) throws DefraException {
        DefraResult result = ListEncryptedIndexesNative(this.txnPtr, collectionName, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns encrypted-index descriptions as JSON within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collectionName collection name
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listEncryptedIndexes(String collectionName, DefraIdentity identity) throws DefraException {
        DefraResult result = ListEncryptedIndexesNative(this.txnPtr, collectionName, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an encrypted index from a collection field within this transaction.
     *
     * @param collectionName collection name
     * @param fieldName field name
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteEncryptedIndex(String collectionName, String fieldName) throws DefraException {
        DefraResult result = DeleteEncryptedIndexNative(this.txnPtr, collectionName, fieldName, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an encrypted index from a collection field within this transaction.
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
        DefraResult result = DeleteEncryptedIndexNative(this.txnPtr, collectionName, fieldName, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Index Methods
    /**
     * Creates an index on the selected collection within this transaction.
     *
     * @param indexName index name
     * @param fields comma-separated fields with optional ASC or DESC order
     * @param isUnique whether indexed values must be unique
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String newIndex(String indexName, String fields, boolean isUnique, DefraCollectionOptions options) throws DefraException {
        DefraResult result = NewIndexNative(this.txnPtr, indexName, fields, isUnique, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Creates an index on the selected collection within this transaction.
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
        DefraResult result = NewIndexNative(this.txnPtr, indexName, fields, isUnique, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns index descriptions for the selected collection as JSON within this transaction.
     *
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listIndexes(DefraCollectionOptions options) throws DefraException {
        DefraResult result = ListIndexesNative(this.txnPtr, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns index descriptions for the selected collection as JSON within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listIndexes(DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = ListIndexesNative(this.txnPtr, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an index from the selected collection within this transaction.
     *
     * @param indexName index name
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteIndex(String indexName, DefraCollectionOptions options) throws DefraException {
        DefraResult result = DeleteIndexNative(this.txnPtr, indexName, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes an index from the selected collection within this transaction.
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
        DefraResult result = DeleteIndexNative(this.txnPtr, indexName, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Identity Methods
    /**
     * Returns the public identity of the node as JSON within this transaction.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String getNodeIdentity() throws DefraException {
        DefraResult result = GetNodeIdentityNative(this.txnPtr);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Lens Methods
    /**
     * Sets a migration lens between collection versions within this transaction.
     *
     * @param src source collection version identifier
     * @param dst destination collection version identifier
     * @param cfg lens configuration encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String setLens(String src, String dst, String cfg) throws DefraException {
        DefraResult result = SetLensNative(this.txnPtr, 0, src, dst, cfg);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Sets a migration lens between collection versions within this transaction.
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
        DefraResult result = SetLensNative(this.txnPtr, identity.getPointer(), src, dst, cfg);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds a lens from its configuration within this transaction.
     *
     * @param cfg lens configuration encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addLens(String cfg) throws DefraException {
        DefraResult result = AddLensNative(this.txnPtr, 0, cfg);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds a lens from its configuration within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param cfg lens configuration encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addLens(String cfg, DefraIdentity identity) throws DefraException {
        DefraResult result = AddLensNative(this.txnPtr, identity.getPointer(), cfg);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns registered lens descriptions as JSON within this transaction.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listLenses() throws DefraException {
        DefraResult result = ListLensesNative(this.txnPtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns registered lens descriptions as JSON within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listLenses(DefraIdentity identity) throws DefraException {
        DefraResult result = ListLensesNative(this.txnPtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // P2P Methods
    /**
     * Returns information about the P2P node as JSON within this transaction.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String getP2PInfo() throws DefraException {
        DefraResult result = GetP2PInfoNative(this.txnPtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns information about the P2P node as JSON within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String getP2PInfo(DefraIdentity identity) throws DefraException {
        DefraResult result = GetP2PInfoNative(this.txnPtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns active P2P peers as JSON within this transaction.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PActivePeers() throws DefraException {
        DefraResult result = ListP2PActivePeersNative(this.txnPtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns active P2P peers as JSON within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PActivePeers(DefraIdentity identity) throws DefraException {
        DefraResult result = ListP2PActivePeersNative(this.txnPtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns configured P2P replicators as JSON within this transaction.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PReplicators() throws DefraException {
        DefraResult result = ListP2PReplicatorsNative(this.txnPtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns configured P2P replicators as JSON within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PReplicators(DefraIdentity identity) throws DefraException {
        DefraResult result = ListP2PReplicatorsNative(this.txnPtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds a P2P replicator for collections and peer addresses within this transaction.
     *
     * @param collections collection selectors encoded as JSON
     * @param addresses peer addresses encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addP2PReplicator(String collections, String addresses) throws DefraException {
        DefraResult result = AddP2PReplicatorNative(this.txnPtr, collections, addresses, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds a P2P replicator for collections and peer addresses within this transaction.
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
        DefraResult result = AddP2PReplicatorNative(this.txnPtr, collections, addresses, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes a P2P replicator within this transaction.
     *
     * @param collections collection selectors encoded as JSON
     * @param id replicator identifier
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteP2PReplicator(String collections, String id) throws DefraException {
        DefraResult result = DeleteP2PReplicatorNative(this.txnPtr, collections, id, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Deletes a P2P replicator within this transaction.
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
        DefraResult result = DeleteP2PReplicatorNative(this.txnPtr, collections, id, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds collections to the P2P collection set within this transaction.
     *
     * @param collections collection selectors encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addP2PCollection(String collections) throws DefraException {
        DefraResult result = AddP2PCollectionNative(this.txnPtr, collections, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds collections to the P2P collection set within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collections collection selectors encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addP2PCollection(String collections, DefraIdentity identity) throws DefraException {
        DefraResult result = AddP2PCollectionNative(this.txnPtr, collections, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Removes collections from the P2P collection set within this transaction.
     *
     * @param collections collection selectors encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteP2PCollection(String collections) throws DefraException {
        DefraResult result = DeleteP2PCollectionNative(this.txnPtr, collections, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Removes collections from the P2P collection set within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collections collection selectors encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteP2PCollection(String collections, DefraIdentity identity) throws DefraException {
        DefraResult result = DeleteP2PCollectionNative(this.txnPtr, collections, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the P2P collection set as JSON within this transaction.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PCollections() throws DefraException {
        DefraResult result = ListP2PCollectionsNative(this.txnPtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the P2P collection set as JSON within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PCollections(DefraIdentity identity) throws DefraException {
        DefraResult result = ListP2PCollectionsNative(this.txnPtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds documents to the P2P document set within this transaction.
     *
     * @param collections collection selectors encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addP2PDocument(String collections) throws DefraException {
        DefraResult result = AddP2PDocumentNative(this.txnPtr, collections, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Adds documents to the P2P document set within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collections collection selectors encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addP2PDocument(String collections, DefraIdentity identity) throws DefraException {
        DefraResult result = AddP2PDocumentNative(this.txnPtr, collections, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Removes documents from the P2P document set within this transaction.
     *
     * @param collections collection selectors encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteP2PDocument(String collections) throws DefraException {
        DefraResult result = DeleteP2PDocumentNative(this.txnPtr, collections, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Removes documents from the P2P document set within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param collections collection selectors encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String deleteP2PDocument(String collections, DefraIdentity identity) throws DefraException {
        DefraResult result = DeleteP2PDocumentNative(this.txnPtr, collections, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the P2P document set as JSON within this transaction.
     *
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PDocuments() throws DefraException {
        DefraResult result = ListP2PDocumentsNative(this.txnPtr, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Returns the P2P document set as JSON within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String listP2PDocuments(DefraIdentity identity) throws DefraException {
        DefraResult result = ListP2PDocumentsNative(this.txnPtr, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Synchronizes documents from connected P2P peers within this transaction.
     *
     * @param collection collection name or collection selector encoded as JSON
     * @param docIDs document identifiers encoded as JSON
     * @param timeout synchronization timeout accepted by DefraDB
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String syncP2PDocuments(String collection, String docIDs, String timeout) throws DefraException {
        DefraResult result = SyncP2PDocumentsNative(this.txnPtr, collection, docIDs, timeout, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Synchronizes documents from connected P2P peers within this transaction.
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
        DefraResult result = SyncP2PDocumentsNative(this.txnPtr, collection, docIDs, timeout, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Synchronizes collection versions from connected P2P peers within this transaction.
     *
     * @param versionIDs collection version identifiers encoded as JSON
     * @param timeout synchronization timeout accepted by DefraDB
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String syncP2PCollectionVersions(String versionIDs, String timeout) throws DefraException {
        DefraResult result = SyncP2PCollectionVersionsNative(this.txnPtr, versionIDs, timeout, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Synchronizes collection versions from connected P2P peers within this transaction.
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
        DefraResult result = SyncP2PCollectionVersionsNative(this.txnPtr, versionIDs, timeout, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Synchronizes a branchable collection from connected P2P peers within this transaction.
     *
     * @param collectionID collection identifier
     * @param timeout synchronization timeout accepted by DefraDB
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String syncP2PBranchableCollection(String collectionID, String timeout) throws DefraException {
        DefraResult result = SyncP2PBranchableCollectionNative(this.txnPtr, collectionID, timeout, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Synchronizes a branchable collection from connected P2P peers within this transaction.
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
        DefraResult result = SyncP2PBranchableCollectionNative(this.txnPtr, collectionID, timeout, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Connects to P2P peer addresses within this transaction.
     *
     * @param peerAddresses peer multiaddresses encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String connectP2PPeers(String peerAddresses) throws DefraException {
        DefraResult result = ConnectP2PPeersNative(this.txnPtr, peerAddresses, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Connects to P2P peer addresses within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param peerAddresses peer multiaddresses encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String connectP2PPeers(String peerAddresses, DefraIdentity identity) throws DefraException {
        DefraResult result = ConnectP2PPeersNative(this.txnPtr, peerAddresses, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Disconnects from P2P peer addresses within this transaction.
     *
     * @param peerAddresses peer multiaddresses encoded as JSON
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String disconnectP2PPeers(String peerAddresses) throws DefraException {
        DefraResult result = DisconnectP2PPeersNative(this.txnPtr, peerAddresses, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Disconnects from P2P peer addresses within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param peerAddresses peer multiaddresses encoded as JSON
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String disconnectP2PPeers(String peerAddresses, DefraIdentity identity) throws DefraException {
        DefraResult result = DisconnectP2PPeersNative(this.txnPtr, peerAddresses, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Query Methods
    /**
     * Executes a GraphQL query and returns its result as JSON within this transaction.
     *
     * @param query GraphQL query
     * @param operationName GraphQL operation name, or an empty string
     * @param variables GraphQL variables encoded as JSON, or an empty string
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String executeQuery(String query, String operationName, String variables) throws DefraException {
        DefraResult result = ExecuteQueryNative(this.txnPtr, query, 0, operationName, variables);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Executes a GraphQL query and returns its result as JSON within this transaction.
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
        DefraResult result = ExecuteQueryNative(this.txnPtr, query, identity.getPointer(), operationName, variables);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // View Methods
    /**
     * Creates a view from a query, SDL, and optional transform within this transaction.
     *
     * @param query GraphQL query
     * @param sdl Schema Definition Language (SDL) collection definition
     * @param transformCID content identifier of the view transform, or an empty string
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String addView(String query, String sdl, String transformCID) throws DefraException {
        DefraResult result = AddViewNative(this.txnPtr, query, sdl, transformCID, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Creates a view from a query, SDL, and optional transform within this transaction.
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
        DefraResult result = AddViewNative(this.txnPtr, query, sdl, transformCID, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Refreshes the selected view within this transaction.
     *
     * @param options collection selector and operation options
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String refreshView(DefraCollectionOptions options) throws DefraException {
        DefraResult result = RefreshViewNative(this.txnPtr, options, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Refreshes the selected view within this transaction.
     *
     * <p>If an identity parameter is present, it authorizes this operation.</p>
     *
     * @param options collection selector and operation options
     * @param identity identity authorizing the operation
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String refreshView(DefraCollectionOptions options, DefraIdentity identity) throws DefraException {
        DefraResult result = RefreshViewNative(this.txnPtr, options, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Block Verification
    /**
     * Verifies the signature of a block within this transaction.
     *
     * @param keyType key type supported by DefraDB
     * @param publicKey public key encoded as hexadecimal
     * @param cid content identifier of the block
     * @return the native operation result
     * @throws DefraException if DefraDB rejects the operation
     */
    public String verifyBlockSignature(String keyType, String publicKey, String cid) throws DefraException {
        DefraResult result = VerifyBlockSignatureNative(this.txnPtr, keyType, publicKey, cid, 0);
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    /**
     * Verifies the signature of a block within this transaction.
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
        DefraResult result = VerifyBlockSignatureNative(this.txnPtr, keyType, publicKey, cid, identity.getPointer());
        if (result.status != 0) {
            throw new DefraException(result.error);
        }
        return result.value;
    }

    // Function is a getter for the transaction pointer
    /**
     * Returns the underlying native pointer.
     * @return the underlying native pointer
     */
    public long getPointer() {
        return this.txnPtr;
    }
}