package DurakGame.ReinforcementLearningPlayer;

import DurakGame.Card;
import DurakGame.Player;

import java.util.ArrayList;

/**
 * Created by HP on 05.03.2017.
 */
public class ReadingSimulationPlayer extends Player {
    @Override
    public ArrayList<Card> attack(ArrayList<Card> cardsOnTable, char trumpSuit) {
        return null;
    }

    @Override
    public ArrayList<Card> defend(ArrayList<Card> attackCards, char trumpSuit) {
        return null;
    }

    @Override
    public boolean canAttack(ArrayList<Card> cardsOnTable) {
        return false;
    }

    @Override
    public boolean canDefend(ArrayList<Card> attack, char trumpSuit) {
        return false;
    }

    @Override
    public void takeCard(Card card) {

    }

    @Override
    public int getCardValue(Card card) {
        return 0;
    }
}
