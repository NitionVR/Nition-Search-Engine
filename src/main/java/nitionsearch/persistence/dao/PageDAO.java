package nitionsearch.persistence.dao;

import nitionsearch.model.Page;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PageDAO {
    Optional<Page> findByUrl(String url);
    Page save(Page page);
    Optional<Page> findById(UUID id);
    Collection<Page> searchByKeyword(String keyword);
}
