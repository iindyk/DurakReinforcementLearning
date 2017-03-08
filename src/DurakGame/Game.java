package DurakGame;

import DurakGame.ReinforcementLearningPlayer.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
        //System.out.println("Trump card is "+trumpCard);//todo logger
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
            player.state.hiddenCards.addAll(deck);
        }
        setTrumpCard(deck.get(0));
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
        Player defender=players[1-attackedID];
        defender.state.actionType= State.ActionType.DEFENCE;
        Player transitPlayer;
        roundNumber=0;
        while (attacker.state.hand.size()!=0 && defender.state.hand.size()!=0 && roundNumber<1000) {
            for (Player player: players) player.state.roundNumber=roundNumber;
            ArrayList<Card> attackCards;
            ArrayList<Card> defenceCards;
            while (attacker.canAttack()&&!(attackCards=attacker.attack()).isEmpty()) {
                cardsOnTable.addAll(attackCards);
                //System.out.println("attacker is "+attacker.name+"\n"+attacker.state+"\n attack is "+attackCards);
                if (defender.canDefend(attackCards)&&!(defenceCards =defender.defend(attackCards)).isEmpty()) {
                    cardsOnTable.addAll(defenceCards);
                    attacker.state.hiddenCards.removeAll(defenceCards);
                    attacker.state.enemyKnownCards.removeAll(defenceCards);
                    //System.out.println("defender is "+defender.name+"\n"+defender.state+"\n attack is "+ defenceCards);
                    if (!attacker.canAttack()) {
                        transitPlayer=attacker;
                        attacker=defender;
                        defender=transitPlayer;
                        attacker.state.actionType= State.ActionType.ATTACK;
                        defender.state.actionType= State.ActionType.DEFENCE;
                        break;
                    }
                }
                else {
                    defender.takeCards(cardsOnTable);
                    attacker.state.enemyKnownCards.addAll(cardsOnTable);
                    //System.out.println(defender.name + " takes cards "+cardsOnTable);
                    //System.out.println("defender state is "+defender.state);
                    break;
                }
            }
            cardsOnTable.clear();
            while (attacker.state.hand.size()<6&&deck.size()!=0) attacker.takeCardFromDeck();
            while (defender.state.hand.size()<6&&deck.size()!=0) defender.takeCardFromDeck();
            roundNumber++;
        }
        if (attacker.state.hand.size()==0) {
            //System.out.println(attacker.name+" wins!");
            winnersTable.put(attacker.name,winnersTable.get(attacker.name)+1);
        }
        else if (roundNumber==999) throw new EndlessGameException();
        else    {
            //System.out.println(defender.name+ " wins!");
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
    }

    public class EndlessGameException extends Exception{}
}
