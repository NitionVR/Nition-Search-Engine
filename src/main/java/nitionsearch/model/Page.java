package nitionsearch.model;

import java.util.Objects;

public class Page {
    private final int id;
    private final String content;

    public Page(int id, String content){
        this.id = id;
        this.content = content;
    }

    public int getId(){
        return id;
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
        return id == page.id && content.equals(page.content);
    }

    @Override
    public int hashCode(){
        return Objects.hash(id, content);
    }
}
