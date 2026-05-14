package com.sgdea.administracion.infrastructure.security;
/**
 * Almacena en un ThreadLocal los claims del usuario propagados por el API Gateway.
 * Permite acceder a datos del tenant autenticado desde cualquier capa sin inyecciones.
 *
 * Ciclo de vida: GatewayAuthenticationFilter carga el contexto al inicio del request
 * y lo limpia en el finally del mismo filtro.
 *
 * Uso:
 *   String companyId = GatewayClaimsHolder.get().companyId();
 *   String connId    = GatewayClaimsHolder.get().connectionId();
 */
public final class GatewayClaimsHolder {
    private static final ThreadLocal<GatewayClaims> HOLDER = new ThreadLocal<>();
    private GatewayClaimsHolder() {}
    public static void set(GatewayClaims claims) { HOLDER.set(claims); }
    public static GatewayClaims get() {
        GatewayClaims claims = HOLDER.get();
        return claims != null ? claims : GatewayClaims.empty();
    }
    public static void clear() { HOLDER.remove(); }
    public record GatewayClaims(
            String email,
            String userId,
            String roleCode,
            String companyId,
            String companyCode,
            String connectionId
    ) {
        public static GatewayClaims empty() {
            return new GatewayClaims("", "", "", "", "", "");
        }
        public boolean isEmpty() { return email == null || email.isBlank(); }
    }
}
