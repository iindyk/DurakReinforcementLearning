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
    static final int FEATURES_NUMBER = 2;
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

    double getRvalue(State currentState, ArrayList<Card> action, State.ActionType actionType) throws State.EmptyEnemyAttackException, State.UndefinedActionException, Card.TrumpIsNotDefinedException, RLPlayer.IncorrectActionException, Card.UnknownSuitException, UndefinedFeatureException {
        RLPlayer.recursionDepth=0;
        HashMap<State,Double> hm=RLPlayer.nextStates(currentState, action);
        State newState;
        if (actionType== State.ActionType.ATTACK) newState = getStateWithMaxReward(hm);
        else newState=getStateWithMinReward(hm);
        logger.log(Level.FINEST,"----State is----"+currentState+"\n---action is---"+action);
        double result = 0;
        for (int i = 0; i < FEATURES_NUMBER; i++) {
            result += this.coefficients[i] * (getBasisFunctionValue(i, newState) - getBasisFunctionValue(i, currentState));
        }
        return result;
    }

    public double getVvalue(ArrayList<State> stateSequence, ArrayList<ArrayList<Card>> actionSequence) throws State.EmptyEnemyAttackException, State.UndefinedActionException, Card.TrumpIsNotDefinedException, RLPlayer.IncorrectActionException, Card.UnknownSuitException, UndefinedFeatureException {
        double result = 0;
        for (int i = 0; i < actionSequence.size(); i++)
            result += getRvalue(stateSequence.get(i), actionSequence.get(i), State.ActionType.ATTACK) * Math.pow(DISCOUNT_FACTOR, i);
        return result;
    }

    static double getBasisFunctionValue(int i, State state) throws UndefinedFeatureException {
        if (i == 0) {
            int sum = 0;
            for (Card card :
                    state.hand) {
                sum += card.valueIntWithTrump;
            }
            if (sum==0) return 1;
            else return ( ((double)sum / state.hand.size())-6) / 17;//normalization
        } else if (i == 1) return -(double) state.hand.size() / 6;//normalization
            //else if (i==2) {                }
        else throw new UndefinedFeatureException();
    }

    public static State getStateWithMaxReward(HashMap<State,Double> states) throws UndefinedFeatureException {
        //Cleans HashMap after defining maxreward state!!!
        RLPlayer.recursionDepth=0;
        State s=new State();
        double maxDefaultReward=-10000;
        states.remove(s);
        for (Map.Entry<State,Double> mentry:
                states.entrySet()) {
            State sp=mentry.getKey();
            double r=0;
            for (int j = 0; j <StateValueFunction.FEATURES_NUMBER ; j++) {
                r+=StateValueFunction.getBasisFunctionValue(j,sp);
            }
            logger.log(Level.FINEST,"Next possible state "+sp+"\n basic reward of the state is "+r);
            if (r>maxDefaultReward){
                maxDefaultReward=r;
                s=sp;
            }
        }
        states.clear();
        logger.log(Level.FINEST,"Maxreward state is "+s);
        return s;
    }

    public static State getStateWithMinReward(HashMap<State,Double> states) throws UndefinedFeatureException{
        //Cleans HashMap after defining maxreward state!!!
        RLPlayer.recursionDepth=0;
        State s=new State();
        double minDefaultReward=10000;
        states.remove(s);
        for (Map.Entry<State,Double> mentry:
                states.entrySet()) {
            State sp=mentry.getKey();
            double r=0;
            for (int j = 0; j <StateValueFunction.FEATURES_NUMBER ; j++) {
                r+=StateValueFunction.getBasisFunctionValue(j,sp);
            }
            logger.log(Level.FINEST,"Next possible state "+sp+"\n basic reward of the state is "+r);
            if (r<minDefaultReward){
                minDefaultReward=r;
                s=sp;
            }
        }
        states.clear();
        logger.log(Level.FINEST,"Minreward state is "+s);
        return s;
    }

    static class UndefinedFeatureException extends Exception {}
}
