package DurakGame;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by igor on 03.10.16.
 */
public class Game {
    public static int count;
    private static Card trumpCard;
    public static ArrayList<Player> players=new ArrayList<>();
    public static ArrayList<Card> deck=new ArrayList<>();
    public static int roundNumber;

    public static Card getTrumpCard() {
        return trumpCard;
    }

    public static void setTrumpCard(Card trumpCard) {
        Game.trumpCard = trumpCard;
        /*for (Card card:
             Card.cards) {
            if (card.suit==trumpCard.suit) card.valueIntWithTrump=card.valueInt+9;
        }*/
    }

    public static void updateStates(){
        //player0:
        players.get(0).state.outOfTheGame=outOfTheGame;
        players.get(0).state.hiddenCards.clear();
        players.get(0).state.hiddenCards.addAll(players.get(1).state.hand);
        players.get(0).state.hiddenCards.addAll(deck);

        //player1:

    }

    public static ArrayList<Card> cardsOnTable=new ArrayList<>();
    public static ArrayList<Card> outOfTheGame=new ArrayList<>();//todo use it
    private Game(){
    }
    public Game(ArrayList<Player> players) {
        this.players=players;
        this.deck=Card.createDeck();
        this.trumpCard=deck.get(0);
        Card.defineValuesWithTrump(this.deck);
        //System.out.println("Trump card is "+this.trumpCard);
        for (Player player: players
             ) {
            //System.out.print(player.name + "'s state.hand is ");
            for (int i = 0; i <6 ; i++) {
                Card card=Card.nextCard(this.deck);
                player.takeCard(card);
                //System.out.print(card+"  ");
            }
            //System.out.println();
        }

        //for 2 players only
        Player attacker=players.get(0);
        Player defender=players.get(1);
        Player transitPlayer;
        roundNumber=0;
        while (attacker.state.hand.size()!=0 && defender.state.hand.size()!=0 && roundNumber<1000) {
            /*
            System.out.print(attacker.name + "'s state.hand is ");
            for(Card card: attacker.state.hand) System.out.print(card+"  ");
            System.out.println();
            System.out.print(defender.name + "'s state.hand is ");
            for(Card card: defender.state.hand) System.out.print(card+"  ");
            System.out.println();
            */
            ArrayList<Card> attackCards;
            ArrayList<Card> defendCards;
            //System.out.println("Round " +roundNumber);
            while (attacker.canAttack(this.cardsOnTable)&&!(attackCards=attacker.attack(this.cardsOnTable,this.trumpCard.suit)).isEmpty()) {
                this.cardsOnTable.addAll(attackCards);
                //System.out.println(attacker.name + " attacks with " + attackCards);//
                if (defender.canDefend(attackCards,this.trumpCard.suit)&&!(defendCards=defender.defend(attackCards,this.trumpCard.suit)).isEmpty()) {
                    this.cardsOnTable.addAll(defendCards);
                    //System.out.println(defender.name +" defends with "+ defendCards);//
                    if (!attacker.canAttack(this.cardsOnTable)) {
                        transitPlayer=attacker;
                        attacker=defender;
                        defender=transitPlayer;
                        break;
                    }
                }
                else {
                    for (Card card: this.cardsOnTable) defender.takeCard(card);
                    //System.out.println(defender.name + " takes cards");
                    break;
                }
            }
            this.cardsOnTable.clear();
            while (attacker.state.hand.size()<6&&deck.size()!=0) {
                attacker.takeCard(Card.nextCard(this.deck));

            }
            while (defender.state.hand.size()<6&&deck.size()!=0) {
                defender.takeCard(Card.nextCard(this.deck));
            }
            roundNumber++;
        }
        if (attacker.state.hand.size()==0) System.out.println(attacker.name+" wins!");
        else if (roundNumber==999) System.out.println("Timeout!");
        else    System.out.println(defender.name+ " wins!");
        if (attacker.name.charAt(0)=='R' && attacker.state.hand.size()==0) Game.count++;
        if (defender.name.charAt(0)=='R' && defender.state.hand.size()==0) Game.count++;
    }
}
