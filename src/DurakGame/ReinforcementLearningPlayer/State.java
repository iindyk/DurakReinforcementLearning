package DurakGame.ReinforcementLearningPlayer;

import DurakGame.Card;
import DurakGame.Game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by HP on 04.02.2017.
 */
public class State {
    public ArrayList<Card> hand=new ArrayList<>();
    public ArrayList<Card> outOfTheGame=new ArrayList<>();
    public ArrayList<Card> enemyKnownCards=new ArrayList<>();
    public HashSet<Card> hiddenCards=new HashSet<>();
    public ArrayList<Card> cardsOnTable=new ArrayList<>();
    public ArrayList<Card> enemyAttack=new ArrayList<>();
    public ActionType actionType;
    public int roundNumber;

    public static final int NUMBER_OF_CLUSTERS=50;

    public enum ActionType{
        ATTACK,
        DEFENCE
    }

    public static class StateAction{
        State state;
        ArrayList<Card> action;
        State nextState; //todo think how to use it
        long gameID; //is not used now

        private StateAction(){}

        @Override
        public String toString() {
            return "StateAction{" +
                    "state=" + state +
                    ", action=" + action +
                    ", nextState=" + nextState +
                    "}\n";
        }

        public StateAction(State state, ArrayList<Card> action) {
            this.state = state;
            this.action = action;
        }

        public StateAction(State state, ArrayList<Card> action, long gameID) {
            this.state = state;
            this.action = action;
            this.gameID=gameID;
        }
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
        this.hand = hand;
        this.outOfTheGame = outOfTheGame;
        this.enemyKnownCards = enemyKnownCards;
    }

    public State(State state){
        this.actionType=state.actionType;
        this.enemyAttack=new ArrayList<>(state.enemyAttack);
        this.roundNumber=state.roundNumber;
        this.cardsOnTable=new ArrayList<>(state.cardsOnTable);
        this.hiddenCards=new HashSet<>(state.hiddenCards);
        this.enemyKnownCards=new ArrayList<>(state.enemyKnownCards);
        this.outOfTheGame=new ArrayList<>(state.outOfTheGame);
        this.hand=new ArrayList<>(state.hand);
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
