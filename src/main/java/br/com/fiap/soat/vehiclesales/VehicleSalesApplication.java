package br.com.fiap.soat.vehiclesales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VehicleSalesApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleSalesApplication.class, args);
    }
}
