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

import de.leycm.orm.config.OrmSqlConfig;
import de.leycm.orm.repository.SqlRepository;
import de.leycm.orm.repository.Repository;

import java.util.HashMap;

/**
 * <p>
 * Core implementation of the {@link OrmApi}.
 * This class registers itself with the {@link OrmApiProvider} upon instantiation.
 * </p>
 */
public class OrmBootstrap implements OrmApi {

    HashMap<Class<?>, Repository<?>> repositories = new HashMap<>();
    OrmSqlConfig config = new OrmSqlConfig("yourhost", 3360, "yourdb", "user", "password", "mysql");

    /**
     * Registers this instance with the {@link OrmApiProvider}.
     */
    public OrmBootstrap() {
        OrmApiProvider.register(this);
    }

    /**
     * Implementation of the example function from the API.
     * @return the Repository for the specified entity class
     */
    @Override
    @SuppressWarnings("unchecked")
    public <E> Repository<E> repository(Class<E> clazz) {
        return (Repository<E>) repositories.computeIfAbsent(clazz,
                k -> new SqlRepository<>(clazz, config));
    }

    /**
     * Configuration logic to set up the ORM with the provided SQL configuration.
     * @param config the SQL configuration
     */
    @Override
    public void configure(OrmSqlConfig config) {
        this.config = config;
    }

    /**
     * Optional shutdown logic to unregister the API implementation.
     */
    public void shutdown() {
        OrmApiProvider.unregister();
    }

}
