package nitionsearch.search;

import nitionsearch.model.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PageIndex {
    private final List<Page> pages;

    public PageIndex(){
        pages = new ArrayList<>();
    }

    public void addPage(Page page){
        pages.add(page);
    }

    public Set<String> getPageTerms(Page page, String[] searchTerms){
        return null;
    }

}
