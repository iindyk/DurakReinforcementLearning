package DurakGame;

import DurakGame.ReinforcementLearningPlayer.State;

import java.util.*;
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
    public static HashSet<Card> outOfTheGame=new HashSet<>();
    public static Player attacker;
    public static Player defender;
    public static final Logger logger=Logger.getLogger(Game.class.getName());
    public static final int CARDS_IN_HAND=6;

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

    public Game(Player[] players) throws Card.TrumpIsNotDefinedException, EndlessGameException, Card.UnknownSuitException {
        Game.players=players;
        deck=Card.createDeck();
        for (Player player: players
             ) {
            player.state.hand.clear();
            player.state.hiddenCards.clear();
            player.state.cardsOnTable=cardsOnTable;
            player.state.outOfTheGame=outOfTheGame;
            player.state.enemyKnownCards.clear();
            player.state.enemyAttack=null;
            for (int i = 0; i <CARDS_IN_HAND ; i++) {
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
        Game.attacker=players[attackedID];
        Game.attacker.state.actionType= State.ActionType.ATTACK;
        logger.log(Level.INFO,"First attacker is "+Game.attacker.name);

        Game.defender=players[1-attackedID];
        Game.defender.state.actionType= State.ActionType.DEFENCE;
        logger.log(Level.INFO,"Defender is "+Game.defender.name);

        Player transitPlayer;
        Game.roundNumber=0;
        boolean defenderTakesCards;

        while (Game.attacker.state.hand.size()!=0 && Game.defender.state.hand.size()!=0 && Game.roundNumber<1000) {
            for (Player player: players) player.state.roundNumber=Game.roundNumber;
            logger.log(Level.INFO,"Round #"+Game.roundNumber);
            logger.log(Level.INFO,players[0].name+" state is "+players[0].state);
            logger.log(Level.INFO,players[1].name+" state is "+players[1].state);
            Card attackCard;
            Card defenceCard;
            defenderTakesCards=false;
            while (Game.attacker.canAttack()&&(attackCard=Game.attacker.attack())!=null) {
                cardsOnTable.add(attackCard);
                logger.log(Level.INFO,Game.attacker.name+" attacks with "+attackCard+"; cards on the table "+cardsOnTable);
                if (Game.defender.canDefend(attackCard)&&(defenceCard =Game.defender.defend(attackCard))!=null && !defenderTakesCards) {
                    cardsOnTable.add(defenceCard);
                    Game.attacker.state.hiddenCards.remove(defenceCard);
                    Game.attacker.state.enemyKnownCards.remove(defenceCard);
                    logger.log(Level.INFO, Game.defender.name+" defends with "+ defenceCard+"; cards on the table "+cardsOnTable);
                    defenderTakesCards=false;

                }
                else defenderTakesCards=true;
            }
            //
            if (!defenderTakesCards) {
                transitPlayer=Game.attacker;
                Game.attacker=Game.defender;
                Game.defender=transitPlayer;
                Game.attacker.state.actionType= State.ActionType.ATTACK;
                Game.defender.state.actionType= State.ActionType.DEFENCE;
            }
            else {
                logger.log(Level.INFO,Game.defender.name+" takes cards.");
                Game.defender.takeCards(cardsOnTable);
                Game.attacker.state.enemyKnownCards.addAll(cardsOnTable);
            }
            //
            cardsOnTable.clear();
            while (Game.defender.state.hand.size()<CARDS_IN_HAND&&deck.size()!=0) Game.defender.takeCardFromDeck();
            while (Game.attacker.state.hand.size()<CARDS_IN_HAND&&deck.size()!=0) Game.attacker.takeCardFromDeck();

            if (deck.size()<2) {
                Game.attacker.state.enemyKnownCards.clear();
                Game.attacker.state.enemyKnownCards.addAll(Game.defender.state.hand);
                Game.attacker.state.hiddenCards.clear();
                Game.defender.state.enemyKnownCards.clear();
                Game.defender.state.enemyKnownCards.addAll(Game.attacker.state.hand);
                Game.defender.state.hiddenCards.clear();
            }

            Game.roundNumber++;
        }
        if (Game.attacker.state.hand.size()==0) {
            logger.log(Level.INFO,Game.attacker.name+" wins!");
            winnersTable.put(Game.attacker.name,winnersTable.get(Game.attacker.name)+1);
        }
        else if (Game.roundNumber==999) throw new EndlessGameException();
        else    {
            logger.log(Level.INFO,Game.defender.name+" wins!");
            winnersTable.put(Game.defender.name,winnersTable.get(defender.name)+1);
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
