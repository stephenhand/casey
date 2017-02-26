package com.handysolutions.casey.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
public class QueryStreamer {

    private final ResultSetStreamer resultsetStreamer;

    public QueryStreamer() {
        this(new ResultSetStreamer());
    }

    public QueryStreamer(ResultSetStreamer rs) {
        this.resultsetStreamer = rs;
    }

    private static class JDBCContext {
        Connection conn;
        PreparedStatement pstmt;
    }

    public Stream<Map<String, Object>> executeQuery(final Supplier<Connection> connectionSupplier,
                                                    String command,
                                                    Object... params) {

        JDBCContext ctx = new JDBCContext();
        Stream<Map<String, Object>> queryStream = StreamSupport.stream(new Spliterators.AbstractSpliterator<Map<String, Object>>(Long.MAX_VALUE, Spliterator.ORDERED) {
            Stream<Map<String, Object>> resultSetStream;

            @Override
            public boolean tryAdvance(Consumer<? super Map<String, Object>> action) {
                try {
                    if (ctx.conn==null) {
                        ctx.conn=connectionSupplier.get();
                        try {
                            ctx.pstmt=prepareQuery(ctx.conn, command, params);
                            resultSetStream = resultsetStreamer.streamResults(ctx.pstmt.executeQuery());
                        } catch (Exception e) {
                            cleanup(ctx);
                            throw e;
                        }
                    }
                    if (resultSetStream.spliterator().tryAdvance(action)) {
                        return true;
                    } else {
                        cleanup(ctx);
                        return false;
                    }
                }
                catch (SQLException sqlEx){
                    cleanup(ctx);
                    throw new RuntimeException(sqlEx);
                }
                catch (Throwable e) {
                    cleanup(ctx);
                    throw e;
                }
            }
        }, false);
        return queryStream.onClose(()->cleanup(ctx));

    }

    private PreparedStatement prepareQuery(Connection connection, String command, Object... params) throws SQLException{
        PreparedStatement pstmt = connection.prepareStatement(command);
        Integer idx = 0;
        if (params != null) {
            for (Object p : params) {
                ++idx;
                pstmt.setObject(idx, p);
            }
        }
        return pstmt;
    }

    private void cleanup(JDBCContext ctx){

        try {
            if (ctx.pstmt!=null && !ctx.pstmt.isClosed()) {
                ctx.pstmt.close();
            }
        } catch (SQLException e1) {
            throw new RuntimeException(e1);
        }
        finally {
            cleanupConnection(ctx.conn);
        }
    }

    private void cleanupConnection(Connection connection){
        try
        {
            if (connection != null && !connection.isClosed())
            {
                if (!connection.getAutoCommit())
                {
                    connection.rollback();
                }
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        finally
        {
            try {
                if (connection != null && !connection.isClosed())
                {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
