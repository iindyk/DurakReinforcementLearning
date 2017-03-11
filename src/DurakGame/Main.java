package DurakGame;

import DurakGame.ReinforcementLearningPlayer.*;


import java.util.ArrayList;
import java.util.logging.Formatter;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import static DurakGame.Game.getWinnersTable;
import static DurakGame.Game.logger;


/**
 * Created by igor on 28.09.16.
 */
public class Main {
    public static void main(String[] args){
        //value functions adjustment test
        try{
            Handler fileHandler=new FileHandler("./logs/log.log");
            Formatter singleLineFormatter=new SingleLineFormatter();
            logger.addHandler(fileHandler);
            fileHandler.setFormatter(singleLineFormatter);
            fileHandler.setLevel(Level.OFF);
            logger.setLevel(Level.OFF);


            RLPlayer player0=new RLPlayer();
            SimpleAgentPlayer player1=new SimpleAgentPlayer();
            Player[] players=new Player[]{player0,player1};
            //write
/*
            RLFileReader reader=new RLFileReader();
            player0.addToHistory(reader.readStateActionsFromTxd("D:\\coding\\java\\DurakReinforcementLearning\\games.txd"));
            player0.adjustValueFunctionsWithHistory();
            player0.writeValueFunctionsToDB(player0.valueFunctions);
*/
            //read

            player0.readValueFunctionsFromDB();
            Game.setWinnersTable(players);
            for (int i = 0; i <10 ; i++) {
                Game game=new Game(players);
            }
            logger.log(Level.INFO,Game.getWinnersTable());

/*            Player player0=new RandomAgentPlayer();
            Player player1=new SimpleAgentPlayer();
            Player[] players=new Player[]{player0,player1};
            Game.setWinnersTable(players);
            Game game=new Game(players);
            System.out.println(getWinnersTable());
*/
        }
        catch (Exception e){
            logger.log(Level.WARNING,"Exception occurred "+e);
        }
    }
}
