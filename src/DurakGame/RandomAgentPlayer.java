package DurakGame;

import java.util.ArrayList;
import java.util.Random;


/**
 * Created by igor.indyk on 11/17/2016.
 */

//always makes random of possible attacks and defences
public class RandomAgentPlayer extends Player {
    private static int count;
    public RandomAgentPlayer(){
        super();
        this.name="RandomAgent"+count;
        RandomAgentPlayer.count++;
    }

    @Override
    public Card attack() throws Card.TrumpIsNotDefinedException {
        while (this.state.hand.remove(null)){}
        ArrayList<Card> possibleAttacks=possibleAttacks(this.state.hand,this.state.cardsOnTable);
        Random random=new Random();
        int r=random.nextInt(possibleAttacks.size());
        Card result=possibleAttacks.get(r);
        this.state.hand.remove(result);
        this.state.hand.remove(result);
        return result;
    }

    @Override
    public Card defend(Card attack) throws Card.TrumpIsNotDefinedException {
        while (this.state.hand.remove(null)){}
        ArrayList<Card> possibleDefences=possibleDefences(this.state.hand,attack);
        Random random=new Random();
        int r=random.nextInt(possibleDefences.size());
        Card result=possibleDefences.get(r);
        this.state.hand.remove(result);
        return result;
    }

    @Override
    public int getCardValue(Card card) {
        if (card==null) return 100;
        else return card.valueInt;
    }
}
