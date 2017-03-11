package DurakGame;

import DurakGame.ReinforcementLearningPlayer.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by igor on 03.10.16.
 */
public class Game {
    private static Card trumpCard;
    public static Player[] players=new Player[2];
    public static ArrayList<Card> deck=new ArrayList<>();
    public static int roundNumber;
    public static ArrayList<Card> cardsOnTable=new ArrayList<>();
    public static ArrayList<Card> outOfTheGame=new ArrayList<>();
    static final Logger logger=Logger.getLogger(Game.class.getName());

    public static HashMap<String,Integer> winnersTable=new HashMap<>();

    public static Card getTrumpCard() {
        return trumpCard;
    }

    public static void setTrumpCard(Card trumpCard) {
        Game.trumpCard = trumpCard;
        Card.defineValuesWithTrump(Game.deck);
        for (Player player:
             Game.players) {
//            Card.defineValuesWithTrump(player.state.hand);
        }
    }

    private Game(){}

    public Game(Player[] players) throws Card.TrumpIsNotDefinedException, EndlessGameException {
        players=players;
        deck=Card.createDeck();
        for (Player player: players
             ) {
            player.state.hand.clear();
            player.state.hiddenCards.clear();
            player.state.cardsOnTable=cardsOnTable;
            player.state.outOfTheGame=outOfTheGame;
            player.state.enemyKnownCards.clear();
            player.state.enemyAttack.clear();
            for (int i = 0; i <6 ; i++) {
                player.takeCardFromDeck();
            }
            logger.log(Level.INFO,player.name+"'s hand is "+player.state.hand);
            player.state.hiddenCards.addAll(deck);
        }
        setTrumpCard(deck.get(0));
        logger.log(Level.INFO,"Trump card is "+trumpCard);
        Card.defineValuesWithTrump(players[0].state.hand);
        Card.defineValuesWithTrump(players[1].state.hand);

        //for 2 players only
        players[0].state.hiddenCards.addAll(players[1].state.hand);
        players[1].state.hiddenCards.addAll(players[0].state.hand);
        //defining attacker
        Random random=new Random();
        int attackedID=random.nextInt(2);
        Player attacker=players[attackedID];
        attacker.state.actionType= State.ActionType.ATTACK;
        logger.log(Level.INFO,"First attacker is "+attacker.name);

        Player defender=players[1-attackedID];
        defender.state.actionType= State.ActionType.DEFENCE;
        logger.log(Level.INFO,"Defender is "+defender.name);

        Player transitPlayer;
        roundNumber=0;
        boolean newAttackerTakesFirst = false;

        while (attacker.state.hand.size()!=0 && defender.state.hand.size()!=0 && roundNumber<1000) {
            for (Player player: players) player.state.roundNumber=roundNumber;
            logger.log(Level.INFO,"Round #"+roundNumber);
            ArrayList<Card> attackCards;
            ArrayList<Card> defenceCards;
            while (attacker.canAttack()&&!(attackCards=attacker.attack()).isEmpty()) {
                cardsOnTable.addAll(attackCards);
                logger.log(Level.INFO,attacker.name+" attacks with "+attackCards+"; cards on the table "+cardsOnTable);
                if (defender.canDefend(attackCards)&&!(defenceCards =defender.defend(attackCards)).isEmpty()) {
                    cardsOnTable.addAll(defenceCards);
                    attacker.state.hiddenCards.removeAll(defenceCards);
                    attacker.state.enemyKnownCards.removeAll(defenceCards);
                    logger.log(Level.INFO, defender.name+" defends with "+ defenceCards+"; cards on the table "+cardsOnTable);
                    if (!attacker.canAttack()) {
                        transitPlayer=attacker;
                        attacker=defender;
                        defender=transitPlayer;
                        attacker.state.actionType= State.ActionType.ATTACK;
                        defender.state.actionType= State.ActionType.DEFENCE;
                        newAttackerTakesFirst=false;
                        break;
                    }
                }
                else {
                    logger.log(Level.INFO,defender.name+" takes cards.");
                    defender.takeCards(cardsOnTable);
                    attacker.state.enemyKnownCards.addAll(cardsOnTable);
                    newAttackerTakesFirst=true;
                    break;
                }
            }
            cardsOnTable.clear();
            if (newAttackerTakesFirst){
                while (attacker.state.hand.size()<6&&deck.size()!=0) attacker.takeCardFromDeck();
                while (defender.state.hand.size()<6&&deck.size()!=0) defender.takeCardFromDeck();
            }
            else {
                while (defender.state.hand.size()<6&&deck.size()!=0) defender.takeCardFromDeck();
                while (attacker.state.hand.size()<6&&deck.size()!=0) attacker.takeCardFromDeck();
            }

            roundNumber++;
        }
        if (attacker.state.hand.size()==0) {
            logger.log(Level.INFO,attacker.name+" wins!");
            winnersTable.put(attacker.name,winnersTable.get(attacker.name)+1);
        }
        else if (roundNumber==999) throw new EndlessGameException();
        else    {
            logger.log(Level.INFO,defender.name+" wins!");
            winnersTable.put(defender.name,winnersTable.get(defender.name)+1);
        }
    }

    public static String getWinnersTable(){
        String w="Statistics of wins:";
        for (Map.Entry<String, Integer> entry:
             winnersTable.entrySet()) {
            w+="\n"+entry.getKey()+": "+entry.getValue();
        }
        return w;
    }

    public static void setWinnersTable(Player[] players){
        winnersTable.put(players[0].name,0);
        winnersTable.put(players[1].name,0);
        logger.log(Level.INFO,"Players are "+players[0].name+" and "+players[1].name);
    }

    public class EndlessGameException extends Exception{}
}
