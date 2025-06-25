package com.synex.controller;

import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synex.configuration.JwtUtil;
import com.synex.domain.LoginRequest;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;


//@RestController
//@RequestMapping("/auth")
//public class AuthController {
//
//    private final AuthenticationManager authManager;
//    private final JwtUtil jwtUtil;
//
//    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil) {
//        this.authManager = authManager;
//        this.jwtUtil = jwtUtil;
//    }
//
//    @PostMapping("/login")
//    public String login(@RequestBody AuthRequest request) {
//        authManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
//        );
//        return jwtUtil.generateToken(request.getUsername());
//    }
//
//    @PostMapping("/logout")
//    public String logout(HttpServletRequest request) {
//        // JWT logout is stateless; just delete token on client side
//        return "Logged out";
//    }
//
//    static class AuthRequest {
//        private String username;
//        private String password;
//        public String getUsername() { return username; }
//        public void setUsername(String username) { this.username = username; }
//        public String getPassword() { return password; }
//        public void setPassword(String password) { this.password = password; }
//    }
//}

@RestController
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if ("user".equals(request.getUsername()) && "pass".equals(request.getPassword())) {
            String token = Jwts.builder()
                    .setSubject(request.getUsername())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                    .signWith(Keys.hmacShaKeyFor("mySecretKeymySecretKeymySecretKeymy".getBytes()), SignatureAlgorithm.HS256)
                    .compact();

            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    public static class LoginRequest {
        private String username;
        private String password;
        // getters and setters
		public String getUsername() {
			return username;
		}
		public void setUsername(String username) {
			this.username = username;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
        
    }
}


