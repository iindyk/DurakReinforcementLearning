package DurakGame.ReinforcementLearningPlayer;

import DurakGame.Card;
import DurakGame.Conn;
import DurakGame.Game;
import DurakGame.Player;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static DurakGame.Conn.conn;
import static DurakGame.Conn.resSet;
import static DurakGame.Conn.statmt;
import static DurakGame.Game.logger;

/**
 * Created by HP on 04.02.2017.
 */
public class RLPlayer extends Player {
    private static int count;
    public static ArrayList<State.StateAction> historyStateActions=new ArrayList<>();
    public ArrayList<StateValueFunction> valueFunctions=new ArrayList<>(State.NUMBER_OF_CLUSTERS);
    public static int recursionDepth=0;

    public RLPlayer(){
        for (int i = 0; i <State.NUMBER_OF_CLUSTERS ; i++) {
            double[] coeff=new double[StateValueFunction.FEATURES_NUMBER];
            for (int j = 0; j <StateValueFunction.FEATURES_NUMBER ; j++) coeff[j]=1/StateValueFunction.FEATURES_NUMBER;
            valueFunctions.add(new StateValueFunction(coeff));
        }
        this.name="RLPlayer"+count;
        count++;
    }

    @Override
    public Card attack() throws Card.TrumpIsNotDefinedException {
        while (this.state.hand.remove(null)){}
        ArrayList<Card> possibleActions=possibleActions(this.state);
        logger.log(Level.FINEST,"Possible actions are "+possibleActions);
        Card attack=possibleActions.get(0);
        try {
            double maxReward=this.valueFunctions.get(this.state.roundNumber).getRvalue(this.state,attack);
            for (Card possibleAttack:
                 possibleActions) {
                double possibleReward=this.valueFunctions.get(this.state.roundNumber).getRvalue(this.state,possibleAttack);
                if (maxReward<possibleReward){
                    maxReward=possibleReward;
                    attack=possibleAttack;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.state.hand.remove(attack);
        return attack;
    }

    @Override
    public Card defend(Card attackCard) throws Card.TrumpIsNotDefinedException {
        while (this.state.hand.remove(null)){}
        ArrayList<Card> possibleActions=possibleActions(this.state);
        logger.log(Level.FINEST,"Possible actions are "+possibleActions);
        Card defence=possibleActions.get(0);
        try {
            double maxReward=this.valueFunctions.get(this.state.roundNumber).getRvalue(this.state,defence);
            for (Card possibleDefence:
                    possibleActions) {
                double possibleReward=this.valueFunctions.get(this.state.roundNumber).getRvalue(this.state,possibleDefence);
                if (maxReward<possibleReward){
                    maxReward=possibleReward;
                    defence=possibleDefence;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.state.hand.remove(defence);
        return defence;
    }

    @Override
    public int getCardValue(Card card) {
        return 0;
    }

    public static HashMap<State,Double> nextStates(State currentState, Card action) throws State.EmptyEnemyAttackException, State.UndefinedActionException, Card.TrumpIsNotDefinedException, IncorrectActionException, Card.UnknownSuitException, StateValueFunction.UndefinedFeatureException {
        recursionDepth++;
        if (recursionDepth>100) {
            logger.log(Level.WARNING, "Recursion depth is to high!!! State is "+currentState+"\n action is "+action);
            return new HashMap<>();
        }
        if (currentState==null) return new HashMap<>();
        if (!currentState.hand.contains(action) && action!=null) {
            /*for (State.StateAction sa:
                 historyStateActions) {
                if (sa.state.equals(currentState)) System.out.println(sa.gameID);
            }*/
            logger.log(Level.WARNING,"problem is "+currentState+"action is"+action);
            throw new IncorrectActionException();
        }
        if (currentState.actionType== State.ActionType.DEFENCE && currentState.enemyAttack==null) {
            /*for (State.StateAction sa:
                    historyStateActions) {
                if (sa.state.equals(currentState)) System.out.println(sa.gameID);
            }*/
            logger.log(Level.WARNING,"problem is "+currentState+"action is"+action);
            throw new State.EmptyEnemyAttackException();
        }


        State nextState=new State(currentState);
        nextState.hand.remove(action);
        if (!nextState.cardsOnTable.contains(action)) nextState.cardsOnTable.add(action);

        int sumVal =0;
        for (Card card: currentState.hiddenCards) sumVal +=card.valueIntWithTrump;
        Card avgHiddenCard=new Card(currentState.hiddenCards.isEmpty()? 0:sumVal /currentState.hiddenCards.size());
        if (sumVal !=0) {
            for (int j = 0; nextState.hand.size()<6 && Game.deck.size()>=j; j++) nextState.hand.add(avgHiddenCard);
        }
        HashMap<State,Double> r=new HashMap<>();
        if (currentState.actionType== State.ActionType.ATTACK){
            if (currentState.enemyKnownCards.isEmpty()){
                for (Card additionalAttack:
                     Player.possibleAttacks(nextState.hand,nextState.cardsOnTable)) {
                    State nextState1=new State(nextState);
                    nextState1.enemyKnownCards.add(action);
                    nextState1.enemyKnownCards.add(additionalAttack);
                    nextState1.cardsOnTable.clear();
                    r.put(nextState1,1d);
                    State nextState2=new State(nextState);
                    for (int i = 0; i < (action==null? 0:1)+(additionalAttack==null? 0:1) ; i++) {
                        nextState2.cardsOnTable.add(new Card(24));
                    }
                    nextState2.outOfTheGame.addAll(nextState2.cardsOnTable);
                    nextState2.cardsOnTable.clear();
                    nextState2.actionType= State.ActionType.DEFENCE;
                    r.put(nextState2,1d);
                }
            }
            else if ((action!=null && (Player.canDefend(nextState.enemyKnownCards,action) || nextState.hiddenCards.size()>0))){
                ArrayList<Card> possibleDefences=possibleDefences(nextState.enemyKnownCards,action);
                Card hiddenDefence=new Card(24);
                possibleDefences.add(hiddenDefence);
                for (Card possibleDefence :
                        possibleDefences) {
                    if (possibleDefence==null){
                        nextState.enemyKnownCards.addAll(nextState.cardsOnTable);
                        nextState.cardsOnTable.clear();
                        State nextState1=new State(nextState);
                        nextState1.roundNumber++;
                        r.put(nextState1,1d);
                        continue;
                    }
                    nextState.cardsOnTable=new ArrayList<>(currentState.cardsOnTable);
                    nextState.cardsOnTable.add(action);
                    nextState.cardsOnTable.add(possibleDefence);
                    nextState.enemyKnownCards=new ArrayList<>(currentState.enemyKnownCards);
                    nextState.enemyKnownCards.remove(possibleDefence);
                    //
                    ArrayList<Card> possibleAdditionalAttacks=possibleAttacks(nextState.hand,nextState.cardsOnTable);
                    double min=1000;
                    double max=-1000;
                    for (Card possibleAdditionalAttack:
                         possibleAdditionalAttacks) {
                        double rv=StateValueFunction.getSimpleRvalue(nextState,possibleAdditionalAttack);
                        if (rv>max) max=rv;
                        if (rv<min) min=rv;
                    }
                    for (Card possibleAdditionalAttack:
                            possibleAdditionalAttacks) {
                        if (StateValueFunction.getSimpleRvalue(nextState,possibleAdditionalAttack)>=(max+min)/2) r.putAll(nextStates(nextState,possibleAdditionalAttack));
                    }
                    //
                }
            }
            else {
                //recursion base case
                if (action!=null) {
                    nextState.actionType= State.ActionType.ATTACK;
                    nextState.enemyKnownCards.addAll(nextState.cardsOnTable);
                    nextState.cardsOnTable.clear();
                }
                else {
                    nextState.actionType= State.ActionType.DEFENCE;
                    nextState.outOfTheGame.addAll(nextState.cardsOnTable);
                    nextState.cardsOnTable.clear();
                    nextState.enemyAttack=avgHiddenCard;
                }
                nextState.roundNumber++;
                r.put(new State(nextState),1d);
                return r;
            }
        }
        ///////////////////////////////
        else {
            if (action == null) {
                //recursion base case
                while(nextState.hand.remove(avgHiddenCard)) { }
                nextState.hand.addAll(nextState.cardsOnTable);
                nextState.hiddenCards.removeAll(nextState.cardsOnTable);
                nextState.cardsOnTable.clear();
                nextState.enemyAttack=avgHiddenCard;
                nextState.roundNumber++;
                r.put(new State(nextState), 1d);
                return r;
            }
            else if(nextState.enemyKnownCards.isEmpty()){
                nextState.outOfTheGame.addAll(nextState.cardsOnTable);
                nextState.cardsOnTable.clear();
                nextState.enemyAttack=null;
                nextState.roundNumber++;
                nextState.actionType= State.ActionType.ATTACK;
                //
                r.put(new State(nextState),1d);
                return r;
            }
            else {
                for (Card possibleAdditionalAttack :
                        Player.possibleAttacks(nextState.enemyKnownCards, nextState.cardsOnTable)) {
                    nextState.cardsOnTable = new ArrayList<>(currentState.cardsOnTable);
                    nextState.cardsOnTable.add(action);
                    nextState.cardsOnTable.add(possibleAdditionalAttack);
                    nextState.enemyKnownCards=new ArrayList<>(currentState.enemyKnownCards);
                    nextState.enemyKnownCards.remove(possibleAdditionalAttack);
                    if (possibleAdditionalAttack==null){
                        State nextState1=new State(nextState);
                        if (Game.deck.size() >0) nextState1.hand.add(avgHiddenCard);
                        nextState1.outOfTheGame.addAll(nextState1.cardsOnTable);
                        nextState1.hand.removeAll(nextState1.cardsOnTable);
                        nextState1.enemyKnownCards.removeAll(nextState1.cardsOnTable);
                        nextState1.cardsOnTable.clear();
                        nextState1.actionType= State.ActionType.ATTACK;
                        nextState1.roundNumber++;
                        r.put(nextState1,1d);
                        continue;
                    }
                    //
                    ArrayList<Card> possibleAdditionalDefences=possibleDefences(nextState.hand,possibleAdditionalAttack);
                    double min=1000;
                    double max=-1000;
                    for (Card possibleAdditionalDefence:
                            possibleAdditionalDefences) {
                        double rv=StateValueFunction.getSimpleRvalue(nextState,possibleAdditionalDefence);
                        if (rv>max) max=rv;
                        if (rv<min) min=rv;
                    }
                    for (Card possibleAdditionalDefence :
                            possibleAdditionalDefences) {
                        if (StateValueFunction.getSimpleRvalue(nextState,possibleAdditionalDefence)>=(max+min)/2) r.putAll(nextStates(nextState, possibleAdditionalDefence));
                    }
                    //
                }
            }
        }

        for (Map.Entry<State,Double> mentry: r.entrySet()) mentry.setValue((double)1/r.size());
        return r;
    }

    public void addToHistory(State state,Card action, float reward){//reward?
        historyStateActions.add(new State.StateAction(state,action,0));
    }

    public void addToHistory(State.StateAction stateAction){
        historyStateActions.add(stateAction);
    }

    public void addToHistory(ArrayList<State.StateAction> stateActions){
        this.historyStateActions.addAll(stateActions);
    }

    public void adjustValueFunctionsWithHistory() throws State.EmptyEnemyAttackException, State.UndefinedActionException, Card.TrumpIsNotDefinedException, IncorrectActionException, Card.UnknownSuitException, StateValueFunction.UndefinedFeatureException, StateValueFunction.EmptyStateException {
        valueFunctions.clear();
        //to use the best value prediction
        ArrayList<State.StateAction> stateActions=new ArrayList<>();
        for (int i = 0; i <State.NUMBER_OF_CLUSTERS ; i++) {
            for (State.StateAction historyStateAction : historyStateActions) {
                if (historyStateAction.state.roundNumber == i) {
                    stateActions.add(historyStateAction);
                }
            }
            if (stateActions.isEmpty()) return;
            //LP
            double[] accumulatedCoef=new double[StateValueFunction.FEATURES_NUMBER];
            for (State.StateAction stateAction: stateActions) {
                stateAction.state.cardsOnTable.remove(stateAction.action);//
                double[] lpCoef=new double[StateValueFunction.FEATURES_NUMBER];
                State nextState=StateValueFunction.getStateWithMaxReward(nextStates(stateAction.state,stateAction.action));
                ArrayList<Card> possibleActions=possibleActions(stateAction.state);
                for (Card possibleAction:
                     possibleActions) {
                    if ( stateAction.action!=null &&!stateAction.action.equals(possibleAction)){
                        State nextPossibleState=StateValueFunction.getStateWithMaxReward(nextStates(stateAction.state,possibleAction));
                        for (int j = 0; j <StateValueFunction.FEATURES_NUMBER ; j++) {
                            lpCoef[j]+=(StateValueFunction.getBasisFunctionValue(j,nextPossibleState)-StateValueFunction.getBasisFunctionValue(j,nextState));
                        }
                    }
                }
                LinearProgram lp = new LinearProgram(lpCoef);

                double[] eqcon=new double[StateValueFunction.FEATURES_NUMBER];
                for (int j = 0; j <StateValueFunction.FEATURES_NUMBER ; j++) {
                    double[] cons=new double[StateValueFunction.FEATURES_NUMBER];
                    cons[j]=1;
                    eqcon[j]=1;
                    lp.addConstraint(new LinearBiggerThanEqualsConstraint(cons, 0, "c"+j));
                }
                lp.addConstraint(new LinearSmallerThanEqualsConstraint(eqcon, 1, "c"+StateValueFunction.FEATURES_NUMBER));
                lp.setMinProblem(true);
                LinearProgramSolver solver  = SolverFactory.newDefault();
                double[] solution = solver.solve(lp);
                for (int j = 0; j < StateValueFunction.FEATURES_NUMBER; j++) accumulatedCoef[j]+=solution[j];
            }
            for (int j = 0; j <StateValueFunction.FEATURES_NUMBER ; j++)accumulatedCoef[j]/=stateActions.size();

            this.valueFunctions.add( new StateValueFunction(accumulatedCoef));
            stateActions.clear();
        }

    }

    public void writeValueFunctionsToDB(ArrayList<StateValueFunction> valueFunctions) throws SQLException, ClassNotFoundException {
        new Conn("RLPlayerDB.db");
        statmt = conn.createStatement();
        String sql="INSERT INTO 'valueFunctions' ('id','coef0','coef1','coef2') VALUES ('";
        for (int i = 0; i <valueFunctions.size() ; i++) {
            statmt.execute(sql+i+"','"+valueFunctions.get(i).coefficients[0]+"','"+
            valueFunctions.get(i).coefficients[1]+"','"+ valueFunctions.get(i).coefficients[2]+"')");
        }
        conn.close();
        statmt.close();
    }

    public void readValueFunctionsFromDB() throws SQLException, ClassNotFoundException{
        new Conn("RLPlayerDB.db");
        statmt = conn.createStatement();
        resSet = statmt.executeQuery("SELECT * FROM 'valueFunctions'");
        this.valueFunctions.clear();
        while(resSet.next()) {
            int id = resSet.getInt("id");
            double coef0 = resSet.getDouble("coef0");
            double coef1 = resSet.getDouble("coef1");
            double coef2 = resSet.getDouble("coef2");
            double[] coef=new double[]{coef0,coef1,coef2};
            this.valueFunctions.add(new StateValueFunction(coef));
        }
        resSet.close();
        conn.close();
        statmt.close();
    }

    static class IncorrectActionException extends Exception {}
}
