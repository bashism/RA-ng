package edu.duke.ra.core;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import edu.duke.ra.core.db.DB;

public class RAConfig {
    private final String propsFileName;
    private final boolean verbose;
    private Properties properties;

    public static class Builder {
        private boolean verbose = false;
        private String propsFileName = "/ra.properties";
        private String url = "";
        private String schema = "";
        private String user = "";
        private String password = "";
        public Builder verbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }
        public Builder propsFileName(String name) {
            this.propsFileName = name;
            return this;
        }
        public Builder url(String url) {
            this.url = url;
            return this;
        }
        public Builder schema(String schema) {
            this.schema = schema;
            return this;
        }
        public Builder user(String user) {
            this.user = user;
            return this;
        }
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        public RAConfig build() throws IOException, RAConfigException {
            return new RAConfig(this);
        }
    }

    private RAConfig(Builder builder) throws IOException {
        this.propsFileName = builder.propsFileName;
        this.verbose = Boolean.parseBoolean(getValueOf("verbose", Boolean.toString(builder.verbose)));
        this.properties = new Properties();
        this.properties.put("url", getValueOf("url", builder.url));
        this.properties.put("schema", getValueOf("schema", builder.schema));
        this.properties.put("user", getValueOf("user", builder.user));
        this.properties.put("password", getValueOf("password", builder.password));
    }

    public boolean verbose() {
        return verbose;
    }

    public DB configureDB() throws IOException, SQLException {
        DB database = new DB(properties.getProperty("url"), properties);
        return database;
    }

    private String getValueOf(String property, String value) throws IOException {
        if (value.length() != 0) {
            return value;
        }
        else if (propsFileName.length() != 0) {
            Properties properties = new Properties();
            properties.load(this.getClass().getResourceAsStream(propsFileName));
            String newValue = properties.getProperty(property);
            if (newValue == null) {
                return "";
            }
            return newValue;
        }
        else {
            return "";
        }
    }
}
