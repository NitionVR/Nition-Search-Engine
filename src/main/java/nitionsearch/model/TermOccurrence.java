package nitionsearch.model;

public class TermOccurrence {
    private final int documentId;
    private final String term;
    private final int position;

    public TermOccurrence(int documentId, String term, int position) {
        this.documentId = documentId;
        this.term = term;
        this.position = position;
    }

    public int getDocumentId() {
        return documentId;
    }

    public String getTerm() {
        return term;
    }

    public int getPosition() {
        return position;
    }
}
