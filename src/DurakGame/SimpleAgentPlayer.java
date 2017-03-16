package DurakGame;

import DurakGame.ReinforcementLearningPlayer.State;

import java.util.ArrayList;

/**
 * Created by igor on 09.10.16.
 */

//attacks and defends with smallest possible cards
public class SimpleAgentPlayer extends Player {
    private static int count;
    public SimpleAgentPlayer(){
        this.name="SimpleAgent"+count;
        this.state=new State();
        SimpleAgentPlayer.count++;
    }

    @Override
    public Card attack() throws Card.TrumpIsNotDefinedException {
        while (this.state.hand.remove(null)){}
        ArrayList<Card> possibleAttacks=possibleAttacks(this.state.hand,this.state.cardsOnTable);
        Card result=null;
        int minv=1000;
        for (Card possibleAttack:
                possibleAttacks) {
            if (possibleAttack.valueIntWithTrump<minv){
                minv=possibleAttack.valueIntWithTrump;
                result=possibleAttack;
            }
        }
        this.state.hand.remove(result);
        return result;
    }

    @Override
    public Card defend(Card attack) throws Card.TrumpIsNotDefinedException {
        while (this.state.hand.remove(null)){}
        ArrayList<Card> possibleDefences=possibleDefences(this.state.hand,attack);
        Card result=null;
        int minv=1000;
        for (Card possibleDefence:
                possibleDefences) {
            int v=(possibleDefence==null? 100:possibleDefence.valueIntWithTrump);
            if (v<minv){
                minv=v;
                result=possibleDefence;
            }
        }
        this.state.hand.remove(result);
        return result;
    }

    @Override
    public int getCardValue(Card card) {
        if (card==null) return 100;
        else return card.valueInt;
    }

}
