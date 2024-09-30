package com.tictactoe.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.tictactoe.demo.model.GameState;
import com.tictactoe.demo.model.TicTacToeAI;
import com.tictactoe.demo.model.UserTokenMessage;
import com.tictactoe.demo.service.MessageProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.security.GeneralSecurityException;
import java.io.IOException;

@RestController
@RequestMapping("/api/tictactoe")
public class TicTacToeController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private ObjectMapper objectMapper;

    private TicTacToeAI ai = new TicTacToeAI();

    @PostMapping("/move")
    public ResponseEntity<?> makeMove(@RequestHeader("Authorization") String token, @RequestHeader("userId") String userId, @RequestBody GameState gameState,
            @RequestParam boolean isPlayerMove) throws GeneralSecurityException, IOException, IllegalAccessException {

        
        if (!validateTokenAsRedis(userId)) {
            return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        if (isPlayerMove) {
            if (fullBoard(gameState.getBoard())) {
                gameState.setWinner('N');
                return ResponseEntity.ok(gameState);
            }
            gameState.setCurrentPlayer('O');
            int[] aiMove = ai.findBestMove(gameState.getBoard());
            gameState.getBoard()[aiMove[0]][aiMove[1]] = 'O';
            char winner = checkWinner(gameState.getBoard());

            //if winner is 'O', the user should be going out automatically
            if(winner == 'O'){

                UserTokenMessage message = new UserTokenMessage(userId, token);
                System.out.println(token);
                String jsonMessage = objectMapper.writeValueAsString(message);
                messageProducer.sendMessage(jsonMessage);
            }

            gameState.setWinner(winner);
            gameState.setCurrentPlayer('X');
            return ResponseEntity.ok(gameState);

        }

        return ResponseEntity.ok(gameState);
    }

    private boolean fullBoard(char[][] board) {
        for (char[] row : board) {
            for (char cell : row) {
                if (cell == '-') {
                    return false;
                }
            }
        }
        return true;
    }

    private char checkWinner(char[][] board) {
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


   
    public String getTokenFromRequest(String token) throws IllegalAccessException {
        String[] parts = token.split(" ");
        if (parts.length != 2 || !parts[0].contains("Bearer")) {
            throw new IllegalAccessException("Authorization Bearer format invalid. <Bearer {token}>");
        }
        return parts[1];
    }


    private boolean validateTokenAsRedis(String userId){
        String token = redisTemplate.opsForValue().get("USER_INFO:" + userId);
        return token != null;
    }


}
