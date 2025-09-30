package de.leycm.orm.repository;

import de.leycm.orm.annotation.Column;
import de.leycm.orm.annotation.Entity;
import de.leycm.orm.annotation.Identifier;
import de.leycm.orm.config.OrmSqlConfig;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SqlRepository<E> implements Repository<E> {

    private final Class<E> entityClass;
    private final OrmSqlConfig sqlConfig;
    private final String tableName;
    private Field idField;

    public SqlRepository(@NotNull Class<E> entityClass, OrmSqlConfig sqlConfig) {
        this.entityClass = entityClass;
        this.sqlConfig = sqlConfig;

        Entity entityAnno = entityClass.getAnnotation(Entity.class);
        if (entityAnno == null) {
            throw new IllegalArgumentException("Class " + entityClass.getName() + " is not annotated with @Entity");
        }
        this.tableName = entityAnno.table().isEmpty() ? entityClass.getSimpleName().toLowerCase() : entityAnno.table();

        for (Field f : entityClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(Identifier.class)) {
                f.setAccessible(true);
                this.idField = f;
                break;
            }
        }

        if (idField == null) {
            throw new IllegalArgumentException("Entity " + entityClass.getName() + " has no @Id field");
        }
    }

    @Override
    public E save(E entity) {
        try (Connection conn = sqlConfig.getDataSource().getConnection()) {
            List<String> columns = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            for (Field f : entityClass.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.isAnnotationPresent(Identifier.class)) {
                    Identifier idAnno = f.getAnnotation(Identifier.class);
                    if (idAnno.autoGen()) continue; // Auto-gen, skip insert
                }
                if (f.isAnnotationPresent(Column.class) || !f.isAnnotationPresent(Identifier.class)) {
                    Column colAnno = f.getAnnotation(Column.class);
                    String colName = (colAnno != null && !colAnno.name().isEmpty()) ? colAnno.name() : f.getName();
                    columns.add(colName);
                    values.add(f.get(entity));
                }
            }

            StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName)
                    .append(" (").append(String.join(", ", columns)).append(") VALUES (");
            sql.append("?,".repeat(columns.size()));
            sql.setLength(sql.length() - 1);
            sql.append(")");

            PreparedStatement stmt = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.executeUpdate();

            if (idField.isAnnotationPresent(Identifier.class) && idField.getAnnotation(Identifier.class).autoGen()) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    idField.set(entity, keys.getObject(1));
                }
            }

        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return entity;
    }

    @Override
    public void delete(E entity) {
        try (Connection conn = sqlConfig.getDataSource().getConnection()) {
            Object id = idField.get(entity);
            String sql = "DELETE FROM " + tableName + " WHERE " + idField.getName() + " = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setObject(1, id);
            stmt.executeUpdate();
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<E> findById(Object id) {
        try (Connection conn = sqlConfig.getDataSource().getConnection()) {
            String sql = "SELECT * FROM " + tableName + " WHERE " + idField.getName() + " = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                E entity = entityClass.getDeclaredConstructor().newInstance();
                for (Field f : entityClass.getDeclaredFields()) {
                    f.setAccessible(true);
                    String colName = f.isAnnotationPresent(Column.class) && !f.getAnnotation(Column.class).name().isEmpty()
                            ? f.getAnnotation(Column.class).name() : f.getName();
                    f.set(entity, rs.getObject(colName));
                }
                return Optional.of(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<E> findAll() {
        List<E> result = new ArrayList<>();
        try (Connection conn = sqlConfig.getDataSource().getConnection()) {
            String sql = "SELECT * FROM " + tableName;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                E entity = entityClass.getDeclaredConstructor().newInstance();
                for (Field f : entityClass.getDeclaredFields()) {
                    f.setAccessible(true);
                    String colName = f.isAnnotationPresent(Column.class) && !f.getAnnotation(Column.class).name().isEmpty()
                            ? f.getAnnotation(Column.class).name() : f.getName();
                    f.set(entity, rs.getObject(colName));
                }
                result.add(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public List<E> saveAll(@NotNull Iterable<E> entities) {
        List<E> saved = new ArrayList<>();
        for (E e : entities) {
            saved.add(save(e));
        }
        return saved;
    }

    @Override
    public void deleteById(E entity) {
        try {
            Object id = idField.get(entity);
            delete(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<E> where(Predicate<E> predicate) {
        List<E> all = findAll();
        List<E> filtered = new ArrayList<>();
        for (E e : all) {
            if (predicate.test(e)) filtered.add(e);
        }
        return filtered;
    }

    @Override
    public List<E> where(String fieldName, String operator, Object value) {
        List<E> result = new ArrayList<>();
        try (Connection conn = sqlConfig.getDataSource().getConnection()) {
            String sql = "SELECT * FROM " + tableName + " WHERE " + fieldName + " " + operator + " ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setObject(1, value);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                E entity = entityClass.getDeclaredConstructor().newInstance();
                for (Field f : entityClass.getDeclaredFields()) {
                    f.setAccessible(true);
                    String colName = f.isAnnotationPresent(Column.class) && !f.getAnnotation(Column.class).name().isEmpty()
                            ? f.getAnnotation(Column.class).name() : f.getName();
                    f.set(entity, rs.getObject(colName));
                }
                result.add(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
