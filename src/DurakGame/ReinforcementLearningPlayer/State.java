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
    public HashSet<Card> outOfTheGame=new HashSet<>();
    public ArrayList<Card> enemyKnownCards=new ArrayList<>();
    public HashSet<Card> hiddenCards=new HashSet<>();

    public ArrayList<Card> cardsOnTable=new ArrayList<>();
    public Card enemyAttack;
    public ActionType actionType;
    public int roundNumber;

    static final int NUMBER_OF_CLUSTERS=50;

    public enum ActionType{
        ATTACK,
        DEFENCE,
        UNDEFINED
    }

    public static class StateAction{
        public State state;
        public Card action;
        State nextState; //todo think how to use it
        long gameID;

        private StateAction(){}

        @Override
        public String toString() {
            return "StateAction{" +
                    "state=" + state +
                    ", action=" + action +
                    ", nextState=" + nextState +
                    ", gameID="+gameID+
                    "}\n";
        }

        private StateAction(State state, Card action) {
            this.state = state;
            this.action = action;
        }

        public StateAction(State state, Card action, long gameID) {
            this.state = state;
            this.action = action;
            this.gameID=gameID;
        }
    }

    public State(){
        this.actionType=ActionType.UNDEFINED;
        hand=new ArrayList<>();
        outOfTheGame=new HashSet<>();
        enemyKnownCards=new ArrayList<>();
        hiddenCards=new HashSet<>();
        cardsOnTable=new ArrayList<>();
    }

    public State(ArrayList<Card> hand, HashSet<Card> outOfTheGame, ArrayList<Card> enemyKnownCards, ActionType actionType,
          Card enemyAttack, ArrayList<Card> cardsOnTable, int roundNumber) throws EmptyEnemyAttackException, UndefinedActionException{
        if (actionType==ActionType.DEFENCE && enemyAttack==null) throw new EmptyEnemyAttackException();
        if (!(actionType==ActionType.DEFENCE||actionType==ActionType.ATTACK)) throw new UndefinedActionException();
        this.enemyAttack=enemyAttack;
        this.roundNumber=roundNumber;
        this.cardsOnTable = cardsOnTable;
        this.actionType=actionType;
        this.hand = hand;
        this.outOfTheGame = outOfTheGame;
        this.enemyKnownCards = enemyKnownCards;
    }

    State(State state){
        this.actionType=state.actionType;
        this.enemyAttack=state.enemyAttack;
        this.roundNumber=state.roundNumber;
        this.cardsOnTable=new ArrayList<>(state.cardsOnTable);
        this.hiddenCards=new HashSet<>(state.hiddenCards);
        this.enemyKnownCards=new ArrayList<>(state.enemyKnownCards);
        this.outOfTheGame=new HashSet<>(state.outOfTheGame);
        this.hand=new ArrayList<>(state.hand);
    }

    public int distTo(State state){
        int r=0;
        r+=Card.distTo(this.hand,state.hand);
        //r+=Card.distTo(this.outOfTheGame,state.outOfTheGame);
        r+=Card.distTo(this.enemyKnownCards,state.enemyKnownCards);

        return r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;

        State state = (State) o;

        if (roundNumber != state.roundNumber) return false;
        if (!hand.equals(state.hand)) return false;
        //if (outOfTheGame != null ? !outOfTheGame.equals(state.outOfTheGame) : state.outOfTheGame != null) return false;
        //if (enemyKnownCards != null ? !enemyKnownCards.equals(state.enemyKnownCards) : state.enemyKnownCards != null)
          //  return false;
        //if (!hiddenCards.equals(state.hiddenCards)) return false;
        //if (cardsOnTable != null ? !cardsOnTable.equals(state.cardsOnTable) : state.cardsOnTable != null) return false;
        return (enemyAttack != null ? enemyAttack.equals(state.enemyAttack) : state.enemyAttack == null) && actionType == state.actionType;
    }

    @Override
    public int hashCode() {
        int result = hand.hashCode();
        //result = 31 * result + (outOfTheGame != null ? outOfTheGame.hashCode() : 0);
        //result = 31 * result + (enemyKnownCards != null ? enemyKnownCards.hashCode() : 0);
        //result = 31 * result + hiddenCards.hashCode();
        //result = 31 * result + (cardsOnTable != null ? cardsOnTable.hashCode() : 0);
        //result = 31 * result + (enemyAttack != null ? enemyAttack.hashCode() : 0);
        result = 31 * result + actionType.hashCode();
        result = 31 * result + roundNumber;
        return result;
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
                "}\n";
    }

    static class EmptyEnemyAttackException extends Exception{}

    static class UndefinedActionException extends Exception {}

    static class WrongEnemyAttackException extends Exception {}
}
