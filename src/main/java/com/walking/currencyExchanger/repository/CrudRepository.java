package com.walking.currencyExchanger.repository;

import java.util.List;
import java.util.Optional;

public interface CrudRepository<K, T> {

    Optional<T> findById(K id);

    List<T> findAll();

    T save(T entity);

    T update(T entity);

    void deleteById(K id);
}
