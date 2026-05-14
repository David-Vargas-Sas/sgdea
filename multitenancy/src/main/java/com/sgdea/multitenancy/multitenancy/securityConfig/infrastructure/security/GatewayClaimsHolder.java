package com.sgdea.multitenancy.multitenancy.securityConfig.infrastructure.security;

import com.sgdea.multitenancy.multitenancy.securityConfig.domain.model.GatewayClaims;

/**
 * Gestiona el ciclo de vida del contexto de autenticación por hilo (ThreadLocal).
 *
 * <p><b>Responsabilidad única:</b> almacenar y liberar el objeto {@link GatewayClaims}
 * durante el ciclo de vida de un request HTTP. No contiene lógica de negocio
 * ni representación de datos.</p>
 *
 * <h3>Ciclo de vida</h3>
 * <ol>
 *   <li>{@link JwtAuthenticationFilter} invoca {@link #set(GatewayClaims)} al validar
 *       el JWT al inicio del request.</li>
 *   <li>Cualquier capa (servicio, repositorio) accede con {@link #get()} mientras
 *       dure el request.</li>
 *   <li>{@link JwtAuthenticationFilter} invoca {@link #clear()} en el bloque
 *       {@code finally} para evitar fugas de memoria en el pool de hilos.</li>
 * </ol>
 *
 * <h3>Uso</h3>
 * <pre>{@code
 *   GatewayClaims claims = GatewayClaimsHolder.get();
 *   if (claims.isPresent()) {
 *       String companyId    = claims.companyId();
 *       String connectionId = claims.connectionId();
 *   }
 * }</pre>
 *
 * <h3>Hilo-seguridad y ThreadLocal</h3>
 * <p>{@link ThreadLocal} garantiza aislamiento por hilo: cada thread del pool
 * Tomcat/Undertow obtiene su propia copia del contexto. El {@code clear()} en
 * {@code finally} es <b>obligatorio</b> para evitar que hilos reutilizados
 * conserven datos de requests anteriores.</p>
 */
public final class GatewayClaimsHolder {

    private static final ThreadLocal<GatewayClaims> HOLDER = new ThreadLocal<>();

    /** Clase utilitaria: no se permite instanciar. */
    private GatewayClaimsHolder() {}

    /**
     * Establece los claims del usuario autenticado para el hilo actual.
     *
     * @param claims claims obtenidos tras validar el JWT; no debe ser {@code null}
     */
    public static void set(GatewayClaims claims) {
        HOLDER.set(claims);
    }

    /**
     * Retorna los claims del hilo actual, o {@link GatewayClaims#empty()} si no
     * se han establecido (request no autenticado o {@link #clear()} ya fue llamado).
     *
     * <p>Nunca retorna {@code null}. El llamador debe comprobar
     * {@link GatewayClaims#isPresent()} para distinguir un contexto autenticado
     * de uno vacío.</p>
     *
     * @return claims activos o instancia vacía; nunca {@code null}
     */
    public static GatewayClaims get() {
        GatewayClaims claims = HOLDER.get();
        return claims != null ? claims : GatewayClaims.empty();
    }

    /**
     * Elimina los claims del hilo actual.
     *
     * <p><b>Debe llamarse siempre en un bloque {@code finally}</b> para evitar
     * fugas de memoria cuando Tomcat/Undertow reutiliza el hilo en requests
     * posteriores.</p>
     */
    public static void clear() {
        HOLDER.remove();
    }
}
