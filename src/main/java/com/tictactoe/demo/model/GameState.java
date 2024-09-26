package com.tictactoe.demo.model;

public class GameState {
    private char[][] board;
    private char currentPlayer;
    private char winner;

    public GameState() {
        this.board = new char[3][3];
        this.currentPlayer = 'X'; // X starts the game
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                this.board[i][j] = '-'; // Empty cells
            }
        }
    }

    // Getters and Setters
    public char[][] getBoard() {
        return board;
    }

    public void setBoard(char[][] board) {
        this.board = board;
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(char currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public char getWinner(){
        return this.winner;
    }
    
    public void setWinner(char x){
        this.winner = x;
    }
}
