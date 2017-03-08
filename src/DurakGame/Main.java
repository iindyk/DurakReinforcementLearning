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
            /*
            RLPlayer player1=new RLPlayer();

            SimpleAgentPlayer player2=new SimpleAgentPlayer();
            ArrayList<Player> players=new ArrayList<>();
            players.add(player1);
            players.add(player2);
            //write

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
            Player player0=new RandomAgentPlayer();
            Player player1=new RandomAgentPlayer();
            Player[] players=new Player[]{player0,player1};
            Game game=new Game(players);
            System.out.println(Game.getWinnersTable());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
