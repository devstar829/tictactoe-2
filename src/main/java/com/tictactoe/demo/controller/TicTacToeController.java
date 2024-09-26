package com.tictactoe.demo.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders; // From Spring
import com.google.api.client.util.Value;
import com.tictactoe.demo.model.GameState;
import com.tictactoe.demo.model.TicTacToeAI;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;


@RestController
@RequestMapping("/api/tictactoe")
// @CrossOrigin(origins = "*", allowedHeaders = "*", allowCredentials = "true")
public class TicTacToeController {

    private String tokenInfoUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token="; // Set this in your application properties

    private TicTacToeAI ai = new TicTacToeAI();

    @PostMapping("/move")
    // @CrossOrigin(origins = "*", allowedHeaders = "*", allowCredentials = "true")
    public ResponseEntity<?> makeMove(@RequestHeader("Authorization") String token, @RequestBody GameState gameState, @RequestParam boolean isPlayerMove) {


        if (!validateToken(getTokenFromRequest(token))) {
            return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        if (isPlayerMove) {
            gameState.setCurrentPlayer('O');
            int[] aiMove = ai.findBestMove(gameState.getBoard());
            gameState.getBoard()[aiMove[0]][aiMove[1]] = 'O';
            gameState.setCurrentPlayer('X');
        }

        return ResponseEntity.ok(gameState);
    }

     /**
     * @param token
     * @return
     */
    private boolean validateToken(String token) throws GeneralSecurityException, IOException, IllegalAccessException {
//        RestTemplate restTemplate = new RestTemplate();
//
//        // Prepare the request to Google's token info endpoint
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", token);
//        HttpEntity<String> request = new HttpEntity<>(headers);
//
//        try {
//            // Call Google's token info endpoint
//            ResponseEntity<String> response = restTemplate.exchange(
//                tokenInfoUrl + token, HttpMethod.GET, request, String.class
//            );
//
//            // Check if response status is OK
//            return response.getStatusCode() == HttpStatus.OK;
//        } catch (Exception e) {
//            return false; // Token validation failed
//        }
        NetHttpTransport transport  = new NetHttpTransport();
        GsonFactory gsonFactory = new GsonFactory();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, gsonFactory)
                .setAudience(Collections.singletonList("118071667465-aa58e14p3cjeqhncamleb7bvcb3gdcm0.apps.googleusercontent.com"))
                .build();

        GoogleIdToken idToken = verifier.verify(token);
        return idToken != null;
    }

    public String getTokenFromRequest(String token) throws IllegalAccessException {
        String[] parts = token.split(" ");
        if (parts.length != 2 || !parts[0].contains("Bearer")) {
            throw new IllegalAccessException("Authorization Bearer format invalid. <Bearer {token}>");
        }
        return parts[1];
    }
}
