package com.handysolutions.casey.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by stephen.hand on 26/02/2017.
 */
public class ResultSetStreamer {
    private final Supplier<Map<String, Object>> mapSupplier;
    public interface FieldConverter{
        Object convert(Object input);
    }

    public ResultSetStreamer(Supplier<Map<String, Object>> mapSupplier) {
        this.mapSupplier = mapSupplier;
    }

    public ResultSetStreamer() {
        this(HashMap<String, Object>::new);
    }

    public Stream<Map<String, Object>> streamResults(ResultSet rs){
        ResultSetMetaData meta;
        try {
            meta = rs.getMetaData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<Map<String, Object>>(Long.MAX_VALUE, Spliterator.ORDERED) {

            @Override
            public boolean tryAdvance(Consumer<? super Map<String, Object>> action) {
                try {
                    if (!rs.isClosed() && rs.next()) {
                        Map<String, Object> row = mapSupplier.get();
                        for (int i=1;i<=meta.getColumnCount(); i++){
                            row.put(meta.getColumnName(i), rs.getObject(i));
                        }
                        action.accept(row);
                        return true;
                    } else {
                        return false;
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }, false);
    }
}

