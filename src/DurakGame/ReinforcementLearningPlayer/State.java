package DurakGame.ReinforcementLearningPlayer;

import DurakGame.Card;
import DurakGame.Game;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by HP on 04.02.2017.
 */
public class State {
    public ArrayList<Card> hand=new ArrayList<>();
    public ArrayList<Card> outOfTheGame=new ArrayList<>();
    public ArrayList<Card> enemyKnownCards=new ArrayList<>();
    public ArrayList<Card> hiddenCards=new ArrayList<>();
    int roundNumber;

    public ArrayList<Card> cardsOnTable=new ArrayList<>();
    public ArrayList<Card> enemyAttack=new ArrayList<>();
    public ActionType actionType;

    public static ArrayList<State> states=new ArrayList<>();

    public static final int NUMBER_OF_CLUSTERS=50;

    public enum ActionType{
        ATTACK,
        DEFENCE
    }

    public State(){}

    public State(ArrayList<Card> hand, ArrayList<Card> outOfTheGame, ArrayList<Card> enemyKnownCards,ActionType actionType,
                 ArrayList<Card> enemyAttack,ArrayList<Card> cardsOnTable,int roundNumber) throws EmptyEnemyAttackException, UndefinedActionException{
        if (actionType==ActionType.DEFENCE && enemyAttack.size()==0) throw new EmptyEnemyAttackException();
        if (!(actionType==ActionType.DEFENCE||actionType==ActionType.ATTACK)) throw new UndefinedActionException();
        this.enemyAttack=enemyAttack;
        this.roundNumber=roundNumber;
        this.cardsOnTable = cardsOnTable;
        this.actionType=actionType;
        this.hand = Card.getSorted(hand);
        this.outOfTheGame = Card.getSorted(outOfTheGame);
        this.enemyKnownCards = Card.getSorted(enemyKnownCards);

        this.hiddenCards.clear();
        this.hiddenCards.addAll(Game.deck);
        this.hiddenCards.addAll(Game.players.get(1).state.hand);
        ArrayList<Card> hiddenCardsCopy=Card.getSorted(this.hiddenCards);
        for (int i = 0; i <hiddenCardsCopy.size()-1 ; i++) {
            for (int j = i+1; j <hiddenCardsCopy.size() ; j++) {
                if (hiddenCardsCopy.get(i).equals(hiddenCardsCopy.get(j))) hiddenCardsCopy.remove(j);
            }
        }
        this.hiddenCards=hiddenCardsCopy;
        states.add(this);
    }

    public int distTo(State state){
        int r=0;
        r+=Card.distTo(this.hand,state.hand);
        r+=Card.distTo(this.outOfTheGame,state.outOfTheGame);
        r+=Card.distTo(this.enemyKnownCards,state.enemyKnownCards);

        return r;
    }

    @Override
    public String toString() {
        return "State{" +
                "actionType=" + actionType +
                ", hand=" + hand +
                ", outOfTheGame=" + outOfTheGame +
                ", enemyKnownCards=" + enemyKnownCards +
                "\n, enemyAttack=" + enemyAttack +
                ", cardsOnTable=" + cardsOnTable+
                ", roundNumber=" + roundNumber+
                "\n, hiddenCards="+hiddenCards+
                '}';
    }

    public static class EmptyEnemyAttackException extends Exception{}

    public static class UndefinedActionException extends Exception {}
}
