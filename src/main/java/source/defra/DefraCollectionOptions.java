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
