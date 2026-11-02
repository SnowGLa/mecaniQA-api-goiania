package br.com.mecaniQA.api.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    T save(T entidade);

    Optional<T> findById(ID id);

    List<T> findAll();

    T update(T entidade);

    boolean deleteById(ID id);
}
