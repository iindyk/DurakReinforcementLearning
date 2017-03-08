package DurakGame;

import DurakGame.ReinforcementLearningPlayer.RLFileReader;
import DurakGame.ReinforcementLearningPlayer.RLPlayer;
import DurakGame.ReinforcementLearningPlayer.State;
import DurakGame.ReinforcementLearningPlayer.StateValueFunction;


import java.util.ArrayList;


/**
 * Created by igor on 28.09.16.
 */
public class Main {
    public static void main(String[] args){
        //value functions adjustment test
        try{
            RLPlayer player1=new RLPlayer();

            SimpleAgentPlayer player2=new SimpleAgentPlayer();
            ArrayList<Player> players=new ArrayList<>();
            players.add(player1);
            players.add(player2);
            //write
/*
            RLFileReader reader=new RLFileReader();
            player1.addToHistory(reader.readStateActionsFromTxd("D:\\coding\\java\\MyJavaTest\\games (2).txd"));
            State.makeClustering(player1.historyStates);
            player1.adjustValueFunctionsWithHistory();
            player1.writeValueFunctionsToDB(player1.valueFunctions);
*/
            //read
/*
            player1.readValueFunctionsFromDB();
            for (int i = 0; i <500 ; i++) {
                Game game=new Game(players);
            }
            System.out.println(Game.count);
*/
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //SingleAgentPlayer Game test
        //Game.trumpCard=new Card('6','d');
        //Player player1=new RelationsNetPlayer();
        /*Player player2=new RandomAgentPlayer();
        ArrayList<Player> players=new ArrayList<>();
        players.add(player1);
        players.add(player2);
        try{
            for (int i = 0; i <1000 ; i++) {
                Game game=new Game(players);
            }
            DurakGame.Conn.CloseDB();
            System.out.println(Game.count);
        }
        catch (Exception e){
            e.printStackTrace();
        }*/

        //DB connection test
        /*try{
            Conn.Conn();
            Conn.CloseDB();
        }
        catch (Exception e) {
            System.out.println(e);
        }*/





    }
}
