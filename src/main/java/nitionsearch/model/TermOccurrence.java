package nitionsearch.model;

import java.util.UUID;

public class TermOccurrence {
    private final UUID documentId;
    private final String term;
    private final int position;

    public TermOccurrence(UUID documentId, String term, int position) {
        this.documentId = documentId;
        this.term = term;
        this.position = position;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public String getTerm() {
        return term;
    }

    public int getPosition() {
        return position;
    }
}
