package com.clinicHelper.Config;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.clinicHelper.exceptions.ApiException;
import com.clinicHelper.exceptions.ValidationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor // if you have private final fields, it will create constructor for them
// public class JwtAuthenticationFilter implement Filter(interface) -> can do that
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String userEmail;

            try {
                if(authHeader == null || !authHeader.startsWith("Bearer ")) {
                    filterChain.doFilter(request, response);
                    return;
                }
                jwt = authHeader.substring("Bearer ".length());
                userEmail = jwtService.extractUsername(jwt);
                if (userEmail == null) throw new ValidationException("JWT token is invalid or missing username");
                UserDetails userDetails;
                try {
                    userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                } catch (UsernameNotFoundException ex) {
                    throw new NotFoundException();
                }

                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    throw new ValidationException("JWT token is invalid or expired");
                }

                 UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                filterChain.doFilter(request, response);
            } catch (ApiException ex) {
                response.setStatus(ex.getStatus().value());
                response.setContentType("application/json");
                response.getWriter().write(
                "{\"code\":\"" + ex.getErrorCode() + "\", \"message\":\"" + ex.getMessage() + "\"}");
            }
            catch (Exception ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"code\":\"INTERNAL_ERROR\", \"message\":\"Unexpected error occurred\"}");
            }
    }
}

