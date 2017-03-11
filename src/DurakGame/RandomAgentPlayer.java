package DurakGame;

import java.util.ArrayList;
import java.util.Random;


/**
 * Created by igor.indyk on 11/17/2016.
 */

//always makes random of possible attacks and defences
public class RandomAgentPlayer extends Player {
    private static int count;
    public RandomAgentPlayer(){
        super();
        RandomAgentPlayer.count++;
        this.name="RandomAgent"+count;
    }

    @Override
    public ArrayList<Card> attack() {
        ArrayList<Card> retainedHand=new ArrayList<>();
        retainedHand.addAll(this.state.hand);
        int in=0;
        if (!(Game.cardsOnTable.size()==0)) {
            for (Card cardOnHand :
                    this.state.hand) {
                for (Card cardOnTable :
                        Game.cardsOnTable) {
                    if (cardOnHand.value == cardOnTable.value) in = 1;
                }
                if (in == 0) retainedHand.remove(cardOnHand);
                else in = 0;
            }
        }
        Random r=new Random();
        ArrayList<Card> result=new ArrayList<>();
        result.add(retainedHand.get(r.nextInt(retainedHand.size())));
        ArrayList<Card> handDuplicate=new ArrayList<>();
        handDuplicate.addAll(state.hand);
        for (Card att:
                result) {
            for (Card card:
                    handDuplicate) {
                if (att.value==card.value&&att.suit==card.suit) this.state.hand.remove(card);
            }
        }
        this.state.hand.removeAll(result);
        return result;
    }

    @Override
    public ArrayList<Card> defend(ArrayList<Card> attack) throws Card.TrumpIsNotDefinedException {
        ArrayList<ArrayList<Card>> possibleDefences = new ArrayList<>();
        ArrayList<Card> availableHand0=new ArrayList<>();
        ArrayList<Card> availableHand1=new ArrayList<>();
        ArrayList<Card> availableHand2=new ArrayList<>();
        ArrayList<Card> tmp=new ArrayList<>();
        //defining possible defences list
        for (int i = 0; i <this.state.hand.size() ; i++) {
            tmp.clear();
            availableHand0.clear();
            availableHand0.addAll(state.hand);
            if (state.hand.get(i).beats(attack.get(0))) {
                tmp.add(state.hand.get(i));
                availableHand0.remove(state.hand.get(i));
                if (attack.size() > 1) {
                    availableHand1.clear();
                    availableHand1.addAll(availableHand0);
                    for (Card card :
                            availableHand0) {
                        if (card.beats(attack.get(1))) {
                            availableHand1.remove(card);
                            tmp.add(card);
                            if (attack.size() > 2) {
                                availableHand2.clear();
                                availableHand2.addAll(availableHand1);
                                for (Card card1 :
                                        availableHand1) {
                                    if (card1.beats(attack.get(2))) {
                                        availableHand2.remove(card1);
                                        tmp.add(card1);
                                        if (attack.size() > 3) {
                                            for (Card card2 :
                                                    availableHand2) {
                                                if (card2.beats(attack.get(3))) {
                                                    tmp.add(card2);
                                                    possibleDefences.add(tmp);
                                                    tmp.clear();
                                                }
                                            }
                                        }
                                        else {
                                            possibleDefences.add(new ArrayList(tmp));
                                            tmp.remove(2);
                                        }
                                    }
                                }
                            }
                            else {
                                possibleDefences.add(new ArrayList(tmp));
                                tmp.remove(1);
                            }

                        }
                    }

                }
                else {
                    possibleDefences.add(new ArrayList(tmp));
                    tmp.remove(0);
                }


            }

        }
        Random r=new Random();
        ArrayList<Card> bestDefence=possibleDefences.get(r.nextInt(possibleDefences.size()));
        ArrayList<Card> handDuplicate=new ArrayList<>();
        handDuplicate.addAll(state.hand);
        for (Card att:
                bestDefence) {
            for (Card card:
                    handDuplicate) {
                if (att.value==card.value&&att.suit==card.suit) this.state.hand.remove(card);
            }
        }
        return bestDefence;
    }

    @Override
    public int getCardValue(Card card) {
        if (card==null) return 100;
        else return card.valueInt;
    }
}
