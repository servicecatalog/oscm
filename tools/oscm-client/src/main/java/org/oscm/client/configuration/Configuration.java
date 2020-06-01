package org.oscm.client.configuration;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Getter
public class Configuration {

  private String baseUrl;
  private String trustStoreLocation;
  private String trustStorePassword;

  public Configuration() throws IOException {
    ConfigurationLoader loader = new ConfigurationLoader();
    loader.load();
  }

  private class ConfigurationLoader {
    private Properties properties;

    private ConfigurationLoader() throws IOException {
      properties = new Properties();

      InputStream propertiesStream =
          getClass().getClassLoader().getResourceAsStream("configuration.properties");

      properties.load(propertiesStream);
    }

    private void load() {
      baseUrl = properties.getProperty("base.url");
      trustStoreLocation = properties.getProperty("trust.store.location");
      trustStorePassword = properties.getProperty("trust.store.password");
    }
  }
}
