package DurakGame.ReinforcementLearningPlayer;

import DurakGame.Card;
import DurakGame.Game;
import DurakGame.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static DurakGame.Game.logger;

/**
 * Created by HP on 04.02.2017.
 */
public class StateValueFunction {
    private static final double DISCOUNT_FACTOR = 0.5;
    public static final double LEARNING_FACTOR = 0.1;
    public static final double RANDOM_FACTOR = 0.1;
    static final int FEATURES_NUMBER = 3;
    double[] coefficients = new double[FEATURES_NUMBER];

    private StateValueFunction() {}

    @Override
    public String toString() {
        return "StateValueFunction{\n" +
                "coefficients=" + Arrays.toString(coefficients) + "\n" +
                "}\n";
    }

    StateValueFunction(double[] coefficients) {
        this.coefficients = coefficients;
    }

    double getRvalue(State currentState, Card action) throws State.EmptyEnemyAttackException, State.UndefinedActionException, Card.TrumpIsNotDefinedException, RLPlayer.IncorrectActionException, Card.UnknownSuitException, UndefinedFeatureException, EmptyStateException {
        RLPlayer.recursionDepth=0;
        logger.log(Level.FINEST,"----State is----"+currentState+"\n---action is---"+action+'\n');
        HashMap<State,Double> hm=RLPlayer.nextStates(currentState, action);
        if (hm.isEmpty()) logger.log(Level.WARNING, "Empty possible states set; state is  "+currentState+"\n action is "+action);
        State newState;
        if (currentState.actionType== State.ActionType.ATTACK) newState = getStateWithMaxReward(hm);
        else newState=getStateWithMinReward(hm);
        double result = 0;
        for (int i = 0; i < FEATURES_NUMBER; i++) {
            result += this.coefficients[i] * (getBasisFunctionValue(i, newState) - getBasisFunctionValue(i, currentState));
        }
        return result;
    }

    public double getVvalue(ArrayList<State> stateSequence, ArrayList<Card> actionSequence) throws State.EmptyEnemyAttackException, State.UndefinedActionException, Card.TrumpIsNotDefinedException, RLPlayer.IncorrectActionException, Card.UnknownSuitException, UndefinedFeatureException, EmptyStateException {
        double result = 0;
        for (int i = 0; i < actionSequence.size(); i++)
            result += getRvalue(stateSequence.get(i), actionSequence.get(i)) * Math.pow(DISCOUNT_FACTOR, i);
        return result;
    }

    static double getBasisFunctionValue(int i, State state) throws UndefinedFeatureException {
        while (state.hand.remove(null)){}
        while (state.enemyKnownCards.remove(null)){}
        if (i == 0) {
            int sum = 0;
            for (Card card :
                    state.hand) {
                sum += card.valueIntWithTrump;
            }
            if (sum==0) return 1;
            else return ( ((double)sum / state.hand.size())-6) / 17;//normalization
        } else if (i == 1) return -(double) state.hand.size() / 6;//normalization
        else if (i==2) {
            double hiddenCardValue=0;
            double r=0;
            int enemyCardsQty=0;
            for (Card hc:
                 state.hiddenCards) {
                hiddenCardValue+=hc.valueIntWithTrump;
            }
            if (!state.hiddenCards.isEmpty()) hiddenCardValue/=state.hiddenCards.size();
            for (Card ec:
                 state.enemyKnownCards) {
                r+=ec.valueIntWithTrump;
                enemyCardsQty++;
            }
            while (enemyCardsQty<6 && Game.deck.size()>0) {
                r+=hiddenCardValue;
                enemyCardsQty++;
            }
            r/=enemyCardsQty;
            return r;
        }
        else throw new UndefinedFeatureException();
    }

    private static double getBasicRvalueOfState(State s) throws UndefinedFeatureException {
        double r=0;
        for (int i = 0; i <FEATURES_NUMBER ; i++) {
            r+=getBasisFunctionValue(i,s);
        }
        return r;
    }

    public static double getSimpleRvalue(State state,Card action) throws RLPlayer.IncorrectActionException, State.EmptyEnemyAttackException, UndefinedFeatureException {
        if (state==null||action==null) return 0;
        if (!state.hand.contains(action)) {
            logger.log(Level.WARNING,"problem is "+state+"action is"+action);
            throw new RLPlayer.IncorrectActionException();
        }
        if (state.actionType== State.ActionType.DEFENCE && state.enemyAttack==null) {
            logger.log(Level.WARNING,"problem is "+state+"action is"+action);
            throw new State.EmptyEnemyAttackException();
        }

        State nextState=new State(state);
        nextState.hand.remove(action);
        nextState.cardsOnTable.clear();

        int sumVal =0;
        for (Card card: state.hiddenCards) sumVal +=card.valueIntWithTrump;
        Card avgHiddenCard=new Card(state.hiddenCards.isEmpty()? 0:sumVal /state.hiddenCards.size());
        if (sumVal !=0) {
            for (int j = 0; nextState.hand.size()<Game.CARDS_IN_HAND && Game.deck.size()>0; j++) nextState.hand.add(avgHiddenCard);
        }
        return (getBasicRvalueOfState(nextState)-getBasicRvalueOfState(state));
    }

    public static State getStateWithMaxReward(HashMap<State, Double> states) throws UndefinedFeatureException, EmptyStateException {
        //Cleans HashMap after defining maxreward state!!!
        RLPlayer.recursionDepth=0;
        State s=new State();
        double maxDefaultReward=-10000;
        states.remove(s);
        for (Map.Entry<State,Double> mentry:
                states.entrySet()) {
            State sp=mentry.getKey();
            double r=getBasicRvalueOfState(sp);
            logger.log(Level.FINEST,"Next possible state "+sp+"\n basic reward of the state is "+r+'\n');
            if (r>maxDefaultReward){
                maxDefaultReward=r;
                s=sp;
            }
        }
        if (s.actionType== State.ActionType.UNDEFINED) throw new EmptyStateException();
        states.clear();
        logger.log(Level.FINEST,"Maxreward state is "+s+'\n');
        return s;
    }

    public static State getStateWithMinReward(HashMap<State, Double> states) throws UndefinedFeatureException, EmptyStateException {
        //Cleans HashMap after defining minreward state!!!
        RLPlayer.recursionDepth=0;
        State s=new State();
        double minDefaultReward=10000;
        states.remove(s);
        for (Map.Entry<State,Double> mentry:
                states.entrySet()) {
            State sp=mentry.getKey();
            double r=getBasicRvalueOfState(sp);
            logger.log(Level.FINEST,"Next possible state "+sp+"\n basic reward of the state is "+r+'\n');
            if (r<minDefaultReward){
                minDefaultReward=r;
                s=sp;
            }
        }
        if (s.actionType== State.ActionType.UNDEFINED) {
            logger.log(Level.WARNING,"states are "+states);
            throw new EmptyStateException();
        }
        states.clear();
        logger.log(Level.FINEST,"Minreward state is "+s+'\n');
        return s;
    }

    static class UndefinedFeatureException extends Exception {}

    static class EmptyStateException extends Exception {}
}
