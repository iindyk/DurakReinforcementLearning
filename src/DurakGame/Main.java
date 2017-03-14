package DurakGame;

import DurakGame.ReinforcementLearningPlayer.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

        try{
/*          Game.deck=Card.createDeck();
            ArrayList<Card> enemyKnownCards=new ArrayList<>();
            Game.setTrumpCard(new Card('a','s'));
            //hand.add(new Card('6','d'));
            enemyKnownCards.add(new Card('7','s'));
            enemyKnownCards.add(new Card('7','d'));
            enemyKnownCards.add(new Card('a','c'));
            ArrayList<Card> action=new ArrayList<>();
            ArrayList<Card> hand=new ArrayList<>();
            hand.add(new Card('6','c'));
            hand.add(new Card('a','h'));
            hand.add(new Card('7','c'));
            hand.add(new Card('6','s'));
            hand.add(new Card('a','s'));
            action.add(hand.get(0));
            Game.deck.removeAll(hand);
            Game.deck.removeAll(action);
            Game.deck.removeAll(enemyKnownCards);
            ArrayList<Card> cardsOnTable=new ArrayList<>();
            State state=new State(hand,new HashSet<>(),enemyKnownCards,State.ActionType.ATTACK,
                    new ArrayList<>(),cardsOnTable,0);
            state.hiddenCards=new HashSet<>(Game.deck);
            System.out.println(state+"\n action is "+action);
            HashMap<State,Double> hms=RLPlayer.nextStates(state,action);
            System.out.println();
            System.out.println(hms.size());
            System.out.println();
            System.out.println("maxreward state is "+StateValueFunction.getStateWithMaxReward(hms));
            System.out.println(hms.size());
*/

            Handler fileHandler=new FileHandler("./logs/log.log");
            Formatter singleLineFormatter=new SingleLineFormatter();

            Level level=Level.FINEST;
            fileHandler.setLevel(level);
            logger.setLevel(level);
            logger.addHandler(fileHandler);
            fileHandler.setFormatter(singleLineFormatter);


            RLPlayer player0=new RLPlayer();
            Player player1=new SimpleAgentPlayer();
            Player[] players=new Player[]{player0,player1};

            //write
/*
            RLFileReader reader=new RLFileReader();
            player0.addToHistory(reader.readStateActionsFromTxd("D:\\coding\\java\\DurakReinforcementLearning\\games.txd"));
            for (State.StateAction sa:
                    RLPlayer.historyStateActions) {
                if (sa.action.size()>1) System.out.println(sa);
            }
            player0.adjustValueFunctionsWithHistory();
            player0.writeValueFunctionsToDB(player0.valueFunctions);
*/
            //read

            player0.readValueFunctionsFromDB();
            Game.setWinnersTable(players);
            for (int i = 0; i <100 ; i++) {
                new Game(players);
            }
            logger.log(Level.WARNING,Game.getWinnersTable());

        }
        catch (Throwable e){
            logger.log(Level.WARNING,"Exception occurred "+e);
            System.out.println();
            System.out.println();
            e.printStackTrace();
        }
    }
}
