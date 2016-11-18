package ai;

import ai.Global;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import javax.swing.*;

import java.awt.*;

import kalaha.*;

/**
 * This is the main class for your Kalaha AI bot. Currently it only makes a
 * random, valid move each turn.
 *
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable {

    private int player;
    private JTextArea text;

    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;

    /**
     * Creates a new client.
     */
    public AIClient() {
        player = -1;
        connected = false;
		// This is some necessary client stuff. You don't need
        // to change anything here.
        initGUI();

        try {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            addText("Done");
            connected = true;
        } catch (Exception ex) {
            addText("Unable to connect to server");
            return;
        }
    }

    /**
     * Starts the client thread.
     */
    public void start() {
        // Don't change this
        if (connected) {
            thr = new Thread(this);
            thr.start();
        }
    }

    /**
     * Creates the GUI.
     */
    private void initGUI() {
        // Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420, 250));
        frame.getContentPane().setLayout(new FlowLayout());

        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));

        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }

    /**
     * Adds a text string to the GUI textarea.
     *
     * @param txt The text to add
     */
    public void addText(String txt) {
        // Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }

    /**
     * Thread for server communication. Checks when it is this client's turn to
     * make a move.
     */
    public void run() {
        String reply;
        running = true;
        int bestmove;
        try {
            while (running) {
                // Checks which player you are. No need to change this.
                if (player == -1) {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);

                    addText("I am player " + player);
                }

                // Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if (reply.equals("1") || reply.equals("2")) {
                    int w = Integer.parseInt(reply);
                    if (w == player) {
                        addText("I won!");
                    } else {
                        addText("I lost...");
                    }
                    running = false;
                }
                if (reply.equals("0")) {
                    addText("Even game!");
                    running = false;
                }

                // Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running) {
                    int nextPlayer = Integer.parseInt(reply);

                    if (nextPlayer == player) {
                        out.println(Commands.BOARD);
                        //System.out.println("1st" );
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove) {
                            long startT = System.currentTimeMillis();
                            //System.out.println("2nd" );
							// This is the call to the function for making a
                            // move.
                            // You only need to change the contents in the
                            // getMove()
                            // function.
                            GameState currentBoard = new GameState(
                                    currentBoardStr);
                            //currentBoard= currentBoard.clone();   // ADDED STATEMENT
                            int cMove = getMove(currentBoard);

                            // Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double) tot / (double) 1000;

                            out.println(Commands.MOVE + " " + cMove + " "
                                    + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR")) {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e
                                        + " secs");
                                //System.out.println("3rd" );
                            }
                        }
                    }
                }

                // Wait
                Thread.sleep(100);
            }
        } catch (Exception ex) {
            running = false;
        }

        try {
            socket.close();
            addText("Disconnected from server");
            //System.exit(0);
        } catch (Exception ex) {
            addText("Error closing connection: " + ex.getMessage());
        }
    }

    /**
     * This is the method that makes a move each time it is your turn. Here you
     * need to change the call to the random method to your Minimax search.
     *
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
    public int getMove(GameState currentBoard) {
        
        int myMove;
        if(currentBoard.getNextPlayer()== 1)
            //if AI is playing first
         myMove = minimax(0,currentBoard,-1)[1] ; //call to minimax search ,  south is AI "MAX"
        else
          myMove = minimax2(0,currentBoard,-1)[1];   //if AI is playing second 
        return myMove;
    }
    public int[] minimax(int level,GameState currentBoard,int cambo) {
        int score = 0; 
        int bestscore = 0, bestmove = -1;    
        if (currentBoard.gameEnded() || level == 5) { // stop generating tree and assign utility value to ambos of 5th level
            bestscore = utility1(cambo,currentBoard); 
            bestmove = cambo;                                         
        } 
        else if (currentBoard.getNextPlayer() == 1) {
            bestscore = Integer.MIN_VALUE;   
            int i=0;
             for ( i = GameState.START_S; i <= GameState.END_S ; i++  ){
                 if((currentBoard.getBoard()[i]) == 0)
                 //checking if ambo is empty , if so go to next ambo
                     continue;                                     
               cambo=i;
                if (currentBoard.makeMove(i)) {
                    while (currentBoard.getNextPlayer() == 1 && !currentBoard.gameEnded()) {
                        //checking for second chance
                        for(int j = GameState.START_S; j <= GameState.END_S; j++) {
                            //selecting non empty ambo for second chance
                            if(currentBoard.getBoard()[j]>0){
                               currentBoard.makeMove(j);
                               j=GameState.END_S+1;}
                        }
                    }//while
                        score = minimax(level + 1, currentBoard,cambo)[0]; //recursive function to generate tree
                        if (score > bestscore) {
                            bestscore = score; //assigning best possible score to bestscore
                            bestmove = i;// assigning bestscore corresponding move to bestmove
                        } //inner if                 
                }//if
             }// for loop
    }//else if
    else if (currentBoard.getNextPlayer() == 2)
    {      
            bestscore = Integer.MAX_VALUE;
            int j = 0;
            for (j = GameState.START_N; j <= GameState.END_N ; j++) {
                if((currentBoard.getBoard()[j]) == 0)
                    //checking if ambo is empty , if so go to next ambo
                    continue;
               cambo = j;              
                int z = j - GameState.HOUSE_S; // ambo number should be between 1 to 6 
                if (currentBoard.makeMove(z)) {                   
                    while (currentBoard.getNextPlayer() == 2 && !currentBoard.gameEnded()) {   
                        //checking for second chance
                        for(int j1 = GameState.START_N; j1 <= GameState.END_N; j1++)
                        {
                            if(currentBoard.getBoard()[j1]>0){  
                                //selecting non empty ambo for second chance
                               int j2 = j1 - GameState.HOUSE_S; //as ambo number should be between 1 to 6 for moving
                               currentBoard.makeMove(j2); //make second move
                               j1=GameState.END_N+1;}
                        } // for                      
                    } //while                                      
                    score = minimax(level + 1, currentBoard,cambo)[0];  //recursive minimax function                
                    if (score < bestscore) {
                        bestscore = score; //assiging least possible score to MIN  as it should be given least utility
                        bestmove = j; //its corresponding move i.e ambo
                    }// if
                }//main if                      
        }//for                       
    }// else if
        return new int[]{bestscore, bestmove}; //returning bestscore and its corresponding ambo
   } // minmax
    
    int utility1(int ambo, GameState gs) { 
        //AI playing first so its SOUTH
            int[] b1 = gs.getBoard();
            int utility_value = 0;      
                utility_value = b1[GameState.HOUSE_S]-b1[GameState.HOUSE_N];    
          return utility_value;
        }
    
   public int[] minimax2(int levelx,GameState currentBoard,int cambo) {
       // if player plays second nextplayer 2 is MAX and 1 is MIN
        int score1 = 0; 
        int bestscore1 = 0, bestmove1 = -1;    //different names to avoid confusion
        if (currentBoard.gameEnded() || levelx == 5) { 
            bestscore1 = utility2(cambo,currentBoard); 
            bestmove1 = cambo;                   
        } 
        else if (currentBoard.getNextPlayer() == 1) {
            //MIN
            bestscore1 = Integer.MAX_VALUE;   
            int i=0;
             for ( i = GameState.START_S; i <= GameState.END_S ; i++  ){
                 if((currentBoard.getBoard()[i]) == 0)
                 //checking if ambo is empty , if so go to next ambo
                     continue;                                     
               cambo=i;
                if (currentBoard.makeMove(i)) {
                    while (currentBoard.getNextPlayer() == 1 && !currentBoard.gameEnded()) {
                        //checking for second chance
                        for(int j = GameState.START_S; j <= GameState.END_S; j++) {
                            //selecting non empty ambo for second chance
                            if(currentBoard.getBoard()[j]>0){
                               currentBoard.makeMove(j);
                               j=GameState.END_S+1;}
                        }
                    }//while
                        score1 = minimax2(levelx + 1, currentBoard,cambo)[0]; //recursive function to generate tree
                        if (score1 < bestscore1) {
                            //MIN , so least score should be considered
                            bestscore1 = score1; //assigning least possible score to bestscore
                            bestmove1 = i;// assigning bestscore corresponding move to bestmove
                        } //inner if                 
                }//if
             }// for loop
    }//else if
    else if (currentBoard.getNextPlayer() == 2)
    {   
        //MAX in this case as AI plays second
            bestscore1 = Integer.MIN_VALUE;
            int j = 0;
            for (j = GameState.START_N; j <= GameState.END_N ; j++) {
                if((currentBoard.getBoard()[j]) == 0)
                    //checking if ambo is empty , if so go to next ambo
                    continue;
               cambo = j;              
                int z = j - GameState.HOUSE_S; // ambo number should be between 1 to 6 
                if (currentBoard.makeMove(z)) {                   
                    while (currentBoard.getNextPlayer() == 2 && !currentBoard.gameEnded()) {   
                        //checking for second chance
                        for(int j1 = GameState.START_N; j1 <= GameState.END_N; j1++)
                        {
                            if(currentBoard.getBoard()[j1]>0){  
                                //selecting non empty ambo for second chance
                               int j2 = j1 - GameState.HOUSE_S; //as ambo number should be between 1 to 6 for moving
                               currentBoard.makeMove(j2); //make second move
                               j1=GameState.END_N+1;}
                        } // for                      
                    } //while                                      
                    score1 = minimax(levelx + 1, currentBoard,cambo)[0];  //recursive minimax function                
                    if (score1 > bestscore1) {
                        //MAX so best score should be considered
                        bestscore1 = score1; //assiging best possible score 
                        bestmove1 = j; //its corresponding move i.e ambo
                    }// if
                }//main if                      
        }//for                       
    }// else if
        return new int[]{bestscore1, (bestmove1 - GameState.HOUSE_S)}; //returning bestscore and its corresponding ambo and ambo number should be betweeen 1 to 6
   } // minmax  
    
    int utility2(int ambo, GameState gs) { 
        // AI playing second so its NORTH 
            int[] b2 = gs.getBoard();
            int utility_value1 = 0;
                utility_value1 = b2[GameState.HOUSE_N]-b2[GameState.HOUSE_S]; 
          return utility_value1;
        }
    //public int getRandom() {
        //return 1 + (int) (Math.random() * 6);}
}//public class
/**
     * Returns a random ambo number (1-6) used when making a random move.
     *
     * @return Random ambo number
     */ 