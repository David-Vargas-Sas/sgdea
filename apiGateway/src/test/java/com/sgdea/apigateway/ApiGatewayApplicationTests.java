package com.sgdea.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test de humo: verifica que el contexto de Spring Boot carga correctamente.
 *
 * <p>Usa RANDOM_PORT para evitar conflictos de puerto en CI.
 * La configuración de test (src/test/resources/application.yml) deshabilita:
 * <ul>
 *   <li>Docker Compose — no se necesita durante los tests</li>
 *   <li>Rutas del Gateway — elimina dependencia de Redis/RequestRateLimiter</li>
 * </ul>
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // Deshabilita el mecanismo de bootstrap heredado de Spring Cloud antes de que
                // BootstrapConfigFileApplicationListener intente llamar métodos de
                // ConfigDataEnvironmentPostProcessor que cambiaron en Spring Boot 3.5.x.
                "spring.cloud.bootstrap.enabled=false"
        }
)
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // Si el contexto carga sin excepción, el test pasa.
    }

}
