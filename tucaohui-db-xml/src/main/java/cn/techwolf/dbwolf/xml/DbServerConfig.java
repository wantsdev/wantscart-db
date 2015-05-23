package cn.techwolf.dbwolf.xml;

/**
 * 数据库服务器配置.
 * 
 */
public class DbServerConfig {

    private String type;

    private String database;

    private String host;

    private int port;

    private String charset;

    private String user;

    private String password;

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("type:");
        sb.append(type);
        sb.append(",");
        sb.append("database:");
        sb.append(database);
        sb.append(",");
        sb.append("host:");
        sb.append(host);
        sb.append(",");
        sb.append("port:");
        sb.append(port);
        sb.append(",");
        sb.append("charset:");
        sb.append(charset);
        sb.append(",");
        sb.append("user:");
        sb.append(user);
        sb.append(",");
        sb.append("password:");
        sb.append(password);
        sb.append("}");
        return sb.toString();
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(final String database) {
        this.database = database;
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(final String charset) {
        this.charset = charset;
    }

    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

}
