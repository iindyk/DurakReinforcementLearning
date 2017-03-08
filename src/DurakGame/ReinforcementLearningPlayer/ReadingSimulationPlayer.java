package DurakGame.ReinforcementLearningPlayer;

import DurakGame.Card;
import DurakGame.Player;

import java.util.ArrayList;

/**
 * Created by HP on 05.03.2017.
 */
public class ReadingSimulationPlayer extends Player {
    private static int count;

    public ReadingSimulationPlayer(){
        this.name="ReadingSimulationPlayer"+count;
        count++;
    }
    @Override
    public ArrayList<Card> attack() {
        return null;
    }

    @Override
    public ArrayList<Card> defend(ArrayList<Card> attackCards) {
        return null;
    }

    @Override
    public boolean canAttack() {
        return false;
    }

    @Override
    public boolean canDefend(ArrayList<Card> attack) {
        return false;
    }

    @Override
    public void takeCardFromDeck() {

    }

    @Override
    public int getCardValue(Card card) {
        return 0;
    }
}
