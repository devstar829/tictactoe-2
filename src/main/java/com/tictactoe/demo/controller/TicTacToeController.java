package com.tictactoe.demo.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Value;
import com.tictactoe.demo.model.GameState;
import com.tictactoe.demo.model.TicTacToeAI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.security.GeneralSecurityException;
import java.io.IOException;

@RestController
@RequestMapping("/api/tictactoe")
// @CrossOrigin(origins = "*", allowedHeaders = "*", allowCredentials = "true")
public class TicTacToeController {

    @Value("${google.oauth2.token-info-url}")
    private String tokenInfoUrl; // Set this in your application properties
    private TicTacToeAI ai = new TicTacToeAI();

    @PostMapping("/move")
    public ResponseEntity<?> makeMove(@RequestHeader("Authorization") String token, @RequestBody GameState gameState,
            @RequestParam boolean isPlayerMove) throws GeneralSecurityException, IOException, IllegalAccessException {

        if (!validateToken(getTokenFromRequest(token))) {
            return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
        }


        if (isPlayerMove) {
            if(fullBoard(gameState.getBoard())){
                gameState.setWinner('N');
                return ResponseEntity.ok(gameState);
            }
            gameState.setCurrentPlayer('O');
            int[] aiMove = ai.findBestMove(gameState.getBoard());
            gameState.getBoard()[aiMove[0]][aiMove[1]] = 'O';
            char winner = checkWinner(gameState.getBoard());
            gameState.setWinner(winner);
            gameState.setCurrentPlayer('X');
            return ResponseEntity.ok(gameState);

        }

        return ResponseEntity.ok(gameState);
    }

    private boolean fullBoard(char[][] board){
        for (char[] row : board) {
            for (char cell : row) {
                if (cell == '-') {
                    return false;
                }
            }
        }
        return true;
    }

    private char checkWinner(char[][] board){
        for (int i = 0; i < 3; i++) {
            if (board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != '-') {
                return board[i][0];
            }
        }
        for (int i = 0; i < 3; i++) {
            if (board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != '-') {
                return board[0][i];
            }
        }
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != '-') {
            return board[0][0];
        }
        if (board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != '-') {
            return board[0][2];
        }
        return '-';
    }

    /**
     * @param token
     * @return
     */
    private boolean validateToken(String token) throws GeneralSecurityException, IOException, IllegalAccessException {
        RestTemplate restTemplate = new RestTemplate();

        // Prepare the request to Google's token info endpoint
        NetHttpTransport transport = new NetHttpTransport();
        GsonFactory gsonFactory = new GsonFactory();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, gsonFactory)
                .setAudience(Collections
                        .singletonList("118071667465-aa58e14p3cjeqhncamleb7bvcb3gdcm0.apps.googleusercontent.com"))
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
