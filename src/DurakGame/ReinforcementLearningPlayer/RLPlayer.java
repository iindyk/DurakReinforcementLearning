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

import static DurakGame.Conn.conn;
import static DurakGame.Conn.resSet;
import static DurakGame.Conn.statmt;

/**
 * Created by HP on 04.02.2017.
 */
public class RLPlayer extends Player {
    private static int count;
    public State state=new State();
    public ArrayList<State.StateAction> historyStateActions=new ArrayList<>();
    public ArrayList<StateValueFunction> valueFunctions=new ArrayList<>(State.NUMBER_OF_CLUSTERS);

    public RLPlayer(){
        for (int i = 0; i <State.NUMBER_OF_CLUSTERS ; i++) {
            double[] coeff=new double[StateValueFunction.FEATURES_NUMBER];
            for (int j = 0; j <StateValueFunction.FEATURES_NUMBER ; j++) coeff[j]=1/StateValueFunction.FEATURES_NUMBER;
            valueFunctions.add(new StateValueFunction(coeff));
        }
        this.name="RLPlayer"+count;
        count++;
        this.state=new State();
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

    public static State nextState(State currentState,ArrayList<Card> action) throws State.EmptyEnemyAttackException, State.UndefinedActionException, Card.TrumpIsNotDefinedException {
        ArrayList<Card> nextHand=new ArrayList<>();
        nextHand.addAll(currentState.hand);
        nextHand.removeAll(action);
        ArrayList<Card> nextOutOfTheGame=new ArrayList<>();
        nextOutOfTheGame.addAll(currentState.outOfTheGame);
        ArrayList<Card> nextEnemyKnownCards=new ArrayList<>();
        nextEnemyKnownCards.addAll(currentState.enemyKnownCards);
        State.ActionType nextActionType;
        ArrayList<Card> nexEnemyAttack=new ArrayList<>();
        //simplest prediction
        //getting average hidden card
        int sumVal =0;
        int i;
        for (Card card: currentState.hiddenCards) sumVal +=card.valueIntWithTrump;
        Card avgHiddenCard=new Card(currentState.hiddenCards.isEmpty()? 0:sumVal /currentState.hiddenCards.size());
        if (sumVal !=0) {
            for (int j = 0; j <action.size() ; j++) nextHand.add(avgHiddenCard);
        }
        if (currentState.actionType== State.ActionType.ATTACK){
            //getting average beating card
            for (Card card:
                 action) {
                sumVal =0;
                i=0;
                for (Card hcard:
                     currentState.hiddenCards) {
                    if (hcard.beats(card)) {
                        i++;
                        sumVal +=hcard.valueIntWithTrump;
                    }
                }
                if (sumVal !=0) nextOutOfTheGame.add(new Card(sumVal /(i+1)));
            }
            if (sumVal ==0) {
                nextActionType= State.ActionType.ATTACK;
                nextEnemyKnownCards.addAll(action);
            }
            else {
                nextActionType= State.ActionType.DEFENCE;
                nexEnemyAttack.add(avgHiddenCard);
            }

        }
        else{
            if (action.size()==0) {
                nextHand.addAll(Game.cardsOnTable);
                nextActionType= State.ActionType.DEFENCE;
                nexEnemyAttack.add(avgHiddenCard);
            }
            else {
                nextHand.removeAll(action);
                nextActionType= State.ActionType.ATTACK;
            }
        }
        State newState=new State(nextHand,nextOutOfTheGame,nextEnemyKnownCards,nextActionType,nexEnemyAttack, null,
                currentState.roundNumber+1);
        return newState;
    }

    public static ArrayList<ArrayList<Card>> possibleActions(State currentState) throws Card.TrumpIsNotDefinedException {
        ArrayList<ArrayList<Card>> possibleActions=new ArrayList<>(new ArrayList<>());
        if (currentState.actionType== State.ActionType.ATTACK){
            ArrayList<Card> tmp=new ArrayList<>();
            ArrayList<Card> retainedHand=new ArrayList<>();
            retainedHand.addAll(currentState.hand);

            int in=0;
            if (!(currentState.cardsOnTable.isEmpty())){
                possibleActions.add(new ArrayList<>());
                for (Card cardOnHand:
                        currentState.hand) {
                    for (Card cardOnTable:
                            currentState.cardsOnTable) {
                        if (cardOnHand.value==cardOnTable.value) in=1;
                    }
                    if (in==0) retainedHand.remove(cardOnHand);
                    else in=0;
                }
            }

            for (int i = 0; i <retainedHand.size() ; i++) {
                tmp.add(retainedHand.get(i));
                possibleActions.add(new ArrayList<>(tmp));
                for (int j = i+1; j < retainedHand.size(); j++) {
                    if (retainedHand.get(i).value==retainedHand.get(j).value) {
                        tmp.add(retainedHand.get(j));
                        possibleActions.add(new ArrayList<>(tmp));
                        for (int k = j+1; k <retainedHand.size(); k++) {
                            if (retainedHand.get(j).value==retainedHand.get(k).value){
                                tmp.add(retainedHand.get(k));
                                possibleActions.add(new ArrayList<>(tmp));
                                for (int l = k+1; l <retainedHand.size() ; l++) {
                                    if (retainedHand.get(k).value==retainedHand.get(l).value) {
                                        tmp.add(retainedHand.get(l));
                                        possibleActions.add(new ArrayList<>(tmp));
                                    }
                                }
                                tmp.clear();
                            }
                        }
                        tmp.clear();
                    }
                }
                tmp.clear();
            }
        }
        else {
            possibleActions.add(new ArrayList<>());
            ArrayList<Card> availableHand0 = new ArrayList<>();
            ArrayList<Card> availableHand1 = new ArrayList<>();
            ArrayList<Card> availableHand2 = new ArrayList<>();
            ArrayList<Card> tmp = new ArrayList<>();
            for (int i = 0; i < currentState.hand.size(); i++) {
                tmp.clear();
                availableHand0.clear();
                availableHand0.addAll(currentState.hand);
                if (currentState.hand.get(i).beats(currentState.enemyAttack.get(0))) {
                    tmp.add(currentState.hand.get(i));
                    availableHand0.remove(currentState.hand.get(i));
                    if (currentState.enemyAttack.size() > 1) {
                        availableHand1.clear();
                        availableHand1.addAll(availableHand0);
                        for (Card card :
                                availableHand0) {
                            if (card.beats(currentState.enemyAttack.get(1))) {
                                availableHand1.remove(card);
                                tmp.add(card);
                                if (currentState.enemyAttack.size() > 2) {
                                    availableHand2.clear();
                                    availableHand2.addAll(availableHand1);
                                    for (Card card1 :
                                            availableHand1) {
                                        if (card1.beats(currentState.enemyAttack.get(2))) {
                                            availableHand2.remove(card1);
                                            tmp.add(card1);
                                            if (currentState.enemyAttack.size() > 3) {
                                                for (Card card2 :
                                                        availableHand2) {
                                                    if (card2.beats(currentState.enemyAttack.get(3))) {
                                                        tmp.add(card2);
                                                        possibleActions.add(new ArrayList<>(tmp));
                                                        tmp.clear();
                                                    }
                                                }
                                            } else {
                                                possibleActions.add(new ArrayList<>(tmp));
                                                tmp.remove(2);
                                            }
                                        }
                                    }
                                } else {
                                    possibleActions.add(new ArrayList<>(tmp));
                                    tmp.remove(1);
                                }

                            }
                        }

                    } else {
                        possibleActions.add(new ArrayList<>(tmp));
                        tmp.remove(0);
                    }


                }

            }
            ArrayList<ArrayList<Card>> possibleActionsCopy=new ArrayList<>(possibleActions);
            for (int i = 0; i <possibleActionsCopy.size() ; i++) {
                for (int j = i+1; j <possibleActionsCopy.size() ; j++) {
                    ArrayList<Card> l1=possibleActionsCopy.get(i);
                    ArrayList<Card> l2=possibleActionsCopy.get(j);
                    if (l1.containsAll(l2)&&l2.containsAll(l1)) possibleActions.remove(l2);
                }
            }
        }

        return possibleActions;
    }

    public void addToHistory(State state,ArrayList<Card> action, float reward){//reward?
        this.historyStateActions.add(new State.StateAction(state,action));
    }

    public void addToHistory(State.StateAction stateAction){
        this.historyStateActions.add(stateAction);
    }

    public void addToHistory(ArrayList<State.StateAction> stateActions){
        this.historyStateActions.addAll(stateActions);
    }

    public void adjustValueFunctionsWithHistory() throws State.EmptyEnemyAttackException, State.UndefinedActionException, Card.TrumpIsNotDefinedException {
        valueFunctions.clear();
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
                double[] lpCoef=new double[StateValueFunction.FEATURES_NUMBER];
                State nextState=nextState(stateAction.state,stateAction.action);
                ArrayList<ArrayList<Card>> possibleActions=possibleActions(state);
                for (ArrayList<Card> possibleAction:
                     possibleActions) {
                    if (!(stateAction.action.containsAll(possibleAction)&&possibleAction.containsAll(stateAction.action))){
                        State nextPossibleState=nextState(stateAction.state,possibleAction);
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
}
