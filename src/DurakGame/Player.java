package DurakGame;

import DurakGame.ReinforcementLearningPlayer.State;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by igor on 11.10.16.
 */
public abstract class Player {
    public String name;
    public static int count;
    public State state;
    public abstract ArrayList<Card> attack(ArrayList<Card> cardsOnTable,char trumpSuit);
    public abstract ArrayList<Card> defend(ArrayList<Card> attackCards, char trumpSuit);
    public abstract boolean canAttack(ArrayList<Card> cardsOnTable);
    public abstract boolean canDefend(ArrayList<Card> attack,char trumpSuit);
    public abstract void takeCard(Card card);
    public abstract int getCardValue(Card card);
}
