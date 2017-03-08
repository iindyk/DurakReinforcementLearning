package OptimizationHW;

import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ihor on 20.02.2017
 */
public class Homework2Problem1 {
    public static void main(String[] args){
        //Primal-dual method
        ArrayList<double[]> pefficientPoints =new ArrayList<>();
        double[] u=new double[]{1,1};
        double[] c=new double[]{1,1};

        double[] solution;
        double d;
        double d_k;
        double epsilon=0.01;
        double[] newPpoint;
        //Step 0
        //getting first p-efficient point:
        LinearProgram lpStep0 = new LinearProgram(u);
        //Z_p:
        lpStep0.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{1,1}, 7, "c1"));
        lpStep0.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{1,0}, 3, "c2"));
        lpStep0.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{0,1}, 3, "c3"));
        //adding constraints to get intersection with D:
        lpStep0.addConstraint(new LinearSmallerThanEqualsConstraint(new double[]{1,0}, 100, "c4"));
        lpStep0.addConstraint(new LinearSmallerThanEqualsConstraint(new double[]{0,1}, 100, "c5"));
        LinearProgramSolver solver0  = SolverFactory.newDefault();
        lpStep0.setMinProblem(true);
        newPpoint=solver0.solve(lpStep0);
        pefficientPoints.add(newPpoint);
        int k=1;
        do {
            /*Step 1
            coefficients for LP problem in Step 1(default array values=0):
            first two values for vector c, next for lambdas from simplex S_k */
            double[] lpCoefficientsStep1=new double[2+k];
            lpCoefficientsStep1[0]=c[0];
            lpCoefficientsStep1[1]=c[1];
            LinearProgram lpStep1 = new LinearProgram(lpCoefficientsStep1);
            //defining coefficients for constraints
            double[] cs1 =new double[2+k];
            double[] cs2 =new double[2+k];
            cs1[0]=1;//coefficient near x_1 in g_1
            cs1[1]=2;//coefficient near x_2 in g_1
            cs2[0]=3;//coefficient near x_1 in g_2
            cs2[1]=-1;//coefficient near x_2 in g_2
            //define coefficients for lambdas as -coordinate of p-efficient point
            //for 1 iteration it is just 1 point
            for (int i = 0; i <k ; i++) {
                cs1[2+i]=-pefficientPoints.get(i)[0];
                cs2[2+i]=-pefficientPoints.get(i)[1];
            }
            lpStep1.addConstraint(new LinearBiggerThanEqualsConstraint(cs1, 0, "c1"));
            lpStep1.addConstraint(new LinearBiggerThanEqualsConstraint(cs2, 0, "c2"));
            //x is in D:
            double[] cs3=new double[2+k];
            cs3[0]=1;//all other are 0 by default
            lpStep1.addConstraint(new LinearBiggerThanEqualsConstraint(cs3, 0, "c3"));
            double[] cs4=new double[2+k];
            cs4[1]=1;//all other are 0 by default
            lpStep1.addConstraint(new LinearBiggerThanEqualsConstraint(cs4, 0, "c4"));
            lpStep1.addConstraint(new LinearSmallerThanEqualsConstraint(cs3, 100, "c5"));
            lpStep1.addConstraint(new LinearSmallerThanEqualsConstraint(cs4, 100, "c6"));
            //lambdas are in S_k:
            double[] cs7=new double[k+2];
            for (int i = 0; i < k; i++) cs7[i+2]=1;
            lpStep1.addConstraint(new LinearEqualsConstraint(cs7,1,"c7"));
            for (int i = 0; i < k; i++) {
                double[] cs8=new double[k+2];
                cs8[2+i]=1;
                lpStep1.addConstraint(new LinearBiggerThanEqualsConstraint(cs8, 0, "c8"));
                lpStep1.addConstraint(new LinearBiggerThanEqualsConstraint(cs8, 0, "c9"));
            }
            lpStep1.setMinProblem(true);
            LinearProgramSolver solver1  = SolverFactory.newDefault();
            solution = solver1.solve(lpStep1);
            u[0]=1/5;u[1]=2/5;
            //Step 2
            d_k=pefficientPoints.get(0)[0]*u[0]+pefficientPoints.get(0)[1]*u[1];//d_k=(v_0)_1 * u_1 + (v_0)_2 * u_2
            //finding minimal value(for 1 iteration it is just value above):
            for (double[] pPoint:
                 pefficientPoints) {
                double tmp=pPoint[0]*u[0]+pPoint[1]*u[1];
                if (tmp<d_k) d_k=tmp;
            }
            //Step 3
            LinearProgram lpStep2 = new LinearProgram(u);
            //Z_p:
            lpStep2.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{1,1}, 7, "c1"));
            lpStep2.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{1,0}, 3, "c2"));
            lpStep2.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{0,1}, 3, "c3"));
            //adding constraints to get intersection with D:
            lpStep2.addConstraint(new LinearSmallerThanEqualsConstraint(new double[]{1,0}, 100, "c4"));
            lpStep2.addConstraint(new LinearSmallerThanEqualsConstraint(new double[]{0,1}, 100, "c5"));
            LinearProgramSolver solver2  = SolverFactory.newDefault();
            lpStep2.setMinProblem(true);
            newPpoint=solver2.solve(lpStep2);
            d=newPpoint[0]*u[0]+newPpoint[1]*u[0];
            //Step 4
            pefficientPoints.add(newPpoint);
            k++;
        }
        while (d<d_k-epsilon && k<2);
        System.out.print("Iterations: "+ (k-1)+
                "\nFirst p-efficient point is "+Arrays.toString(pefficientPoints.get(0))+
                "\nSolution is: x1 = "+solution[0]+"; x2 = "+solution[1]+
                "\nMinimal value f is "+(solution[0]*c[0]+solution[1]*c[1])+
                "\ng1 = "+(solution[0]+2*solution[1])+"; g2 = "+(3*solution[0]-solution[1])+
                "\nNew p-efficient point is "+Arrays.toString(newPpoint));
    }
}
