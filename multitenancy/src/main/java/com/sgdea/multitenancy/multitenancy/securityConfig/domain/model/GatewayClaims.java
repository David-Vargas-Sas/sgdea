package com.sgdea.multitenancy.multitenancy.securityConfig.domain.model;

/**
 * Objeto de valor inmutable que representa los claims del usuario autenticado
 * propagados desde el API Gateway a través del JWT.
 *
 * <p>Responsabilidad única: <b>representar datos de identidad y tenant</b>.
 * No contiene lógica de almacenamiento ni ciclo de vida del request.</p>
 *
 * <p>Al ser un {@code record} de Java, la igualdad, {@code hashCode} y
 * {@code toString} se generan automáticamente a partir de sus componentes,
 * garantizando inmutabilidad y semántica de valor.</p>
 *
 * <h3>Campos</h3>
 * <ul>
 *   <li>{@code email}        – correo del usuario autenticado (claim requerido)</li>
 *   <li>{@code userId}       – identificador único del usuario</li>
 *   <li>{@code roleCode}     – código del rol asignado (ej. {@code ADMIN}, {@code USER})</li>
 *   <li>{@code companyId}    – UUID de la empresa/tenant</li>
 *   <li>{@code companyCode}  – código legible de la empresa</li>
 *   <li>{@code connectionId} – UUID de la conexión de BD del tenant</li>
 * </ul>
 *
 * <h3>Uso típico</h3>
 * <pre>{@code
 *   GatewayClaims claims = GatewayClaimsHolder.get();
 *   if (claims.isPresent()) {
 *       String tenantId = claims.companyId();
 *   }
 * }</pre>
 */
public record GatewayClaims(
        String email,
        String userId,
        String roleCode,
        String companyId,
        String companyCode,
        String connectionId
) {

    /**
     * Instancia canónica vacía para representar la ausencia de autenticación.
     *
     * <p>Se usa como valor por defecto en {@link com.sgdea.multitenancy.multitenancy.securityConfig.infrastructure.security.GatewayClaimsHolder#get()}
     * para evitar {@code NullPointerException} en código que consulta el contexto
     * sin comprobar si existe sesión activa.</p>
     *
     * <p><b>Alternativa a {@code null}:</b> devolver {@code empty()} evita
     * propagación de nulos, pero el llamador <em>debe</em> comprobar
     * {@link #isEmpty()} antes de confiar en los valores.</p>
     *
     * @return instancia de {@code GatewayClaims} con todos los campos vacíos
     */
    public static GatewayClaims empty() {
        return new GatewayClaims("", "", "", "", "", "");
    }

    /**
     * Indica si estos claims corresponden a un contexto sin autenticación.
     *
     * <p>Se evalúa sobre {@code email} porque es el único claim marcado como
     * requerido en el filtro JWT. Si el email está ausente, el resto de campos
     * no son confiables.</p>
     *
     * @return {@code true} si no hay usuario autenticado
     */
    public boolean isEmpty() {
        return email == null || email.isBlank();
    }

    /**
     * Inverso semántico de {@link #isEmpty()}. Permite escribir código más expresivo:
     * <pre>{@code
     *   if (GatewayClaimsHolder.get().isPresent()) { ... }
     * }</pre>
     *
     * @return {@code true} si hay un usuario autenticado con email válido
     */
    public boolean isPresent() {
        return !isEmpty();
    }
}

