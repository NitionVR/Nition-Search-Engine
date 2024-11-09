package nitionsearch.model;

import java.util.Objects;
import java.util.UUID;

public class Page {
    private final UUID id;
    private final String content;
    private final String url;

    public Page(UUID id, String url, String content ){
        this.id = id;
        this.url = Objects.requireNonNull(url,"URL cannot be null");
        this.content = content;
    }

    public Page(String url, String content ){
        this(UUID.randomUUID(),url,content);
    }

    public UUID getId(){
        return id;
    }

    public String getUrl(){
        return url;
    }

    public String getContent(){
        return content;
    }


    @Override
    public String toString(){
        return "Page{"+
                "id=" + id +
                ", content='" +
                content + '\'' + '}';
    }

    @Override
    public boolean equals(Object o){
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Page page = (Page) o;
        return id == page.id && content.equals(page.content)
                && url.equals(page.getUrl());
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, content);
    }
}
