package de.leycm.orm.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface Repository<E> {

    E save(E entity);

    List<E> saveAll(Iterable<E> entities);

    void delete(E entity);

    void deleteById(E entity);

    Optional<E> findById(Object id);

    List<E> findAll();

    List<E> where(Predicate<E> predicate);

    List<E> where(String fieldName, String operator, Object value);

}
