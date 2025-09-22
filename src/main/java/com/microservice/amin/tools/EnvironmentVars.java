
package com.microservice.amin.tools;

import com.google.gson.annotations.SerializedName;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentVars {
    public final EnvData envData;

    {
        envData = new EnvData();

        // Configuración de las variables de entorno
        String port = System.getenv("PORT");
        if (port != null && !port.isEmpty()) {
            envData.port = Integer.parseInt(port);
        }

        String rabbitUrl = System.getenv("RABBIT_URL");
        if (rabbitUrl != null && !rabbitUrl.isEmpty()) {
            envData.rabbitServerUrl = rabbitUrl;
        }

        String mongoUrl = System.getenv("MONGO_URL");
        if (mongoUrl != null && !mongoUrl.isEmpty()) {
            envData.databaseUrl = mongoUrl;
        }

        String authService = System.getenv("AUTH_SERVICE_URL");
        if (authService != null && !authService.isEmpty()) {
            envData.securityServerUrl = authService;
        }

        String catalogService = System.getenv("CATALOG_SERVICE_URL");
        if (catalogService != null && !catalogService.isEmpty()) {
            envData.catalogServerUrl = catalogService;
        }

        String fluentUrl = System.getenv("FLUENT_URL");
        if (fluentUrl != null && !fluentUrl.isEmpty()) {
            envData.fluentServerUrl = fluentUrl;
        }
    }

    public static class EnvData {
        @SerializedName("port")
        public int port = 3003; // Valor por defecto

        @SerializedName("rabbitServerUrl")
        public String rabbitServerUrl = "localhost"; // Valor por defecto

        @SerializedName("databaseUrl")
        public String databaseUrl = "mongodb://localhost:27017"; // Valor por defecto

        @SerializedName("securityServerUrl")
        public String securityServerUrl = "http://localhost:3000"; // Valor por defecto

        @SerializedName("catalogServerUrl")
        public String catalogServerUrl = "http://localhost:3002"; // Valor por defecto

        @SerializedName("fluentServerUrl")
        public String fluentServerUrl = "localhost:24224"; // Valor por defecto
    }
}
