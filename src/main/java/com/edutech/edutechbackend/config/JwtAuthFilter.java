package com.edutech.edutechbackend.config;


import com.edutech.edutechbackend.service.CustomUserDetailsService;
import com.edutech.edutechbackend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
// ↑ makes this a Spring bean so SecurityConfig can inject it
// and register it in the filter chain

@RequiredArgsConstructor
// ↑ Lombok generates constructor for all final fields
// equivalent to writing @Autowired constructor manually

public class JwtAuthFilter extends OncePerRequestFilter {
// ↑ extends OncePerRequestFilter
//   guarantees doFilterInternal() runs EXACTLY once per request
//   even if internal forwards/redirects happen

    private final JwtUtil jwtUtil;
    // ↑ from Step 3c
    //   used for: extractEmail(), validateToken()

    private final CustomUserDetailsService userDetailsService;
    // ↑ our bridge
    //   used for: loadUserByUsername() → hits DB → returns UserDetails

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            // ↑ everything about the incoming request
            //   headers, body, URL, method etc

            HttpServletResponse response,
            // ↑ the outgoing response we can modify if needed

            FilterChain filterChain
            // ↑ the remaining filters after this one
            //   we MUST call filterChain.doFilter() to continue

    ) throws ServletException, IOException {

        // ── STEP 1: Read Authorization header ──────────────────────────────
        final String authHeader = request.getHeader("Authorization");
        // reads the value of the "Authorization" header
        // e.g. "Bearer eyJhbGciOiJIUzI1NiJ9.xxx.yyy"
        // or null if header is missing

        // ── STEP 2: Guard clause — skip if no Bearer token ─────────────────
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // authHeader == null      → no Authorization header at all
            // !startsWith("Bearer ")  → header exists but not a Bearer token
            //                          could be Basic auth or something else

            filterChain.doFilter(request, response);
            // ↑ "I have nothing to do here, pass this along"
            // happens for:
            //   → public routes (/api/auth/login, /api/auth/register)
            //   → requests with no token
            //   → requests with non-Bearer auth

            return;
            // ↑ STOP executing this method
            // without return, code below would still run
        }

        // ── STEP 3: Extract raw token ───────────────────────────────────────
        final String token = authHeader.substring(7);
        // "Bearer eyJhbGciOiJIUzI1NiJ9.xxx.yyy"
        //  0123456
        //         ^ index 7 starts here
        // substring(7) = "eyJhbGciOiJIUzI1NiJ9.xxx.yyy"

        // ── STEP 4 + 5 + 6 + 7 + 8 + 9: Process token ─────────────────────
        // wrapped in try-catch because:
        //   extractEmail() can throw:
        //     ExpiredJwtException    → token expired
        //     SignatureException     → token tampered
        //     MalformedJwtException  → garbage token
        //   loadUserByUsername() can throw:
        //     UsernameNotFoundException → student deleted from DB
        // Any of these → we catch → authentication not set → 401 later
        try {

            // ── STEP 4: Extract email from token ───────────────────────────
            final String email = jwtUtil.extractEmail(token);
            // internally runs extractAllClaims():
            //   → verifies HMAC-SHA256 signature
            //   → checks structure
            //   → reads "sub" claim
            //   → returns email string

            // ── STEP 5: Check if should proceed ────────────────────────────
            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {
                // email != null
                //   → we successfully got email from token

                // getAuthentication() == null
                //   → this request hasn't been authenticated yet
                //   → SecurityContext is empty for this request
                //   → prevents processing the same request twice
                //   → important for thread safety

                // ── STEP 6: Load student from database ─────────────────────
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);
                // calls CustomUserDetailsService.loadUserByUsername()
                // → hits PostgreSQL
                // → SELECT * FROM students WHERE email = ?
                // → converts Student → UserDetails
                // → userDetails.getUsername() = email FROM DATABASE
                //   this is the second parameter for validateToken()

                // ── STEP 7: Validate token ──────────────────────────────────
                if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                    // validateToken(token, userDetails.getUsername())
                    //
                    // param 1: token
                    //   → extractEmail(token) runs internally
                    //   → gets email FROM THE TOKEN
                    //
                    // param 2: userDetails.getUsername()
                    //   → email FROM THE DATABASE (loaded in step 6)
                    //
                    // comparison:
                    //   token email == database email? ✅
                    //   !isExpired()? ✅
                    //   both true → returns true

                    // ── STEP 8: Create authentication object ────────────────
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    // ↑ principal: WHO is authenticated
                                    //   the full UserDetails object
                                    //   controllers can call getAuthentication()
                                    //   to get this back

                                    null,
                                    // ↑ credentials: password
                                    //   null because we already verified via JWT
                                    //   no need to store password in memory

                                    userDetails.getAuthorities()
                                    // ↑ roles/permissions
                                    //   empty ArrayList for now
                                    //   will be ROLE_STUDENT, ROLE_ADMIN later
                            );

                    // attach extra HTTP request details
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );
                    // ↑ adds metadata like:
                    //   remote IP address
                    //   session ID
                    //   useful for audit logging later

                    // ── STEP 9: Register in SecurityContext ─────────────────
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authToken);
                    // ↑ THE most important line in this entire filter
                    //
                    // SecurityContextHolder = global holder
                    // .getContext()         = this request's context
                    // .setAuthentication()  = store the identity
                    //
                    // After this line:
                    //   Spring Security knows john@gmail.com is authenticated
                    //   for THIS request on THIS thread
                    //   Protected routes will allow this request
                    //   Controllers can read this to get current user
                }
            }

        } catch (Exception e) {
            // any exception during token processing:
            //   ExpiredJwtException, SignatureException,
            //   MalformedJwtException, UsernameNotFoundException
            //
            // we intentionally do NOTHING here
            // authentication was never set in SecurityContext
            // filterChain.doFilter() still runs below
            // Spring Security sees no authentication
            // returns 401 automatically
            //
            // we could log the exception here for debugging:
            // logger.warn("JWT validation failed: {}", e.getMessage());
        }

        // ── STEP 10: Pass to next filter ────────────────────────────────────
        filterChain.doFilter(request, response);
        // ALWAYS called regardless of success or failure
        // passes request to remaining filters in chain
        // eventually reaches DispatcherServlet → Controller
        //
        // if authentication was set    → controller runs ✅
        // if authentication was not set → Spring Security blocks with 401
    }
}