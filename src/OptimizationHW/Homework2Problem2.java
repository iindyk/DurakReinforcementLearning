package OptimizationHW;

import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.FunctionsUtils;
import com.joptimizer.functions.PDQuadraticMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.constraints.QuadraticSmallerThanEqualsContraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;
import scpsolver.qpsolver.QuadraticProgram;
import scpsolver.qpsolver.QuadraticProgramSolver;

import java.util.Arrays;

/**
 * Created by HP on 20.02.2017.
 */
public class Homework2Problem2 {
    public static void main(String[] args){
        double[] r=new double[]{15.1,12.5,14.7,9.02,17.68};
        double[][] c=new double[][]{{2.3,0.93,0.62,0.74,-0.23},
                {0.93,1.4,0.22,0.56,-0.26},
                {0.62,0.22,1.8,0.78,-0.27},
                {0.74,0.56,0.78,3.4,-0.56},
                {-0.23,-0.26,-0.27,-0.56,2.6}};
        try{
            double[][] m=new double[2][2];
            //m[0][0]=1;
            //m[1][1]=1;
            // Objective function
            PDQuadraticMultivariateRealFunction objectiveFunction = new PDQuadraticMultivariateRealFunction(m, new double[]{1,1}, 0);
            //inequalities
            ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[1];
            inequalities[0] = FunctionsUtils.createCircle(2, 1, new double[]{0, 0});

            //optimization problem
            OptimizationRequest or = new OptimizationRequest();
            or.setF0(objectiveFunction);
            or.setInitialPoint(new double[] { 0, 0});
            or.setFi(inequalities);
            or.setCheckKKTSolutionAccuracy(true);

            //optimization
            JOptimizer opt = new JOptimizer();
            opt.setOptimizationRequest(or);
            int returnCode = opt.optimize();
            double[] sol = opt.getOptimizationResponse().getSolution();
            System.out.println(Arrays.toString(sol));
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }
    public static double[][] cholesky(double[][] A) {
        int N  = A.length;
        double[][] L = new double[N][N];

        for (int i = 0; i < N; i++)  {
            for (int j = 0; j <= i; j++) {
                double sum = 0.0;
                for (int k = 0; k < j; k++) {
                    sum += L[i][k] * L[j][k];
                }
                if (i == j) L[i][i] = Math.sqrt(A[i][i] - sum);
                else        L[i][j] = 1.0 / L[j][j] * (A[i][j] - sum);
            }
            if (L[i][i] <= 0) {
                throw new RuntimeException("Matrix not positive definite");
            }
        }
        return L;
    }
}
