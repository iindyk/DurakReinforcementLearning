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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static DurakGame.Conn.conn;
import static DurakGame.Conn.resSet;
import static DurakGame.Conn.statmt;

/**
 * Created by HP on 04.02.2017.
 */
public class RLPlayer extends Player {
    private static int count;
    public static ArrayList<State.StateAction> historyStateActions=new ArrayList<>();
    public ArrayList<StateValueFunction> valueFunctions=new ArrayList<>(State.NUMBER_OF_CLUSTERS);

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
    public ArrayList<Card> attack() throws Card.TrumpIsNotDefinedException {
        ArrayList<ArrayList<Card>> possibleActions=possibleActions(this.state);
        ArrayList<Card> attack=possibleActions.get(0);
        try {
            double maxReward=this.valueFunctions.get(this.state.roundNumber).getRvalue(this.state,attack);
            for (ArrayList<Card> possibleAttack:
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
        this.state.hand.removeAll(attack);
        return attack;
    }

    @Override
    public ArrayList<Card> defend(ArrayList<Card> attackCards) throws Card.TrumpIsNotDefinedException {
        ArrayList<ArrayList<Card>> possibleActions=possibleActions(this.state);
        ArrayList<Card> defence=possibleActions.get(0);
        try {
            double maxReward=this.valueFunctions.get(this.state.roundNumber).getRvalue(this.state,defence);
            for (ArrayList<Card> possibleDefence:
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
        this.state.hand.removeAll(defence);
        return defence;
    }

    @Override
    public int getCardValue(Card card) {
        return 0;
    }

    public static HashMap<State,Double> nextStates(State currentState, ArrayList<Card> action) throws State.EmptyEnemyAttackException, State.UndefinedActionException, Card.TrumpIsNotDefinedException, IncorrectActionException, Card.UnknownSuitException {
        if (currentState==null||action==null) return null;
        if (!currentState.hand.containsAll(action)) {
            for (State.StateAction sa:
                 historyStateActions) {
                if (sa.state.equals(currentState)) System.out.println(sa.gameID);
            }
            Game.logger.log(Level.WARNING,"problem is "+currentState+"action is"+action);
            throw new IncorrectActionException();
        }
        if (currentState.actionType== State.ActionType.DEFENCE && currentState.enemyAttack.isEmpty()) {
            for (State.StateAction sa:
                    historyStateActions) {
                if (sa.state.equals(currentState)) System.out.println(sa.gameID);
            }
            Game.logger.log(Level.WARNING,"problem is "+currentState+"action is"+action);
            throw new State.EmptyEnemyAttackException();
        }

        //System.out.println(currentState+"\n action is "+action);

        State nextState=new State(currentState);
        nextState.hand.removeAll(action);
        if (!nextState.cardsOnTable.containsAll(action)) nextState.cardsOnTable.addAll(action);

        int sumVal =0;
        for (Card card: currentState.hiddenCards) sumVal +=card.valueIntWithTrump;
        Card avgHiddenCard=new Card(currentState.hiddenCards.isEmpty()? 0:sumVal /currentState.hiddenCards.size());
        if (sumVal !=0) {
            for (int j = 0; j <action.size() && Game.deck.size()<=j; j++) nextState.hand.add(avgHiddenCard);
        }
        HashMap<State,Double> r=new HashMap<>();
        if (currentState.actionType== State.ActionType.ATTACK){
            if (currentState.enemyKnownCards.isEmpty()){
                for (ArrayList<Card> additionalAttack:
                     Player.possibleAttacks(nextState.hand,nextState.cardsOnTable)) {
                    State nextState1=new State(nextState);
                    nextState1.enemyKnownCards.addAll(action);
                    nextState1.enemyKnownCards.addAll(additionalAttack);
                    nextState1.cardsOnTable.clear();
                    r.put(nextState1,1d);
                    State nextState2=new State(nextState);
                    for (int i = 0; i <action.size()+additionalAttack.size() ; i++) {
                        nextState2.cardsOnTable.add(new Card('j','j'));
                    }
                    nextState2.outOfTheGame.addAll(nextState2.cardsOnTable);
                    nextState2.cardsOnTable.clear();
                    nextState2.actionType= State.ActionType.DEFENCE;
                    r.put(nextState2,1d);
                }
            }
            else if ((action.size()!=0 && (Player.canDefend(nextState.enemyKnownCards,action) || nextState.hiddenCards.size()>0))){
                ArrayList<ArrayList<Card>> possibleDefences=possibleDefences(nextState.enemyKnownCards,action);
                ArrayList<Card> hiddenDefence=new ArrayList<>();
                for (Card card: action) hiddenDefence.add(new Card(24));
                possibleDefences.add(hiddenDefence);
                for (ArrayList<Card> possibleDefence :
                        possibleDefences) {
                    if (possibleDefence.isEmpty()){
                        nextState.enemyKnownCards.addAll(nextState.cardsOnTable);
                        nextState.cardsOnTable.clear();
                        State nextState1=new State(nextState);
                        nextState1.roundNumber++;
                        r.put(nextState1,1d);
                        continue;
                    }
                    nextState.cardsOnTable=new ArrayList<>(currentState.cardsOnTable);
                    nextState.cardsOnTable.addAll(action);
                    nextState.cardsOnTable.addAll(possibleDefence);
                    nextState.enemyKnownCards=new ArrayList<>(currentState.enemyKnownCards);
                    nextState.enemyKnownCards.removeAll(possibleDefence);
                    for (ArrayList<Card> possibleAdditionalAttack:
                         possibleAttacks(nextState.hand,nextState.cardsOnTable)) {
                        r.putAll(nextStates(nextState,possibleAdditionalAttack));
                    }
                }
            }
            else {
                //recursion base case
                if (action.size()!=0) {
                    nextState.actionType= State.ActionType.ATTACK;
                    nextState.enemyKnownCards.addAll(nextState.cardsOnTable);
                    nextState.cardsOnTable.clear();
                }
                else {
                    nextState.actionType= State.ActionType.DEFENCE;
                    nextState.outOfTheGame.addAll(nextState.cardsOnTable);
                    nextState.cardsOnTable.clear();
                    nextState.enemyAttack.add(avgHiddenCard);
                }
                nextState.roundNumber++;
                r.put(new State(nextState),1d);
                return r;
            }
        }
        ///////////////////////////////
        else {
            if (action.size() == 0) {
                //recursion base case
                nextState.hand.addAll(nextState.cardsOnTable);
                nextState.hiddenCards.removeAll(nextState.cardsOnTable);
                nextState.cardsOnTable.clear();
                nextState.enemyAttack.clear();
                nextState.enemyAttack.add(avgHiddenCard);
                nextState.roundNumber++;
                r.put(new State(nextState), 1d);
                return r;
            }
            else if(nextState.enemyKnownCards.isEmpty()){
                nextState.outOfTheGame.addAll(nextState.cardsOnTable);
                nextState.cardsOnTable.clear();
                nextState.enemyAttack.clear();
                nextState.roundNumber++;
            }
            else {
                for (ArrayList<Card> possibleAdditionalAttack :
                        Player.possibleAttacks(nextState.enemyKnownCards, nextState.cardsOnTable)) {
                    nextState.cardsOnTable = new ArrayList<>(currentState.cardsOnTable);
                    nextState.cardsOnTable.addAll(action);
                    nextState.cardsOnTable.addAll(possibleAdditionalAttack);
                    nextState.enemyKnownCards=new ArrayList<>(currentState.enemyKnownCards);
                    nextState.enemyKnownCards.removeAll(possibleAdditionalAttack);
                    if (possibleAdditionalAttack.size()==0){
                        State nextState1=new State(nextState);
                        for (int j = 0; j < action.size() && Game.deck.size() <= j; j++) nextState1.hand.add(avgHiddenCard);
                        nextState1.outOfTheGame.addAll(nextState1.cardsOnTable);
                        nextState1.hand.removeAll(nextState1.cardsOnTable);
                        nextState1.enemyKnownCards.removeAll(nextState1.cardsOnTable);
                        nextState1.cardsOnTable.clear();
                        nextState1.actionType= State.ActionType.ATTACK;
                        nextState1.roundNumber++;
                        r.put(nextState1,1d);
                        continue;
                    }
                    for (ArrayList<Card> possibleAdditionalDefence :
                            Player.possibleDefences(nextState.hand, possibleAdditionalAttack)) {
                        //
                        r.putAll(nextStates(nextState, possibleAdditionalDefence));
                    }
                }
            }
        }

        for (Map.Entry<State,Double> mentry: r.entrySet()) mentry.setValue((double)1/r.size());
        return r;
    }

    public void addToHistory(State state,ArrayList<Card> action, float reward){//reward?
        historyStateActions.add(new State.StateAction(state,action,0));
    }

    public void addToHistory(State.StateAction stateAction){
        historyStateActions.add(stateAction);
    }

    public void addToHistory(ArrayList<State.StateAction> stateActions){
        this.historyStateActions.addAll(stateActions);
    }

    public void adjustValueFunctionsWithHistory() throws State.EmptyEnemyAttackException, State.UndefinedActionException, Card.TrumpIsNotDefinedException, IncorrectActionException, Card.UnknownSuitException {
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
                stateAction.state.cardsOnTable.removeAll(stateAction.action);//
                double[] lpCoef=new double[StateValueFunction.FEATURES_NUMBER];
                State nextState=StateValueFunction.getStateWithMaxReward(nextStates(stateAction.state,stateAction.action));
                ArrayList<ArrayList<Card>> possibleActions=possibleActions(stateAction.state);
                for (ArrayList<Card> possibleAction:
                     possibleActions) {
                    if (!(stateAction.action.containsAll(possibleAction)&&possibleAction.containsAll(stateAction.action))){
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
        String sql="INSERT INTO 'valueFunctions' ('id','coef0','coef1') VALUES ('";
        for (int i = 0; i <valueFunctions.size() ; i++) {
            statmt.execute(sql+i+"','"+valueFunctions.get(i).coefficients[0]+"','"+
            valueFunctions.get(i).coefficients[1]+"')");
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
            double[] coef=new double[]{coef0,coef1};
            this.valueFunctions.add(new StateValueFunction(coef));
        }
        resSet.close();
        conn.close();
        statmt.close();
    }

    static class IncorrectActionException extends Exception {}
}
