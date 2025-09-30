/**
 * LECP-LICENSE NOTICE
 * <br><br>
 * This Sourcecode is under the LECP-LICENSE. <br>
 * License at: <a href="https://github.com/leycm/leycm/blob/main/LICENSE">GITHUB</a>
 * <br><br>
 * Copyright (c) LeyCM <leycm@proton.me> <br>
 * Copyright (c) maintainers <br>
 * Copyright (c) contributors
 */
package de.leycm.orm;

import de.leycm.orm.repository.Repository;

/**
 * <p>
 * Central access class for the ORM API.
 * Provides static methods that delegate calls to the registered API implementation.
 * This class cannot be instantiated.
 * </p>
 */
public final class ObjectRelationalMapping {

    private ObjectRelationalMapping() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    /**
     * Delegates to the registered ORM API implementation.
     *
     * @param clazz the entity class
     * @param <E>   the entity type
     * @return a repository for the specified entity class
     * @throws IllegalStateException if no API implementation has been registered
     */
    public static <E> Repository<E> repository(Class<E> clazz) {
        return OrmApiProvider.get().repository(clazz);
    }

}
