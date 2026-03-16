package in.maheshshelakee.moneymanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.isTokenValid(token)) {
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                // FIX: Guard null status claim — tokens issued by an older app version may not
                //      carry this claim. Default to "ACTIVE" so they are not silently passed
                //      through, but are still treated as valid non-banned sessions.
                //      (The real status check on login and the DB-based guard in AdminService
                //      are the authoritative sources; this is a defence-in-depth layer.)
                String status = jwtUtil.extractStatus(token);
                if (status == null) {
                    status = "ACTIVE";
                }

                if ("BANNED".equals(status) || "SUSPENDED".equals(status)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,
                            "User account is " + status.toLowerCase());
                    return;
                }

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // FIX: Guard null role claim the same way
                    String resolvedRole = (role != null) ? role : "USER";
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + resolvedRole);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null,
                                    Collections.singletonList(authority));
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
