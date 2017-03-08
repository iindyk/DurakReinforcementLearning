package DurakGame;

import DurakGame.ReinforcementLearningPlayer.RLFileReader;
import DurakGame.ReinforcementLearningPlayer.RLPlayer;
import DurakGame.ReinforcementLearningPlayer.State;
import DurakGame.ReinforcementLearningPlayer.StateValueFunction;


import java.util.ArrayList;

import static DurakGame.Game.getWinnersTable;


/**
 * Created by igor on 28.09.16.
 */
public class Main {
    public static void main(String[] args){
        //value functions adjustment test
        try{
            RLPlayer player0=new RLPlayer();
            SimpleAgentPlayer player1=new SimpleAgentPlayer();
            Player[] players=new Player[]{player0,player1};
            //write
/*
            RLFileReader reader=new RLFileReader();
            player1.addToHistory(reader.readStateActionsFromTxd("D:\\coding\\java\\DurakReinforcementLearning\\games.txd"));
            player1.adjustValueFunctionsWithHistory();
            player1.writeValueFunctionsToDB(player1.valueFunctions);
*/
            //read

            player0.readValueFunctionsFromDB();
            for (int i = 0; i <500 ; i++) {
                Game game=new Game(players);
            }
            System.out.println(Game.getWinnersTable());

/*            Player player0=new RandomAgentPlayer();
            Player player1=new SimpleAgentPlayer();
            Player[] players=new Player[]{player0,player1};
            Game.setWinnersTable(players);
            Game game=new Game(players);
            System.out.println(getWinnersTable());
*/
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
