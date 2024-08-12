package me.liwk.karhu.database;

import lombok.SneakyThrows;
import me.liwk.karhu.util.NetUtil;

import java.sql.*;
import java.util.UUID;

public class ExecutableStatement {
    private PreparedStatement statement;
    private int pos = 1;

    public ExecutableStatement(PreparedStatement statement) {
        this.statement = statement;
    }

    @SneakyThrows
    public ExecutableStatement append(Float obj) {
        this.statement.setFloat(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Double obj) {
        this.statement.setDouble(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Date obj) {
        this.statement.setDate(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Long obj) {
        this.statement.setLong(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Short obj) {
        this.statement.setShort(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(byte[] obj) {
        this.statement.setBytes(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Blob obj) {
        this.statement.setBlob(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Time obj) {
        this.statement.setTime(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Timestamp obj) {
        this.statement.setTimestamp(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Object obj) {
        this.statement.setObject(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(String obj) {
        this.statement.setString(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(UUID uuid) {
        if (uuid != null) {
            this.statement.setString(this.pos++, uuid.toString().replace("-", ""));
        } else {
            this.statement.setString(this.pos++, null);
        }

        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Array obj) {
        this.statement.setArray(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public ExecutableStatement append(Integer obj) {
        this.statement.setInt(this.pos++, obj);
        return this;
    }

    @SneakyThrows
    public Integer execute() {
        int ex;
        try {
            ex = this.statement.executeUpdate();
        } finally {
            NetUtil.close(this.statement);
        }

        return ex;
    }

    @SneakyThrows
    public void execute(ResultSetIterator iterator) {
        ResultSet rs = null;

        try {
            rs = this.statement.executeQuery();

            while (rs.next()) {
                iterator.next(rs);
            }
        } finally {
            NetUtil.close(this.statement, rs);
        }
    }

    @SneakyThrows
    public void executeSingle(ResultSetIterator iterator) {
        ResultSet rs = null;

        try {
            rs = this.statement.executeQuery();
            if (rs.next()) {
                iterator.next(rs);
            } else {
                iterator.next(null);
            }
        } finally {
            NetUtil.close(this.statement, rs);
        }
    }

    @SneakyThrows
    public ResultSet executeQuery() {
        return this.statement.executeQuery();
    }
}
