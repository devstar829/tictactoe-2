package com.tictactoe.demo.controller;

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


@RestController
@RequestMapping("/api/tictactoe")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true")
public class TicTacToeController {

    @Value("${google.oauth2.token-info-url}")
    private String tokenInfoUrl; // Set this in your application properties
    private TicTacToeAI ai = new TicTacToeAI();

    @PostMapping("/move")
    public ResponseEntity<?> makeMove(@RequestHeader("Authorization") String token, @RequestBody GameState gameState, @RequestParam boolean isPlayerMove) {

        if (!validateToken(token)) {
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
    private boolean validateToken(String token) {
        RestTemplate restTemplate = new RestTemplate();

        // Prepare the request to Google's token info endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            // Call Google's token info endpoint
            ResponseEntity<String> response = restTemplate.exchange(
                tokenInfoUrl, HttpMethod.GET, request, String.class
            );

            // Check if response status is OK
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false; // Token validation failed
        }
    }
}
