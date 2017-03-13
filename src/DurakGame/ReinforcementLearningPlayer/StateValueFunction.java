package DurakGame.ReinforcementLearningPlayer;

import DurakGame.Card;
import DurakGame.Game;
import DurakGame.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    double getRvalue(State currentState, ArrayList<Card> action) throws State.EmptyEnemyAttackException, State.UndefinedActionException, Card.TrumpIsNotDefinedException, RLPlayer.IncorrectActionException {
        HashMap<State,Double> hm=RLPlayer.nextStates(currentState, action);
        if (hm.size()>100) {
            System.out.println(currentState+"action"+action);
            System.out.println(Game.deck);
        }
        State newState = getStateWithMaxReward(hm);
        double result = 0;
        for (int i = 0; i < FEATURES_NUMBER; i++) {
            result += this.coefficients[i] * (getBasisFunctionValue(i, newState) - getBasisFunctionValue(i, currentState));
        }
        return result;
    }

    public double getVvalue(ArrayList<State> stateSequence, ArrayList<ArrayList<Card>> actionSequence) throws State.EmptyEnemyAttackException, State.UndefinedActionException, Card.TrumpIsNotDefinedException, RLPlayer.IncorrectActionException {
        double result = 0;
        for (int i = 0; i < actionSequence.size(); i++)
            result += getRvalue(stateSequence.get(i), actionSequence.get(i)) * Math.pow(DISCOUNT_FACTOR, i);
        return result;
    }

    static double getBasisFunctionValue(int i, State state) {
        if (i == 0) {
            int sum = 0;
            for (Card card :
                    state.hand) {
                sum += card.valueIntWithTrump;
            }
            return ((double) sum / state.hand.size()) / 17;//normalization
        } else if (i == 1) return -(double) state.hand.size() / 4;//normalization
            //else if (i==2) {                }
        else return -1;
    }

    static State getStateWithMaxReward(HashMap<State,Double> states){
        State s=new State();
        double maxDefaultReward=-2;
        for (Map.Entry<State,Double> mentry:
                states.entrySet()) {
            State sp=mentry.getKey();
            double r=0;
            for (int j = 0; j <StateValueFunction.FEATURES_NUMBER ; j++) {
                r+=StateValueFunction.getBasisFunctionValue(j,s);
            }
            if (r>maxDefaultReward){
                maxDefaultReward=r;
                s=sp;
            }
        }
        return s;
    }
}
